package ru.molluk.git_authorization.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import ru.molluk.git_authorization.R
import ru.molluk.git_authorization.databinding.ActivityMainBinding
import ru.molluk.git_authorization.utils.observeNetworkStatusAndShowSnackbar

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var networkStatusSource: LiveData<Boolean>

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val myApplication = application as ru.molluk.git_authorization.ui.application.Application
        networkStatusSource = myApplication.networkMonitor

        observeNetworkStatusAndShowSnackbar(networkStatusSource, binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}