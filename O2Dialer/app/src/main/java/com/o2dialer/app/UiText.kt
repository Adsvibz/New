package com.o2dialer.app
import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import java.util.Locale

/** Presentation-only resources follow the existing saved language setting. */
object UiText {
    private lateinit var app: Context
    private var language = -1
    private var localized: Context? = null
    fun init(context: Context) { app=context.applicationContext }
    fun get(@StringRes id:Int,vararg args:Any):String {
        check(::app.isInitialized)
        if(localized==null || language!=Store.cfg.lang) {
            language=Store.cfg.lang
            localized=app.createConfigurationContext(Configuration(app.resources.configuration).apply { setLocale(Locale.forLanguageTag(if(language==1) "ar" else "en")) })
        }
        return localized!!.resources.getString(id,*args)
    }
}
fun L(@StringRes id:Int,vararg args:Any):String=UiText.get(id,*args)
