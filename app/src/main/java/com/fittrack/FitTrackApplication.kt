package com.fittrack

import android.app.Application
import com.fittrack.data.FitTrackDatabase

class FitTrackApplication : Application() {
    val database: FitTrackDatabase by lazy { FitTrackDatabase.getDatabase(this) }
}
