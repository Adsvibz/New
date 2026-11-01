package com.o2dialer.app.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.o2dialer.app.R

data class ReferenceAccent(val id: String, val color: Color, val label: Int)

/** Presentation preferences are isolated from existing campaign/config serialization. */
object ReferenceAppearance {
    val colors = listOf(
        ReferenceAccent("sunset", Color(0xFFE2532B), R.string.ref_color_sunset),
        ReferenceAccent("royal", Color(0xFF2563EB), R.string.ref_color_royal),
        ReferenceAccent("emerald", Color(0xFF059669), R.string.ref_color_emerald),
        ReferenceAccent("violet", Color(0xFF7C3AED), R.string.ref_color_violet),
        ReferenceAccent("crimson", Color(0xFFE11D48), R.string.ref_color_crimson),
        ReferenceAccent("teal", Color(0xFF0D9488), R.string.ref_color_teal),
        ReferenceAccent("indigo", Color(0xFF4F46E5), R.string.ref_color_indigo),
        ReferenceAccent("amber", Color(0xFFD97706), R.string.ref_color_amber),
        ReferenceAccent("fuchsia", Color(0xFFC026D3), R.string.ref_color_fuchsia),
        ReferenceAccent("slate", Color(0xFF334155), R.string.ref_color_slate)
    )
    private var preferences: SharedPreferences? = null
    var colorId by mutableStateOf("sunset")
        private set
    var deviceFrame by mutableStateOf(true)
        private set
    val selected: ReferenceAccent get() = colors.firstOrNull { it.id == colorId } ?: colors.first()

    fun init(context: Context) {
        if (preferences != null) return
        preferences = context.applicationContext.getSharedPreferences("o2_reference_appearance", Context.MODE_PRIVATE)
        colorId = preferences!!.getString("accent", "sunset") ?: "sunset"
        deviceFrame = preferences!!.getBoolean("deviceFrame", true)
    }

    fun select(id: String) {
        require(colors.any { it.id == id })
        colorId = id
        preferences?.edit()?.putString("accent", id)?.apply()
    }

    fun toggleDeviceFrame() {
        deviceFrame = !deviceFrame
        preferences?.edit()?.putBoolean("deviceFrame", deviceFrame)?.apply()
    }
}
