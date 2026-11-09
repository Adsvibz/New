package com.o2dialer.app
import androidx.compose.ui.graphics.vector.ImageVector
import com.o2dialer.app.ui.components.O2Icons
data class WelcomeFeature(val title:Int,val description:Int,val icon:ImageVector)
data class WelcomePage(val badge:Int,val title:Int,val subtitle:Int,val features:List<WelcomeFeature>)
val ReferenceWelcomePages=listOf(
    WelcomePage(R.string.ref_welcome_0_badge,R.string.ref_welcome_0_title,R.string.ref_welcome_0_subtitle,listOf(WelcomeFeature(R.string.ref_welcome_0_0_title,R.string.ref_welcome_0_0_desc,O2Icons.Spreadsheet),WelcomeFeature(R.string.ref_welcome_0_1_title,R.string.ref_welcome_0_1_desc,O2Icons.Users),WelcomeFeature(R.string.ref_welcome_0_2_title,R.string.ref_welcome_0_2_desc,O2Icons.VerifiedUser))),
    WelcomePage(R.string.ref_welcome_1_badge,R.string.ref_welcome_1_title,R.string.ref_welcome_1_subtitle,listOf(WelcomeFeature(R.string.ref_welcome_1_0_title,R.string.ref_welcome_1_0_desc,O2Icons.Phone),WelcomeFeature(R.string.ref_welcome_1_1_title,R.string.ref_welcome_1_1_desc,O2Icons.Bolt),WelcomeFeature(R.string.ref_welcome_1_2_title,R.string.ref_welcome_1_2_desc,O2Icons.Sparkles))),
    WelcomePage(R.string.ref_welcome_2_badge,R.string.ref_welcome_2_title,R.string.ref_welcome_2_subtitle,listOf(WelcomeFeature(R.string.ref_welcome_2_0_title,R.string.ref_welcome_2_0_desc,O2Icons.Chart),WelcomeFeature(R.string.ref_welcome_2_1_title,R.string.ref_welcome_2_1_desc,O2Icons.Chat),WelcomeFeature(R.string.ref_welcome_2_2_title,R.string.ref_welcome_2_2_desc,O2Icons.Share)))
)
