package com.o2dialer.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** First-run guide uses real product capabilities, with no sample dashboard statistics. */
@Composable
fun WelcomeScreen() {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val cs = MaterialTheme.colorScheme
    val titles = listOf(T("Your next conversation starts here", "مكالمتك الجاية تبدأ هنا"),
        T("A pace that works for you", "تحكّم في إيقاع مكالماتك"), T("Keep every follow-up in sight", "كل متابعة في مكانها"))
    val bodies = listOf(
        T("Organize contacts into campaigns. Add a number, paste a list, or import a CSV or text file.", "نظّم جهات الاتصال في حملات. أضف رقم، الصق قائمة، أو استورد ملف CSV أو نصّي."),
        T("Choose a SIM, set delays and retries, then pause or stop the queue whenever you need.", "اختار الشريحة والفاصل بين المكالمات وعدد المحاولات. تقدر توقف الحملة مؤقتًا أو تنهيها في أي وقت."),
        T("Record outcomes, save notes and follow up on WhatsApp. Campaigns and history stay on this device.", "صنّف النتائج، احفظ ملاحظاتك وتابع على واتساب. الحملات وسجل المكالمات بيتحفظوا على جهازك."))
    val icons = listOf(Icons.Rounded.Campaign, Icons.Rounded.Tune, Icons.Rounded.Insights)
    BackHandler(page > 0) { page-- }
    Responsive {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = Sp.xxl)) {
            Row(Modifier.fillMaxWidth().heightIn(min = 64.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(14.dp), color = cs.primaryContainer) {
                    Box(Modifier.padding(10.dp)) { LogoMark(28.dp, cs.primary) }
                }
                Spacer(Modifier.width(10.dp))
                Text(APP_NAME, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { Store.updateCfg { it.copy(lang = 1 - it.lang) } }) {
                    Text(if (Store.cfg.lang == 0) "العربية" else "English")
                }
            }
            AnimatedContent(page, modifier = Modifier.weight(1f), transitionSpec = {
                fadeIn(tween(Motion.NAV_ENTER_MS)) togetherWith fadeOut(tween(Motion.NAV_EXIT_MS))
            }, label = "welcomePage") { current ->
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = Sp.xxl),
                    verticalArrangement = Arrangement.spacedBy(Sp.xxl)) {
                    Surface(color = cs.primaryContainer, shape = RoundedCornerShape(28.dp)) {
                        Column(Modifier.fillMaxWidth().padding(Sp.xxl), verticalArrangement = Arrangement.spacedBy(Sp.l)) {
                            Icon(icons[current], null, tint = cs.onPrimaryContainer, modifier = Modifier.size(44.dp))
                            Text(T("STEP ${current + 1} OF 3", "الخطوة ${current + 1} من 3"), color = cs.onPrimaryContainer,
                                style = MaterialTheme.typography.labelMedium)
                            Text(titles[current], color = cs.onPrimaryContainer, style = MaterialTheme.typography.headlineLarge)
                        }
                    }
                    Text(bodies[current], style = MaterialTheme.typography.bodyLarge, color = cs.onSurfaceVariant)
                    if (current == 1) Banner(Tk.INFO, T("You stay in control", "التحكّم في إيدك"),
                        T("Pause stops the queue; the current call can continue. Ending calls and changing audio depend on your phone.",
                            "الإيقاف المؤقت بيوقف قائمة الاتصال، والمكالمة الحالية ممكن تستمر. إنهاء المكالمة وتغيير الصوت بيعتمدوا على جهازك."))
                    if (current == 2) Banner(Tk.INFO, T("Permissions when needed", "الصلاحيات وقت الحاجة"),
                        T("Calling and phone-state access are required to start. Call-log access helps verify answers; without it, results are marked unverified. Device licensing may require internet.",
                            "بدء الحملة محتاج صلاحيات الاتصال وحالة الهاتف. سجل المكالمات بيساعد في تأكيد الرد؛ بدونه النتيجة بتظهر غير مؤكدة. ترخيص الجهاز ممكن يحتاج الإنترنت."))
                }
            }
            Row(Modifier.fillMaxWidth().padding(vertical = Sp.l), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { i -> Box(Modifier.weight(1f).height(4.dp).clip(CircleShape)
                    .background(if (i <= page) cs.primary else cs.outlineVariant)) }
            }
            BButton(if (page == 2) T("Open my workspace", "افتح مساحة عملي") else T("Continue", "متابعة"),
                { if (page == 2) Store.finishWelcome() else page++ }, Modifier.fillMaxWidth())
            TextButton(onClick = { if (page == 0) Store.finishWelcome() else page-- }, modifier = Modifier.fillMaxWidth()) {
                Text(if (page == 0) T("Skip introduction", "تخطي المقدمة") else T("Back", "رجوع"))
            }
        }
    }
}
