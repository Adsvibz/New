// O2Dialer license server: Cloudflare Worker + D1 database (both free tier).
// One file. Paste it into a Worker, bind a D1 database named DB, add a secret ADMIN_TOKEN. See README.md.

const HEARTBEAT_SEC = 300;       // how often an app that is open reports in
const OFFLINE_UNLIMITED = 9999;  // offline_hours value that means "no limit"
let schemaReady = false;

const SCHEMA = [
  `CREATE TABLE IF NOT EXISTS codes (
     code TEXT PRIMARY KEY, label TEXT, max_uses INTEGER NOT NULL DEFAULT 1, uses INTEGER NOT NULL DEFAULT 0,
     offline_hours INTEGER NOT NULL DEFAULT 0, can_import INTEGER NOT NULL DEFAULT 1, can_export INTEGER NOT NULL DEFAULT 1,
     expires_at INTEGER, revoked INTEGER NOT NULL DEFAULT 0, created_at INTEGER NOT NULL)`,
  `CREATE TABLE IF NOT EXISTS devices (
     id TEXT PRIMARY KEY, uid TEXT NOT NULL UNIQUE, name TEXT, model TEXT, app_version TEXT, os TEXT, note TEXT,
     code TEXT, token_hash TEXT NOT NULL, status TEXT NOT NULL DEFAULT 'active',
     can_start INTEGER NOT NULL DEFAULT 1, can_import INTEGER NOT NULL DEFAULT 1, can_export INTEGER NOT NULL DEFAULT 1,
     offline_hours INTEGER NOT NULL DEFAULT 0,
     state TEXT, called INTEGER NOT NULL DEFAULT 0, total INTEGER NOT NULL DEFAULT 0,
     calls_today INTEGER NOT NULL DEFAULT 0, answered_today INTEGER NOT NULL DEFAULT 0, campaigns INTEGER NOT NULL DEFAULT 0,
     last_seen INTEGER, last_validate INTEGER, last_campaign INTEGER, created_at INTEGER NOT NULL)`,
  `CREATE TABLE IF NOT EXISTS events (
     id INTEGER PRIMARY KEY AUTOINCREMENT, device_id TEXT, ts INTEGER NOT NULL, type TEXT NOT NULL, detail TEXT)`,
  `CREATE INDEX IF NOT EXISTS idx_events_device ON events(device_id, id DESC)`,
];

async function ensureSchema(env) {
  if (schemaReady) return;
  await env.DB.batch(SCHEMA.map((s) => env.DB.prepare(s)));
  schemaReady = true;
}

// ------------------------------------------------------------------ helpers
const SEC_HEADERS = {
  'X-Content-Type-Options': 'nosniff',
  'Referrer-Policy': 'no-referrer',
  'X-Frame-Options': 'DENY',
  'Cache-Control': 'no-store',
};
const json = (data, status = 200) =>
  new Response(JSON.stringify(data), { status, headers: { 'Content-Type': 'application/json; charset=utf-8', ...SEC_HEADERS } });
const now = () => Date.now();
const str = (v, max) => (typeof v === 'string' && v.length ? v.slice(0, max) : null);
const int = (v, lo, hi, dflt) => {
  if (v === null || v === undefined || v === '') return dflt;
  const n = Math.trunc(Number(v));
  return Number.isFinite(n) ? Math.min(hi, Math.max(lo, n)) : dflt;
};
const flag = (v, dflt) => (v === undefined || v === null ? dflt : v ? 1 : 0);

async function readJson(req) {
  try { return (await req.json()) || {}; } catch { return {}; }
}
async function sha256Hex(s) {
  const buf = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(s));
  return [...new Uint8Array(buf)].map((b) => b.toString(16).padStart(2, '0')).join('');
}
function randomHex(bytes) {
  const a = new Uint8Array(bytes);
  crypto.getRandomValues(a);
  return [...a].map((b) => b.toString(16).padStart(2, '0')).join('');
}
const CODE_ALPHABET = '23456789ABCDEFGHJKLMNPQRSTUVWXYZ'; // no 0/O/1/I
function newCode() {
  const a = new Uint8Array(12);
  crypto.getRandomValues(a);
  const c = [...a].map((b) => CODE_ALPHABET[b % 32]).join('');
  return `O2D-${c.slice(0, 4)}-${c.slice(4, 8)}-${c.slice(8, 12)}`;
}
function normCode(s) {
  if (typeof s !== 'string') return null;
  let c = s.toUpperCase().replace(/[^A-Z0-9]/g, '');
  if (c.startsWith('O2D')) c = c.slice(3);
  if (c.length !== 12) return null;
  return `O2D-${c.slice(0, 4)}-${c.slice(4, 8)}-${c.slice(8, 12)}`;
}
function safeEqual(a, b) {
  if (typeof a !== 'string' || typeof b !== 'string' || a.length !== b.length) return false;
  let r = 0;
  for (let i = 0; i < a.length; i++) r |= a.charCodeAt(i) ^ b.charCodeAt(i);
  return r === 0;
}
function bearer(req) {
  const h = req.headers.get('Authorization') || '';
  return h.startsWith('Bearer ') ? h.slice(7).trim() : '';
}
const isAdmin = (req, env) => !!env.ADMIN_TOKEN && safeEqual(bearer(req), String(env.ADMIN_TOKEN));

async function deviceAuth(req, env) {
  const t = bearer(req);
  if (t.length < 32) return null;
  return await env.DB.prepare('SELECT * FROM devices WHERE token_hash=?').bind(await sha256Hex(t)).first();
}
async function logEvent(env, deviceId, type, detail) {
  await env.DB.prepare('INSERT INTO events (device_id, ts, type, detail) VALUES (?,?,?,?)')
    .bind(deviceId ?? null, now(), type, detail ? String(detail).slice(0, 200) : null).run();
  if (deviceId && Math.random() < 0.05) {
    await env.DB.prepare('DELETE FROM events WHERE device_id=? AND id NOT IN (SELECT id FROM events WHERE device_id=? ORDER BY id DESC LIMIT 200)')
      .bind(deviceId, deviceId).run();
  }
}
const policyOf = (d) => ({
  status: d.status,
  can_start: !!d.can_start,
  can_import: !!d.can_import,
  can_export: !!d.can_export,
  offline_hours: d.offline_hours,
  heartbeat_sec: HEARTBEAT_SEC,
  server_time: now(),
});

// ------------------------------------------------------------------ device API
async function activate(req, env) {
  const b = await readJson(req);
  const code = normCode(b.code);
  const uid = str(b.uid, 64);
  if (!code || !uid) return json({ error: 'bad_request' }, 400);
  const ts = now();
  const existing = await env.DB.prepare('SELECT * FROM devices WHERE uid=?').bind(uid).first();
  const c = await env.DB.prepare('SELECT * FROM codes WHERE code=?').bind(code).first();
  if (!c || c.revoked || (c.expires_at && c.expires_at < ts)) {
    await logEvent(env, existing ? existing.id : null, 'activate_denied', 'invalid code');
    return json({ error: 'invalid_code' }, 403);
  }
  const model = str(b.model, 60);
  const os = str(b.os, 30);
  const app = str(b.app, 20);
  const token = randomHex(32);
  const tokenHash = await sha256Hex(token);

  if (existing) {
    // Known device activating again (reinstall, new token): no extra use is consumed.
    if (existing.status !== 'active') return json({ error: 'suspended' }, 403);
    await env.DB.prepare('UPDATE devices SET token_hash=?, model=?, app_version=?, os=?, last_seen=? WHERE id=?')
      .bind(tokenHash, model, app, os, ts, existing.id).run();
    await logEvent(env, existing.id, 'reactivated_on_device', null);
    return json({ device_id: existing.id, token, policy: policyOf(existing) });
  }

  const used = await env.DB.prepare(
    'UPDATE codes SET uses=uses+1 WHERE code=? AND revoked=0 AND uses<max_uses AND (expires_at IS NULL OR expires_at>?)'
  ).bind(code, ts).run();
  if (!used.meta || !used.meta.changes) {
    await logEvent(env, null, 'activate_denied', 'code fully used');
    return json({ error: 'code_used' }, 403);
  }
  const id = 'd_' + randomHex(6);
  await env.DB.prepare(
    `INSERT INTO devices (id, uid, name, model, app_version, os, code, token_hash, status, can_start, can_import, can_export,
       offline_hours, last_seen, created_at) VALUES (?,?,?,?,?,?,?,?, 'active', 1, ?, ?, ?, ?, ?)`
  ).bind(id, uid, str(b.name, 60) || model || 'Android', model, app, os, code, tokenHash,
    c.can_import, c.can_export, c.offline_hours, ts, ts).run();
  await logEvent(env, id, 'activated', code);
  const dev = await env.DB.prepare('SELECT * FROM devices WHERE id=?').bind(id).first();
  return json({ device_id: id, token, policy: policyOf(dev) });
}

async function validate(req, env) {
  const dev = await deviceAuth(req, env);
  if (!dev) return json({ error: 'unauthorized' }, 401);
  const b = await readJson(req);
  const ts = now();
  await env.DB.prepare('UPDATE devices SET last_seen=?, last_validate=?, app_version=COALESCE(?, app_version), state=COALESCE(?, state) WHERE id=?')
    .bind(ts, ts, str(b.app, 20), str(b.state, 16), dev.id).run();
  if (dev.status !== 'active' || !dev.can_start) await logEvent(env, dev.id, 'validate_denied', dev.status);
  return json({ ok: dev.status === 'active' && !!dev.can_start, policy: policyOf(dev) });
}

async function heartbeat(req, env) {
  const dev = await deviceAuth(req, env);
  if (!dev) return json({ error: 'unauthorized' }, 401);
  const b = await readJson(req);
  await env.DB.prepare(
    `UPDATE devices SET last_seen=?, app_version=COALESCE(?, app_version), state=?, called=?, total=?,
       calls_today=?, answered_today=?, campaigns=? WHERE id=?`
  ).bind(now(), str(b.app, 20), str(b.state, 16) || 'idle', int(b.called, 0, 1e7, 0), int(b.total, 0, 1e7, 0),
    int(b.calls_today, 0, 1e7, 0), int(b.answered_today, 0, 1e7, 0), int(b.campaigns, 0, 1e5, 0), dev.id).run();
  return json({ ok: true, policy: policyOf(dev) });
}

const EVENT_TYPES = ['campaign_start', 'campaign_end', 'app_open'];
async function deviceEvent(req, env) {
  const dev = await deviceAuth(req, env);
  if (!dev) return json({ error: 'unauthorized' }, 401);
  const b = await readJson(req);
  if (!EVENT_TYPES.includes(b.type)) return json({ error: 'bad_request' }, 400);
  if (b.type === 'campaign_start') await env.DB.prepare('UPDATE devices SET last_campaign=?, last_seen=? WHERE id=?').bind(now(), now(), dev.id).run();
  await logEvent(env, dev.id, b.type, str(b.detail, 200));
  return json({ ok: true });
}

// ------------------------------------------------------------------ admin API
async function adminOverview(env) {
  const devices = (await env.DB.prepare(
    `SELECT id, uid, name, model, app_version, os, note, code, status, can_start, can_import, can_export, offline_hours,
       state, called, total, calls_today, answered_today, campaigns, last_seen, last_validate, last_campaign, created_at
     FROM devices ORDER BY COALESCE(last_seen, created_at) DESC`).all()).results;
  const codes = (await env.DB.prepare('SELECT * FROM codes ORDER BY created_at DESC LIMIT 300').all()).results;
  return json({ devices, codes, heartbeat_sec: HEARTBEAT_SEC, server_time: now() });
}

async function adminCreateCodes(req, env) {
  const b = await readJson(req);
  const count = int(b.count, 1, 20, 1);
  const days = int(b.expires_days, 0, 3650, 0);
  const out = [];
  for (let i = 0; i < count; i++) {
    const code = newCode();
    await env.DB.prepare(
      'INSERT INTO codes (code, label, max_uses, offline_hours, can_import, can_export, expires_at, created_at) VALUES (?,?,?,?,?,?,?,?)'
    ).bind(code, str(b.label, 60), int(b.max_uses, 1, 10000, 1), int(b.offline_hours, 0, OFFLINE_UNLIMITED, 0),
      flag(b.can_import, 1), flag(b.can_export, 1), days ? now() + days * 86400000 : null, now()).run();
    out.push(code);
  }
  return json({ codes: out });
}

async function adminCodeAction(req, env, action) {
  const b = await readJson(req);
  const code = normCode(b.code);
  if (!code) return json({ error: 'bad_request' }, 400);
  if (action === 'revoke') await env.DB.prepare('UPDATE codes SET revoked=? WHERE code=?').bind(flag(b.revoked, 1), code).run();
  else await env.DB.prepare('DELETE FROM codes WHERE code=?').bind(code).run();
  return json({ ok: true });
}

async function adminUpdateDevice(req, env) {
  const b = await readJson(req);
  const id = str(b.id, 40);
  if (!id) return json({ error: 'bad_request' }, 400);
  const dev = await env.DB.prepare('SELECT * FROM devices WHERE id=?').bind(id).first();
  if (!dev) return json({ error: 'not_found' }, 404);
  const sets = [];
  const vals = [];
  const put = (col, v) => { sets.push(`${col}=?`); vals.push(v); };
  if (b.name !== undefined) put('name', str(b.name, 60) || dev.name);
  if (b.note !== undefined) put('note', str(b.note, 200));
  if (b.status !== undefined && (b.status === 'active' || b.status === 'suspended')) put('status', b.status);
  if (b.can_start !== undefined) put('can_start', flag(b.can_start, 1));
  if (b.can_import !== undefined) put('can_import', flag(b.can_import, 1));
  if (b.can_export !== undefined) put('can_export', flag(b.can_export, 1));
  if (b.offline_hours !== undefined) put('offline_hours', int(b.offline_hours, 0, OFFLINE_UNLIMITED, 0));
  if (!sets.length) return json({ error: 'nothing_to_update' }, 400);
  await env.DB.prepare(`UPDATE devices SET ${sets.join(', ')} WHERE id=?`).bind(...vals, id).run();
  if (b.status && b.status !== dev.status) await logEvent(env, id, b.status === 'suspended' ? 'suspended' : 'reactivated', 'by admin');
  else await logEvent(env, id, 'settings_changed', Object.keys(b).filter((k) => k !== 'id').join(','));
  return json({ ok: true });
}

async function adminDeleteDevice(req, env) {
  const b = await readJson(req);
  const id = str(b.id, 40);
  if (!id) return json({ error: 'bad_request' }, 400);
  await env.DB.prepare('DELETE FROM events WHERE device_id=?').bind(id).run();
  await env.DB.prepare('DELETE FROM devices WHERE id=?').bind(id).run();
  return json({ ok: true });
}

async function adminEvents(url, env) {
  const id = url.searchParams.get('device');
  const limit = int(url.searchParams.get('limit'), 1, 200, 60);
  const rows = id
    ? (await env.DB.prepare('SELECT ts, type, detail FROM events WHERE device_id=? ORDER BY id DESC LIMIT ?').bind(id, limit).all()).results
    : (await env.DB.prepare('SELECT device_id, ts, type, detail FROM events ORDER BY id DESC LIMIT ?').bind(limit).all()).results;
  return json({ events: rows });
}

// ------------------------------------------------------------------ router
export default {
  async fetch(req, env) {
    const url = new URL(req.url);
    const path = url.pathname.replace(/\/+$/, '') || '/';
    try {
      if (req.method === 'GET' && (path === '/' || path === '/admin')) {
        return new Response(ADMIN_HTML, { headers: { 'Content-Type': 'text/html; charset=utf-8', ...SEC_HEADERS } });
      }
      if (req.method === 'GET' && path === '/health') return json({ ok: true, time: now() });
      if (!env.DB) return json({ error: 'database_not_bound' }, 500);
      await ensureSchema(env);

      if (req.method === 'POST') {
        if (path === '/api/activate') return await activate(req, env);
        if (path === '/api/validate') return await validate(req, env);
        if (path === '/api/heartbeat') return await heartbeat(req, env);
        if (path === '/api/event') return await deviceEvent(req, env);
      }
      if (path.startsWith('/api/admin/')) {
        if (!isAdmin(req, env)) return json({ error: 'unauthorized' }, 401);
        if (req.method === 'GET' && path === '/api/admin/overview') return await adminOverview(env);
        if (req.method === 'GET' && path === '/api/admin/events') return await adminEvents(url, env);
        if (req.method === 'POST') {
          if (path === '/api/admin/codes') return await adminCreateCodes(req, env);
          if (path === '/api/admin/codes/revoke') return await adminCodeAction(req, env, 'revoke');
          if (path === '/api/admin/codes/delete') return await adminCodeAction(req, env, 'delete');
          if (path === '/api/admin/devices/update') return await adminUpdateDevice(req, env);
          if (path === '/api/admin/devices/delete') return await adminDeleteDevice(req, env);
        }
      }
      return json({ error: 'not_found' }, 404);
    } catch (e) {
      return json({ error: 'server_error', detail: String((e && e.message) || e).slice(0, 200) }, 500);
    }
  },
};

// ------------------------------------------------------------------ admin dashboard (Arabic, RTL)
const ADMIN_HTML = `<!doctype html>
<html lang="ar" dir="rtl"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>O2Dialer - لوحة التحكم</title>
<style>
:root{--ink:#171626;--muted:#706e84;--line:#e9e7ef;--soft:#f5f3fb;--violet:#3f2fc4;--indigo:#5b3fe4;--ok:#176a4d;--bad:#b82d3e;--warn:#9a6207}
*{box-sizing:border-box}body{margin:0;font-family:system-ui,-apple-system,"Segoe UI",Tahoma,sans-serif;background:#fbfafd;color:var(--ink)}
header{display:flex;align-items:center;gap:10px;padding:14px 16px;border-bottom:1px solid var(--line);background:#fff;position:sticky;top:0;z-index:5}
header h1{font-size:18px;margin:0;flex:1}main{max-width:980px;margin:0 auto;padding:14px}
section{background:#fff;border:1px solid var(--line);border-radius:18px;padding:14px;margin-bottom:14px}
h2{font-size:16px;margin:0 0 10px}button,input,select{font:inherit}
button{border:1px solid var(--line);background:#fff;border-radius:12px;padding:8px 12px;cursor:pointer;min-height:40px}
button.p{background:var(--indigo);color:#fff;border-color:var(--indigo)}button.d{color:var(--bad);border-color:#efc6cc}
button.s{background:var(--soft)}input,select{border:1px solid #cfcde0;border-radius:12px;padding:8px 10px;min-height:40px;background:#fff}
.row{display:flex;flex-wrap:wrap;gap:8px;align-items:center}.grow{flex:1;min-width:140px}
.card{border:1px solid var(--line);border-radius:16px;padding:12px;margin-bottom:10px}
.badge{display:inline-block;padding:2px 10px;border-radius:99px;font-size:12px;font-weight:700}
.b-ok{background:#e3f1ea;color:var(--ok)}.b-bad{background:#f7e2e5;color:var(--bad)}.b-mute{background:var(--soft);color:var(--muted)}
.dot{display:inline-block;width:9px;height:9px;border-radius:50%;margin-inline-end:6px;background:#bbb}.dot.on{background:#22a06b}
.muted{color:var(--muted);font-size:13px}.code{font-family:ui-monospace,Menlo,Consolas,monospace;direction:ltr;display:inline-block;font-weight:700}
.stats{display:flex;flex-wrap:wrap;gap:6px 14px;margin:6px 0;font-size:13px}.ev{font-size:12px;border-top:1px solid var(--line);padding:5px 0}
.hide{display:none}label.chk{display:inline-flex;gap:5px;align-items:center;font-size:13px}
</style></head><body>
<header><h1>لوحة تحكم O2Dialer</h1><button id="out" class="hide">خروج</button><button id="refresh" class="hide s">تحديث</button></header>
<main>
<section id="login"><h2>تسجيل الدخول</h2><div class="row"><input id="tok" class="grow" type="password" placeholder="رمز الأدمن (ADMIN_TOKEN)" autocomplete="off"><button id="go" class="p">دخول</button></div><p id="lerr" class="muted"></p></section>
<div id="app" class="hide">
<section><h2>إصدار أكواد التفعيل</h2>
<div class="row"><input id="c-label" class="grow" placeholder="اسم / ملاحظة (مثلا: فريق المبيعات)">
<label class="muted">عدد الأجهزة <input id="c-uses" type="number" min="1" max="10000" value="1" style="width:80px"></label>
<label class="muted">عدد الأكواد <input id="c-count" type="number" min="1" max="20" value="1" style="width:70px"></label></div>
<div class="row" style="margin-top:8px"><label class="muted">العمل أوفلاين <select id="c-off"><option value="0">لا (إنترنت مطلوب)</option><option value="24">24 ساعة</option><option value="72">3 أيام</option><option value="168">أسبوع</option><option value="9999">بدون حد</option></select></label>
<label class="muted">صلاحية الكود <select id="c-exp"><option value="0">بدون انتهاء</option><option value="1">يوم</option><option value="7">أسبوع</option><option value="30">شهر</option></select></label>
<label class="chk"><input id="c-imp" type="checkbox" checked>استيراد أرقام</label><label class="chk"><input id="c-exh" type="checkbox" checked>تصدير ومشاركة</label>
<button id="mk" class="p">إصدار</button></div><div id="newcodes" style="margin-top:10px"></div>
<h2 style="margin-top:14px">الأكواد</h2><div id="codes"></div></section>
<section><h2>الأجهزة <span id="cnt" class="muted"></span></h2><div id="devs"></div></section>
<section><h2>آخر النشاط</h2><div id="allev"></div></section>
</div></main>
<script>
(function(){
var TOKEN=localStorage.getItem('o2admin')||'',HB=300,$=function(i){return document.getElementById(i)};
function h(tag,attrs,kids){var e=document.createElement(tag);for(var k in (attrs||{})){if(k==='class')e.className=attrs[k];else if(k==='text')e.textContent=attrs[k];else if(k.slice(0,2)==='on')e.addEventListener(k.slice(2),attrs[k]);else e.setAttribute(k,attrs[k])}
(kids||[]).forEach(function(c){if(c!=null)e.appendChild(typeof c==='string'?document.createTextNode(c):c)});return e}
function api(path,method,body){return fetch(path,{method:method||'GET',headers:{'Authorization':'Bearer '+TOKEN,'Content-Type':'application/json'},body:body?JSON.stringify(body):undefined}).then(function(r){if(r.status===401){logout();throw new Error('unauthorized')}return r.json()})}
function ago(ts){if(!ts)return 'لم يظهر بعد';var s=Math.max(0,Math.round((Date.now()-ts)/1000));if(s<60)return 'الآن';if(s<3600)return 'منذ '+Math.round(s/60)+' د';if(s<86400)return 'منذ '+Math.round(s/3600)+' س';return 'منذ '+Math.round(s/86400)+' يوم'}
var OFF={0:'إنترنت مطلوب',24:'24 ساعة',72:'3 أيام',168:'أسبوع',9999:'بدون حد'};
var STATE={idle:'جاهز',running:'يتصل الآن',paused:'متوقف مؤقتا',waiting:'ينتظر وقت العمل'};
var EV={activated:'تفعيل الجهاز',activate_denied:'محاولة تفعيل مرفوضة',reactivated_on_device:'إعادة تفعيل على الجهاز',validate_denied:'تحقق مرفوض',suspended:'إيقاف الجهاز',reactivated:'إعادة تفعيل من الأدمن',settings_changed:'تغيير إعدادات',campaign_start:'بدء حملة',campaign_end:'انتهاء حملة',app_open:'فتح التطبيق'};
function login(){TOKEN=$('tok').value.trim();if(!TOKEN)return;api('/api/admin/overview').then(function(d){localStorage.setItem('o2admin',TOKEN);show(true);render(d)}).catch(function(){$('lerr').textContent='الرمز غير صحيح'})}
function logout(){localStorage.removeItem('o2admin');TOKEN='';show(false)}
function show(on){$('login').classList.toggle('hide',on);$('app').classList.toggle('hide',!on);$('out').classList.toggle('hide',!on);$('refresh').classList.toggle('hide',!on)}
function load(){return api('/api/admin/overview').then(render).then(loadEvents)}
function upd(id,patch){patch.id=id;return api('/api/admin/devices/update','POST',patch).then(load)}
function render(d){HB=d.heartbeat_sec||300;var devs=$('devs');devs.textContent='';$('cnt').textContent='('+d.devices.length+')';
d.devices.forEach(function(v){var online=v.last_seen&&(d.server_time-v.last_seen)<HB*2.5*1000;var sus=v.status!=='active';
var card=h('div',{class:'card'});
card.appendChild(h('div',{class:'row'},[h('span',{class:'dot'+(online?' on':'')}),h('b',{class:'grow',text:v.name||'جهاز'}),h('span',{class:'badge '+(sus?'b-bad':'b-ok'),text:sus?'موقوف':'مفعّل'}),h('span',{class:'badge b-mute',text:online?'متصل':'غير متصل'})]));
card.appendChild(h('div',{class:'muted',text:(v.model||'')+' | Android '+(v.os||'?')+' | v'+(v.app_version||'?')+' | آخر ظهور: '+ago(v.last_seen)}));
card.appendChild(h('div',{class:'stats'},[h('span',{text:'الحالة: '+(STATE[v.state]||v.state||'-')}),h('span',{text:'اتصالات اليوم: '+v.calls_today}),h('span',{text:'ردّوا: '+v.answered_today}),h('span',{text:'حملات: '+v.campaigns}),h('span',{text:v.state==='running'||v.state==='paused'?('تقدم: '+v.called+'/'+v.total):''}),h('span',{text:'آخر حملة: '+ago(v.last_campaign)})]));
var r1=h('div',{class:'row'});
[['can_start','تشغيل الحملات'],['can_import','استيراد أرقام'],['can_export','تصدير ومشاركة']].forEach(function(p){var cb=h('input',{type:'checkbox'});cb.checked=!!v[p[0]];cb.addEventListener('change',function(){var o={};o[p[0]]=cb.checked;upd(v.id,o)});r1.appendChild(h('label',{class:'chk'},[cb,p[1]]))});
var sel=h('select');Object.keys(OFF).forEach(function(k){var o=h('option',{value:k,text:'أوفلاين: '+OFF[k]});if(String(v.offline_hours)===k)o.selected=true;sel.appendChild(o)});sel.addEventListener('change',function(){upd(v.id,{offline_hours:Number(sel.value)})});r1.appendChild(sel);card.appendChild(r1);
var r2=h('div',{class:'row',style:'margin-top:8px'});
r2.appendChild(h('button',{class:sus?'p':'d',text:sus?'إعادة التفعيل':'إيقاف الجهاز',onclick:function(){upd(v.id,{status:sus?'active':'suspended'})}}));
r2.appendChild(h('button',{text:'تسمية',onclick:function(){var n=prompt('اسم الجهاز',v.name||'');if(n)upd(v.id,{name:n})}}));
r2.appendChild(h('button',{text:'ملاحظة',onclick:function(){var n=prompt('ملاحظة',v.note||'');if(n!==null)upd(v.id,{note:n})}}));
r2.appendChild(h('button',{class:'s',text:'السجل',onclick:function(){showEvents(v.id,card)}}));
r2.appendChild(h('button',{class:'d',text:'حذف',onclick:function(){if(confirm('حذف هذا الجهاز نهائيا؟ سيحتاج كود تفعيل جديد.'))api('/api/admin/devices/delete','POST',{id:v.id}).then(load)}}));
card.appendChild(r2);if(v.note)card.appendChild(h('div',{class:'muted',text:'ملاحظة: '+v.note}));devs.appendChild(card)});
var cs=$('codes');cs.textContent='';d.codes.forEach(function(c){var dead=c.revoked||(c.expires_at&&c.expires_at<d.server_time)||c.uses>=c.max_uses;
cs.appendChild(h('div',{class:'card'},[h('div',{class:'row'},[h('span',{class:'code',text:c.code}),h('span',{class:'badge '+(c.revoked?'b-bad':dead?'b-mute':'b-ok'),text:c.revoked?'ملغي':dead?'منتهي':'متاح'}),h('span',{class:'muted grow',text:(c.label||'')+' | استخدام '+c.uses+'/'+c.max_uses+' | '+(OFF[c.offline_hours]||c.offline_hours+' س')}),
h('button',{text:'نسخ',onclick:function(){navigator.clipboard&&navigator.clipboard.writeText(c.code)}}),
h('button',{text:c.revoked?'استعادة':'إلغاء',onclick:function(){api('/api/admin/codes/revoke','POST',{code:c.code,revoked:!c.revoked}).then(load)}}),
h('button',{class:'d',text:'حذف',onclick:function(){if(confirm('حذف الكود؟'))api('/api/admin/codes/delete','POST',{code:c.code}).then(load)}})])]))})}
function evLine(e,names){return h('div',{class:'ev'},[h('b',{text:(EV[e.type]||e.type)}),' ',h('span',{class:'muted',text:ago(e.ts)+(e.detail?' | '+e.detail:'')+(names&&e.device_id?' | '+(names[e.device_id]||e.device_id):'')})])}
function showEvents(id,card){var old=card.querySelector('.evbox');if(old){old.remove();return}var box=h('div',{class:'evbox'});card.appendChild(box);api('/api/admin/events?device='+encodeURIComponent(id)+'&limit=40').then(function(d){d.events.forEach(function(e){box.appendChild(evLine(e))});if(!d.events.length)box.textContent='لا يوجد نشاط'})}
function loadEvents(){return api('/api/admin/events?limit=25').then(function(d){var box=$('allev');box.textContent='';d.events.forEach(function(e){box.appendChild(evLine(e,{}))});if(!d.events.length)box.textContent='لا يوجد نشاط'})}
$('mk').addEventListener('click',function(){api('/api/admin/codes','POST',{label:$('c-label').value,max_uses:Number($('c-uses').value),count:Number($('c-count').value),offline_hours:Number($('c-off').value),expires_days:Number($('c-exp').value),can_import:$('c-imp').checked,can_export:$('c-exh').checked}).then(function(r){var box=$('newcodes');box.textContent='';(r.codes||[]).forEach(function(c){box.appendChild(h('div',{class:'row'},[h('span',{class:'code',text:c}),h('button',{text:'نسخ',onclick:function(){navigator.clipboard&&navigator.clipboard.writeText(c)}})]))});load()})});
$('go').addEventListener('click',login);$('tok').addEventListener('keydown',function(e){if(e.key==='Enter')login()});
$('out').addEventListener('click',logout);$('refresh').addEventListener('click',load);
if(TOKEN){api('/api/admin/overview').then(function(d){show(true);render(d);loadEvents()}).catch(function(){})}
setInterval(function(){if(TOKEN&&!document.hidden)load().catch(function(){})},20000);
})();
</script></body></html>`;
