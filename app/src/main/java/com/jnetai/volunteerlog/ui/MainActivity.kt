package com.jnetai.volunteerlog.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.jnetai.volunteerlog.R
import com.jnetai.volunteerlog.VolunteerLogApp
import com.jnetai.volunteerlog.data.repository.VolunteerRepository
import com.jnetai.volunteerlog.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var repository: VolunteerRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as VolunteerLogApp
        repository = VolunteerRepository(
            app.database.volunteerEntryDao(),
            app.database.organisationDao(),
            app.database.goalDao(),
            app.database.reminderDao()
        )

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)
    }
}