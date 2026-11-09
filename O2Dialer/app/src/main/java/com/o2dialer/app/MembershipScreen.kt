package com.o2dialer.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.o2dialer.app.ui.components.O2Icons

/**
 * Informational, visual-only membership page.
 * Pricing and entitlements are intentionally NOT invented or connected to licensing.
 */
private data class MembershipOption(
    val id: String,
    val name: String,
    val tagline: String,
    val description: String,
    val highlights: List<String>,
    val featured: Boolean = false
)

@Composable
fun MembershipScreen() {
    val cs = MaterialTheme.colorScheme
    val accent = Brand
    val dark = isDarkTheme()
    val cardColor = if (dark) cs.surface else Color.White
    val lineColor = if (dark) cs.outline.copy(alpha = .23f) else Color(0xFFE8EBEF)
    var detailsId by remember { mutableStateOf<String?>(null) }

    // These are sample *descriptions*, not active products or confirmed price/feature promises.
    val options = listOf(
        MembershipOption(
            "starter", T("Starter", "البداية"),
            T("A simpler way to stay organized.", "كل اللي تحتاجه عشان تبدأ بتنظيم."),
            T("A clean starting point for managing contacts and calling campaigns.",
              "بداية بسيطة لإدارة جهات الاتصال وحملات المكالمات بشكل منظم."),
            listOf(
                T("Campaign organization", "تنظيم الحملات"),
                T("Contact imports", "استيراد جهات الاتصال"),
                T("Calling workflow", "إدارة سير المكالمات"),
                T("History and basic reports", "السجل والتقارير الأساسية")
            )
        ),
        MembershipOption(
            "professional", T("Professional", "الاحترافية"),
            T("More control. Less repetitive work.", "تحكم أكبر وخطوات متكررة أقل."),
            T("A focused choice for professionals managing frequent calling campaigns.",
              "اختيار عملي للمحترفين اللي بيديروا حملات مكالمات بشكل متكرر."),
            listOf(
                T("Everything in Starter", "كل مميزات باقة البداية"),
                T("Advanced campaign controls", "تحكم متقدم في الحملات"),
                T("Extended reporting options", "خيارات تقارير أوسع"),
                T("Additional usage flexibility", "مرونة أكبر في الاستخدام")
            ), featured = true
        ),
        MembershipOption(
            "business", T("Business", "الأعمال"),
            T("Room for demanding daily workflows.", "مساحة أكبر للشغل اليومي المكثف."),
            T("Created for users who need greater capacity and flexible campaign operations.",
              "مخصصة للمستخدمين اللي محتاجين سعة أعلى ومرونة في إدارة الحملات."),
            listOf(
                T("Everything in Professional", "كل مميزات الباقة الاحترافية"),
                T("Expanded campaign capacity", "سعة حملات أكبر"),
                T("Flexible workflow options", "خيارات مرنة لسير العمل"),
                T("Enhanced reporting", "تقارير أكثر تفصيلًا")
            )
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            BlendBar(
                T("Membership", "الاشتراكات"),
                subtitle = T("A plan for the way you work.", "باقة تناسب طريقة شغلك."),
                showLanguage = false,
                showSettings = false
            )
        }
    ) { inner ->
        Column(
            Modifier.fillMaxSize().padding(inner).o2BackdropSource()
                .verticalScroll(rememberScrollState())
                .padding(start = Sp.xl, end = Sp.xl, top = ScreenFrame.contentTop, bottom = ScreenFrame.contentBottom),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // The billing system is not wired yet; never claim an actual active plan.
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp))
                    .background(cardColor)
                    .border(1.dp, lineColor, RoundedCornerShape(17.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(O2Icons.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp), tint = accent)
                    Text(
                        T("MONTHLY MEMBERSHIP", "العضوية الشهرية"),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                        color = cs.onSurfaceVariant
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        T("COMING SOON", "قريبًا"),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                        color = accent
                    )
                }
                Text(
                    T("Plans made for your workflow", "باقات تناسب شغلك"),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                    color = cs.onSurface
                )
                Text(
                    T("Explore the upcoming monthly options. No payment or subscription is available yet.",
                      "تعرّف على الباقات الشهرية القادمة. الاشتراك والدفع غير متاحين حاليًا."),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 18.sp),
                    color = cs.onSurfaceVariant
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    T("Choose your plan", "اختار باقتك"),
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp, fontWeight = FontWeight.SemiBold),
                    color = cs.onSurface
                )
                Text(
                    T("Monthly options, without the clutter.", "خيارات شهرية واضحة ومن غير تعقيد."),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 18.sp),
                    color = cs.onSurfaceVariant
                )
            }

            options.forEach { option ->
                MembershipPlanCard(
                    option = option,
                    accent = accent,
                    background = cardColor,
                    borderColor = lineColor,
                    onView = { detailsId = option.id }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    T("Why O2Dialer?", "ليه O2Dialer؟"),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                    color = cs.onSurface
                )
                MembershipBenefit(
                    O2Icons.FolderOpen,
                    T("Stay organized", "كل حاجة منظمة"),
                    T("Campaigns and contacts in one place.", "الحملات وجهات الاتصال في مكان واحد.")
                )
                MembershipBenefit(
                    O2Icons.Bolt,
                    T("Work efficiently", "شغل أسرع"),
                    T("Fewer repetitive steps every day.", "خطوات متكررة أقل كل يوم.")
                )
                MembershipBenefit(
                    O2Icons.Chart,
                    T("Review your progress", "تابع تقدمك"),
                    T("Clear campaign activity and reporting.", "عرض واضح لنشاط الحملات وتقاريرها.")
                )
            }

            HorizontalDivider(color = lineColor)
            Text(
                T("Pricing, benefits, billing, renewal and cancellation terms will be confirmed before any plan becomes available.",
                  "الأسعار والمميزات وتفاصيل الدفع والتجديد والإلغاء هتتحدد بوضوح قبل إتاحة الاشتراك."),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, lineHeight = 17.sp),
                color = cs.onSurfaceVariant
            )
        }
    }

    val detail = options.firstOrNull { it.id == detailsId }
    if (detail != null) {
        AlertDialog(
            onDismissRequest = { detailsId = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = cardColor,
            tonalElevation = 0.dp,
            title = {
                Text(detail.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(detail.description, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                    detail.highlights.forEach { feature ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(O2Icons.Check, contentDescription = null, tint = accent, modifier = Modifier.size(15.dp))
                            Text(feature, style = MaterialTheme.typography.bodySmall, color = cs.onSurface)
                        }
                    }
                    Text(
                        T("This is a plan preview, not a purchase. Pricing and availability will be announced later.",
                          "دي معاينة للباقة فقط وليست عملية شراء. السعر والإتاحة هيتعلنوا لاحقًا."),
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { detailsId = null }) {
                    Text(T("Close", "إغلاق"), color = accent)
                }
            }
        )
    }
}

@Composable
private fun MembershipPlanCard(
    option: MembershipOption,
    accent: Color,
    background: Color,
    borderColor: Color,
    onView: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(18.dp)
    val outline = if (option.featured) accent.copy(alpha = .70f) else borderColor
    Column(
        Modifier.fillMaxWidth().clip(shape).background(background)
            .border(1.dp, outline, shape).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                option.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                color = cs.onSurface
            )
            if (option.featured) {
                Text(
                    T("MOST POPULAR", "الأكثر طلبًا"),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                    color = accent,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                        .background(accent.copy(alpha = .08f))
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                T("Monthly pricing", "اشتراك شهري"),
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                color = cs.onSurfaceVariant
            )
            Text(
                T("Price coming soon", "السعر يُعلن قريبًا"),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                color = cs.onSurface
            )
        }
        Text(
            option.tagline,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 19.sp),
            color = cs.onSurface
        )
        Text(
            option.description,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 18.sp),
            color = cs.onSurfaceVariant
        )
        HorizontalDivider(color = borderColor)
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            option.highlights.forEach { feature ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Icon(
                        O2Icons.Check, contentDescription = null,
                        modifier = Modifier.size(14.dp), tint = accent
                    )
                    Text(
                        feature,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 17.sp),
                        color = cs.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        val interaction = remember { MutableInteractionSource() }
        val filled = option.featured
        Box(
            Modifier.fillMaxWidth().heightIn(min = 43.dp)
                .pressScale(interaction)
                .clip(RoundedCornerShape(12.dp))
                .background(if (filled) accent else background)
                .border(1.dp, if (filled) accent else borderColor, RoundedCornerShape(12.dp))
                .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onView)
                .padding(horizontal = 12.dp, vertical = 11.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                T("View plan", "تفاصيل الباقة"),
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                color = if (filled) Color.White else cs.onSurface
            )
        }
    }
}

@Composable
private fun MembershipBenefit(icon: ImageVector, title: String, description: String) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp), tint = Brand)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = cs.onSurface)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 17.sp),
                color = cs.onSurfaceVariant
            )
        }
    }
}
