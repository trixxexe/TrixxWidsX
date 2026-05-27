package com.trixxwids.app.ui

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.gson.Gson
import com.trixxwids.app.R
import com.trixxwids.app.data.WidgetConfig
import com.trixxwids.app.data.WidgetEntity
import com.trixxwids.app.databinding.ActivityMainBinding
import com.trixxwids.app.ui.onboarding.OnboardingActivity
import com.trixxwids.app.viewmodel.EditorViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var navController: NavController? = null
    private lateinit var editorViewModel: EditorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            checkPreviousCrash()

            editorViewModel = ViewModelProvider(this).get(EditorViewModel::class.java)

            checkFirstLaunch()

            binding.toolbar.title = getString(R.string.app_name)
            setSupportActionBar(binding.toolbar)

            binding.root.post {
                setupNavigation()
            }
        } catch (e: Throwable) {
            try {
                AlertDialog.Builder(this)
                    .setTitle("Launch Error")
                    .setMessage("${e::class.java.simpleName}: ${e.message}")
                    .setPositiveButton("Exit") { _, _ -> finish() }
                    .setCancelable(false)
                    .show()
            } catch (_: Throwable) {
                finish()
            }
        }
    }

    private fun setupNavigation() {
        try {
            supportFragmentManager.executePendingTransactions()
            val host = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            if (host is NavHostFragment) {
                navController = host.navController
                if (navController != null) {
                    binding.bottomNav.setupWithNavController(navController!!)
                    binding.bottomNav.setOnItemSelectedListener { item ->
                        navController?.navigate(item.itemId)
                        updateToolbarTitle(item)
                        true
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkPreviousCrash() {
        val prefs = getSharedPreferences("crash_prefs", Context.MODE_PRIVATE)
        val crash = prefs.getString("last_crash", null)
        if (crash != null) {
            prefs.edit().remove("last_crash").apply()
            AlertDialog.Builder(this)
                .setTitle("Previous Crash Detected")
                .setMessage(crash.take(500))
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun checkFirstLaunch() {
        val prefs = getSharedPreferences("trixxwids_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("onboarding_complete", false)) {
            startActivity(android.content.Intent(this, OnboardingActivity::class.java))
        }
    }

    private fun updateToolbarTitle(item: MenuItem) {
        binding.toolbar.title = item.title
    }

    fun navigateToEditorWithWidget(widget: WidgetEntity) {
        navController?.navigate(R.id.editor_fragment)

        try {
            val config = Gson().fromJson(widget.configJson, WidgetConfig::class.java)
            editorViewModel.widgetToLoad.value = config
        } catch (e: Exception) {
            com.google.android.material.snackbar.Snackbar.make(
                binding.root, "Failed to load widget", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
            ).show()
        }
    }
}