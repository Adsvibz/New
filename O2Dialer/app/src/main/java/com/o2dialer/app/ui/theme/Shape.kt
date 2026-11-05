package com.o2dialer.app.ui.theme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp
object O2Shape {
    val Card = RoundedCornerShape(24.dp)
    val Pill = RoundedCornerShape(50)
    val Input = RoundedCornerShape(12.dp)
    val Sheet = RoundedCornerShape(topStart=24.dp, topEnd=24.dp)
    val Material = Shapes(RoundedCornerShape(8.dp),Input,RoundedCornerShape(16.dp),Card,RoundedCornerShape(32.dp))
}
