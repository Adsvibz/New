package com.o2dialer.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** V7 campaign detail header, backed by the actual Store progress model. */
@Composable
fun V7CampaignDetailHero(list: CallList, p: Prog) {
    val cs = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = cs.surface,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, cs.outlineVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = cs.primaryContainer, shape = RoundedCornerShape(13.dp)) {
                    Icon(Icons.Rounded.Groups, null, tint = cs.primary,
                        modifier = Modifier.padding(11.dp).size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(T("CONTACT WORKSPACE", "مساحة جهات الاتصال"),
                        color = cs.primary, style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold)
                    Text(T("Campaign overview", "ملخص الحملة"),
                        color = cs.onSurface, style = MaterialTheme.typography.titleMedium)
                }
                Icon(Icons.Rounded.Timeline, null, tint = cs.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${p.pct}%", fontSize = 35.sp, lineHeight = 39.sp,
                    color = cs.onSurface, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(10.dp))
                Text(T("completed", "مكتملة"), color = cs.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 5.dp))
            }
            LinearProgressIndicator(
                progress = { p.pct / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = cs.primary, trackColor = cs.surfaceVariant
            )
            HorizontalDivider(color = cs.outlineVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                V7DetailStat("${p.total}", T("Contacts", "جهات الاتصال"), Modifier.weight(1f))
                V7DetailStat("${p.answered}", T("Answered", "تم الرد"), Modifier.weight(1f))
                V7DetailStat("${p.remaining}", T("Remaining", "المتبقي"), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun V7DetailStat(value: String, label: String, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
