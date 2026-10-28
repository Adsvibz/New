package com.o2dialer.app

import org.json.JSONObject

/** Small, strictly validated feed. Never accept an arbitrary APK URL from remote JSON. */
data class UpdateManifest(
    val versionCode: Int,
    val versionName: String,
    val minSdk: Int,
    val size: Long,
    val sha256: String,
    val apkUrl: String
) {
    fun appliesTo(installed: Int, sdk: Int) = versionCode > installed && minSdk <= sdk

    companion object {
        const val MAX_APK_BYTES = 100L * 1024 * 1024
        const val FEED_URL = "https://github.com/Adsvibz/New/releases/latest/download/update.json"

        fun parse(json: String): UpdateManifest {
            require(json.length <= 65536)
            val o = JSONObject(json)
            require(o.getInt("schema") == 1)
            require(o.getString("packageName") == "com.o2dialer.app")
            val code = o.getInt("versionCode")
            val name = o.getString("versionName")
            val sdk = o.getInt("minSdk")
            val size = o.getLong("size")
            val hash = o.getString("sha256")
            val url = o.getString("apkUrl")
            require(code > 0 && name.matches(Regex("[0-9A-Za-z.+_-]{1,32}")))
            require(sdk in 26..100 && size in 1..MAX_APK_BYTES)
            require(hash.matches(Regex("[a-f0-9]{64}")))
            require(url == "https://github.com/Adsvibz/New/releases/download/o2dialer-build-$code/O2Dialer-7.0-release.apk")
            return UpdateManifest(code, name, sdk, size, hash, url)
        }
    }
}

internal fun matchingUpdateSigners(installed: Set<String>, downloaded: Set<String>): Boolean =
    installed.isNotEmpty() && installed == downloaded
