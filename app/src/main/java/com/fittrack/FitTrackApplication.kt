package com.fittrack

import android.app.Application
import com.fittrack.data.FitTrackDatabase
import com.fittrack.data.SessionManager

class FitTrackApplication : Application() {
    val database: FitTrackDatabase by lazy { FitTrackDatabase.getDatabase(this) }
    val sessionManager: SessionManager by lazy { SessionManager(this) }
}
