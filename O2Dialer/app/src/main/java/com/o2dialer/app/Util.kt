package com.o2dialer.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.telephony.SubscriptionManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Context.has(p: String) = ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED

fun cleanNumber(raw: String): String = buildString {
    for (c in raw) {
        if (c.isDigit()) append(Character.digit(c, 10))
        else if (c == '+' || c == '*' || c == '#') append(c)
    }
}

// ---------- international number auto-correction ----------
private val cc2 = ("20 27 30 31 32 33 34 36 39 40 41 43 44 45 46 47 48 49 51 52 53 54 55 56 57 58 " +
    "60 61 62 63 64 65 66 81 82 84 86 90 91 92 93 94 95 98").split(" ").toSet()

private val cc3 = ("211 212 213 216 218 220 221 222 223 224 225 226 227 228 229 230 231 232 233 234 235 236 " +
    "237 238 239 240 241 242 243 244 245 246 247 248 249 250 251 252 253 254 255 256 257 258 " +
    "260 261 262 263 264 265 266 267 268 269 290 291 297 298 299 350 351 352 353 354 355 356 357 358 359 " +
    "370 371 372 373 374 375 376 377 378 380 381 382 383 385 386 387 389 420 421 423 " +
    "500 501 502 503 504 505 506 507 508 509 590 591 592 593 594 595 596 597 598 599 " +
    "670 672 673 674 675 676 677 678 679 680 681 682 683 685 686 687 688 689 690 691 692 " +
    "850 852 853 855 856 880 886 960 961 962 963 964 965 966 967 968 970 971 972 973 974 975 976 977 " +
    "992 993 994 995 996 998").split(" ").toSet()

private fun hasCc(d: String): Boolean =
    d.startsWith("1") || d.startsWith("7") || (d.length >= 2 && d.take(2) in cc2) || (d.length >= 3 && d.take(3) in cc3)

val isoCc: Map<String, String> = mapOf(
    "eg" to "20", "sa" to "966", "ae" to "971", "kw" to "965", "qa" to "974", "bh" to "973", "om" to "968",
    "jo" to "962", "lb" to "961", "iq" to "964", "sy" to "963", "ps" to "970", "ye" to "967", "ma" to "212",
    "dz" to "213", "tn" to "216", "ly" to "218", "sd" to "249", "us" to "1", "ca" to "1", "gb" to "44",
    "de" to "49", "fr" to "33", "it" to "39", "es" to "34", "tr" to "90", "in" to "91", "pk" to "92",
    "ng" to "234", "za" to "27", "ke" to "254", "br" to "55", "mx" to "52", "ru" to "7", "cn" to "86",
    "au" to "61", "id" to "62", "my" to "60", "sg" to "65", "ph" to "63"
)

/**
 * Normalises any typed number to +<country><number> where possible.
 * 966501234567 -> +966501234567 ; 00201012345678 -> +201012345678 ; 01012345678 -> +201012345678 (default cc 20)
 */
fun smartNumber(raw: String, defCc: String): String {
    val d = cleanNumber(raw)
    if (d.isEmpty() || d.startsWith("*") || d.startsWith("#")) return d
    val digits = d.filter { it.isDigit() }
    if (d.startsWith("+")) return "+$digits"
    if (digits.startsWith("00")) return "+" + digits.drop(2)
    val cc = defCc.filter { it.isDigit() }
    if (digits.startsWith("0")) return if (cc.isNotEmpty()) "+$cc${digits.drop(1)}" else digits
    if (cc.isNotEmpty()) {
        if (digits.startsWith(cc) && digits.length >= cc.length + 7) return "+$digits"
        if (digits.length <= 10) return "+$cc$digits"
    }
    if (digits.length in 10..15 && hasCc(digits)) return "+$digits"
    return digits
}

private val numRegex = Regex("\\+?[0-9٠-٩][0-9٠-٩ \\-()]{5,}")

fun parseLines(text: String): List<Pair<String, String>> = text.lines().mapNotNull { line ->
    var num: String? = null
    val names = mutableListOf<String>()
    for (raw in line.split(',', ';', '\t', '|')) {
        val p = raw.trim()
        if (p.isEmpty()) continue
        val digits = cleanNumber(p)
        val numeric = p.all { it.isDigit() || it in "+-() *#." }
        if (num == null && numeric && digits.length >= 5) num = digits else names.add(p)
    }
    if (num == null) {
        val m = numRegex.find(line)
        if (m != null) {
            num = cleanNumber(m.value)
            names.clear()
            val rest = line.replace(m.value, " ").trim().trim(',', ';', '-', ' ')
            if (rest.isNotEmpty()) names.add(rest)
        }
    }
    num?.let { names.joinToString(" ") to it }
}

fun fmtTime(ms: Long): String = SimpleDateFormat("dd MMM, HH:mm", Locale.US).format(Date(ms))

fun fmtDur(s: Int): String =
    if (s >= 3600) "%d:%02d:%02d".format(s / 3600, (s % 3600) / 60, s % 60)
    else "%d:%02d".format(s / 60, s % 60)

fun shareText(ctx: Context, text: String) {
    if (!License.canExport) { License.denyExport(); return }
    val i = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
    ctx.startActivity(Intent.createChooser(i, "Share"))
}

fun openWhatsApp(ctx: Context, number: String) {
    val n = smartNumber(number, Store.defaultCc()).filter { it.isDigit() }
    try {
        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$n")))
    } catch (e: Exception) {
        Store.notify(Tk.ERROR, T("WhatsApp is not available", "واتساب غير متاح"))
    }
}

fun loadContacts(ctx: Context): List<Pair<String, String>> {
    val out = ArrayList<Pair<String, String>>()
    val seen = HashSet<String>()
    runCatching {
        ctx.contentResolver.query(
            Phone.CONTENT_URI, arrayOf(Phone.DISPLAY_NAME, Phone.NUMBER), null, null, "${Phone.DISPLAY_NAME} ASC"
        )?.use { c ->
            while (c.moveToNext()) {
                val n = cleanNumber(c.getString(1) ?: "")
                if (n.length >= 3 && seen.add(n)) out.add((c.getString(0) ?: "") to n)
            }
        }
    }
    return out
}

/** "SIM 1 · Carrier" labels when the phone lets us read them. */
fun simLabels(ctx: Context): List<String> {
    val base = mutableListOf("SIM 1", "SIM 2")
    if (ctx.has(Manifest.permission.READ_PHONE_STATE)) {
        runCatching {
            val sm = ctx.getSystemService(SubscriptionManager::class.java)
            sm.activeSubscriptionInfoList?.forEach { i ->
                val s = i.simSlotIndex
                if (s in 0..1) base[s] = "SIM ${s + 1} · ${i.carrierName}"
            }
        }
    }
    return base
}
