package com.fittrack.data

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("fittrack_session", Context.MODE_PRIVATE)

    fun saveUserId(userId: Int) = prefs.edit().putInt(KEY_USER_ID, userId).apply()
    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)
    fun clearSession() = prefs.edit().remove(KEY_USER_ID).apply()
    fun hasSession(): Boolean = getUserId() != -1

    companion object {
        private const val KEY_USER_ID = "user_id"
    }
}
