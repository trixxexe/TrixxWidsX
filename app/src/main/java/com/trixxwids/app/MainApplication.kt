package com.trixxwids.app

import android.app.Application
import android.content.SharedPreferences
import com.trixxwids.app.data.AppDatabase
import java.io.StringWriter
import java.io.PrintWriter

class MainApplication : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val prefs = getSharedPreferences("crash_prefs", MODE_PRIVATE)
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            throwable.printStackTrace(pw)
            pw.flush()
            prefs.edit().putString("last_crash", sw.toString()).commit()
            android.os.Process.killProcess(android.os.Process.myPid())
        }
        database = AppDatabase.getInstance(this)
    }
}
