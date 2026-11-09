package com.o2dialer.app

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class UpdateManifestTest {
    private fun feed() = JSONObject().put("schema", 1).put("packageName", "com.o2dialer.app")
        .put("versionCode", 200).put("versionName", "7.0").put("minSdk", 26)
        .put("size", 1500000).put("sha256", "a".repeat(64))
        .put("apkUrl", "https://github.com/Adsvibz/New/releases/download/o2dialer-build-200/O2Dialer-7.0-release.apk")

    private fun rejects(o: JSONObject) {
        assertThrows(Exception::class.java) { UpdateManifest.parse(o.toString()) }
    }

    @Test fun onlyNewerCompatibleVersionsAreOffered() {
        val update = UpdateManifest.parse(feed().toString())
        assertTrue(update.appliesTo(199, 26))
        assertFalse(update.appliesTo(200, 36))
        assertFalse(update.appliesTo(201, 36))
        assertFalse(update.appliesTo(199, 25))
    }

    @Test fun rejectsForeignInsecureAndMismatchedDownloads() {
        rejects(feed().put("apkUrl", "https://evil.example/update.apk"))
        rejects(feed().put("apkUrl", "http://github.com/Adsvibz/New/releases/download/o2dialer-build-200/O2Dialer-7.0-release.apk"))
        rejects(feed().put("versionCode", 201))
        rejects(feed().put("packageName", "other.app"))
    }

    @Test fun requiresCompleteBoundedMetadata() {
        rejects(feed().put("sha256", "bad"))
        rejects(feed().put("size", 0))
        rejects(feed().put("size", UpdateManifest.MAX_APK_BYTES + 1))
        rejects(feed().put("schema", 2))
        rejects(feed().put("minSdk", 0))
        rejects(feed().put("versionName", "<unexpected>"))
        val missing = feed(); missing.remove("sha256"); rejects(missing)
        assertThrows(Exception::class.java) { UpdateManifest.parse(" ".repeat(65537)) }
    }

    @Test fun requiresExactNonemptySigningIdentity() {
        assertTrue(matchingUpdateSigners(setOf("original"), setOf("original")))
        assertFalse(matchingUpdateSigners(emptySet(), emptySet()))
        assertFalse(matchingUpdateSigners(setOf("original"), setOf("other")))
        assertFalse(matchingUpdateSigners(setOf("original"), setOf("original", "other")))
    }
}
