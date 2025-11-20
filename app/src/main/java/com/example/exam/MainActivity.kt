package com.example.exam

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.example.exam.data.database.AppDatabase
import com.example.exam.ui.dashboard.DashboardFragment
import com.example.exam.ui.auth.AuthActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var database: AppDatabase
    private var currentUserId: Long = 0
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = (application as WaterlyApp).database
        val prefs = getSharedPreferences("waterly_prefs", MODE_PRIVATE)
        currentUserId = prefs.getLong("current_user_id", 0)

        if (currentUserId == 0L) {
            finish()
            return
        }

        setupToolbar()
        setupDrawer()
        setupBottomNavigation()
        loadInitialFragment()
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    replaceFragment(DashboardFragment())
                    true
                }
                R.id.nav_notifications -> {
                    Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_settings -> {
                    Toast.makeText(this, "Paramètres", Toast.LENGTH_SHORT).show()
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                Toast.makeText(this, "Mon profile", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_language -> {
                showLanguageDialog()
            }
            R.id.nav_logout -> {
                logout()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
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

    private fun logout() {
        AlertDialog.Builder(this)
            .setTitle("Déconnexion")
            .setMessage("Voulez-vous vraiment vous déconnecter ?")
            .setPositiveButton("Oui") { _, _ ->
                val prefs = getSharedPreferences("waterly_prefs", MODE_PRIVATE)
                prefs.edit().remove("current_user_id").apply()
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            }
            .setNegativeButton("Non", null)
            .show()
    }
}
