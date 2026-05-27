package com.trixxwids.app

import android.app.Application
import com.trixxwids.app.data.AppDatabase

class MainApplication : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
    }
}
