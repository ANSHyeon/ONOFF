package com.anshyeon.onoff.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.anshyeon.onoff.R
import com.anshyeon.onoff.databinding.ActivityMainBinding
import com.anshyeon.onoff.util.NetworkConnection
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
            setNetworkErrorBar()
        }
    }

    private fun setBottomNavigation(navController: NavController) {
        with(binding.bottomNavigation) {
            val mainDestinations = arrayOf(
                R.id.navigation_home,
                R.id.navigation_chat,
                R.id.navigation_board,
                R.id.navigation_user
            )
            setupWithNavController(navController)
            setOnItemReselectedListener {}
            navController.addOnDestinationChangedListener { _, destination, _ ->
                visibility = if (destination.id in mainDestinations) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }

    private fun setNetworkErrorBar() {
        NetworkConnection(applicationContext).observe(this) {
            binding.networkErrorBar.visibility = if (it) View.GONE else View.VISIBLE
        }
    }
}