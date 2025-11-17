package com.example.exam

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.waterly.data.database.AppDatabase
import com.waterly.ui.dashboard.DashboardFragment
import com.waterly.ui.goals.GoalsFragment
import com.waterly.ui.tips.TipsFragment
import com.waterly.ui.account.AccountFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private var currentUserId: Long = 0

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var fragmentContainerId: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // XML layout reference

        // Initialize views using findViewById
        bottomNavigation = findViewById(R.id.bottom_navigation)
        fragmentContainerId = findViewById(R.id.fragment_container)

        // Initialize database
        database = (application as WaterlyApp).database

        // Get current user ID from shared preferences
        val prefs = getSharedPreferences("waterly_prefs", MODE_PRIVATE)
        currentUserId = prefs.getLong("current_user_id", 0)

        if (currentUserId == 0L) {
            // No user logged in, redirect to auth
            finish()
            return
        }

        setupToolbar()
        setupBottomNavigation()
        loadInitialFragment()
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Tableau de bord"
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    replaceFragment(DashboardFragment())
                    supportActionBar?.title = "Tableau de bord"
                    true
                }
                R.id.nav_goals -> {
                    replaceFragment(GoalsFragment())
                    supportActionBar?.title = "Objectifs"
                    true
                }
                R.id.nav_tips -> {
                    replaceFragment(TipsFragment())
                    supportActionBar?.title = "Conseils d'hydratation"
                    true
                }
                R.id.nav_account -> {
                    replaceFragment(AccountFragment())
                    supportActionBar?.title = "Mon compte"
                    true
                }
                else -> false
            }
        }
    }

    private fun loadInitialFragment() {
        replaceFragment(DashboardFragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_language -> {
                showLanguageDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("Français", "English", "العربية")
        val currentLanguage = getCurrentLanguage()

        AlertDialog.Builder(this)
            .setTitle("Choisir la langue")
            .setSingleChoiceItems(languages, getLanguageIndex(currentLanguage)) { dialog, which ->
                when (which) {
                    0 -> setLanguage("fr")
                    1 -> setLanguage("en")
                    2 -> setLanguage("ar")
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun getCurrentLanguage(): String {
        val prefs = getSharedPreferences("waterly_prefs", MODE_PRIVATE)
        return prefs.getString("language", "fr") ?: "fr"
    }

    private fun getLanguageIndex(language: String): Int {
        return when (language) {
            "fr" -> 0
            "en" -> 1
            "ar" -> 2
            else -> 0
        }
    }

    private fun setLanguage(language: String) {
        val prefs = getSharedPreferences("waterly_prefs", MODE_PRIVATE)
        prefs.edit().putString("language", language).apply()

        CoroutineScope(Dispatchers.IO).launch {
            database.userDao().updateLanguage(currentUserId, language)
        }

        Toast.makeText(this, "Langue changée", Toast.LENGTH_SHORT).show()

        recreate()
    }
}
