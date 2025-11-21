package com.example.exam

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
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
        setupWaterReminders()
    }
    
    private fun setupWaterReminders() {
        com.example.exam.notification.NotificationHelper.scheduleWaterReminder(this, 2)
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
        
        loadNavHeaderData(navView)
    }
    
    private fun loadNavHeaderData(navView: NavigationView) {
        val headerView = navView.getHeaderView(0)
        val tvUsername = headerView.findViewById<android.widget.TextView>(R.id.tv_nav_username)
        val tvEmail = headerView.findViewById<android.widget.TextView>(R.id.tv_nav_email)
        val tvGoal = headerView.findViewById<android.widget.TextView>(R.id.tv_nav_goal)
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val user = database.userDao().getUserById(currentUserId)
                user?.let {
                    tvUsername.text = it.username
                    tvEmail.text = it.email
                    tvGoal.text = "${String.format("%.1f", it.dailyGoal)}L Goal"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
                    replaceFragment(com.example.exam.ui.notifications.NotificationsFragment())
                    true
                }
                R.id.nav_settings -> {
                    replaceFragment(com.example.exam.ui.account.AccountFragment())
                    true
                }
                else -> false
            }
        }
        showNotificationBadge()
    }
    
    private fun showNotificationBadge() {
        val badge = bottomNavigation.getOrCreateBadge(R.id.nav_notifications)
        badge.isVisible = true
        badge.number = 5
        badge.backgroundColor = android.graphics.Color.parseColor("#FF0000")
        badge.badgeTextColor = android.graphics.Color.WHITE
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
            R.id.nav_dashboard -> {
                replaceFragment(DashboardFragment())
                bottomNavigation.selectedItemId = R.id.nav_dashboard
            }
            R.id.nav_goals -> {
                replaceFragment(com.example.exam.ui.goals.GoalsFragment())
            }
            R.id.nav_profile -> {
                replaceFragment(com.example.exam.ui.account.AccountFragment())
                bottomNavigation.selectedItemId = R.id.nav_settings
            }
            R.id.nav_language -> {
                showLanguageDialog()
            }
            R.id.nav_notifications -> {
                Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show()
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
        } else if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    private fun showLanguageDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_language, null)
        bottomSheetDialog.setContentView(view)

        val radioGroup = view.findViewById<RadioGroup>(R.id.language_radio_group)
        val radioArabic = view.findViewById<RadioButton>(R.id.radio_arabic)
        val radioFrench = view.findViewById<RadioButton>(R.id.radio_french)
        val radioEnglish = view.findViewById<RadioButton>(R.id.radio_english)

        when (getCurrentLanguage()) {
            "ar" -> radioArabic.isChecked = true
            "fr" -> radioFrench.isChecked = true
            "en" -> radioEnglish.isChecked = true
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_arabic -> setLanguage("ar")
                R.id.radio_french -> setLanguage("fr")
                R.id.radio_english -> setLanguage("en")
            }
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
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
