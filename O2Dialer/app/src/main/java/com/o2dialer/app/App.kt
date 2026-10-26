package com.o2dialer.app

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Store.init(this)
        License.init(this)
    }
}
