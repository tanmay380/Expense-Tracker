package com.example.expensetracker.util

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "expense_tracker_prefs"
        private const val KEY_IMPORT_SPLASH_SEEN = "import_splash_seen"
        private const val KEY_FIRST_LAUNCH = "first_launch"
    }

    fun hasSeenImportSplash(): Boolean {
        return sharedPreferences.getBoolean(KEY_IMPORT_SPLASH_SEEN, false)
    }

    fun setImportSplashSeen(seen: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IMPORT_SPLASH_SEEN, seen).apply()
    }

    fun isFirstLaunch(): Boolean {
        val isFirst = !sharedPreferences.contains(KEY_FIRST_LAUNCH)
        if (isFirst) {
            sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
        }
        return isFirst
    }

    fun resetImportPreference() {
        sharedPreferences.edit().remove(KEY_IMPORT_SPLASH_SEEN).apply()
    }
}
