package com.anshyeon.onoff.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.anshyeon.onoff.R
import com.anshyeon.onoff.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController =
            supportFragmentManager.findFragmentById(R.id.cv_fragment)?.findNavController()
        navController?.let {
            setBottomNavigation(it)
        }
    }

    private fun setBottomNavigation(navController: NavController) {
        with(binding.bottomNavigation) {
            setupWithNavController(navController)
            setOnItemReselectedListener {}
            navController.addOnDestinationChangedListener { _, destination, _ ->
                if (destination.id == R.id.navigation_home) {
                    visibility = View.VISIBLE
                }
            }
        }
    }
}