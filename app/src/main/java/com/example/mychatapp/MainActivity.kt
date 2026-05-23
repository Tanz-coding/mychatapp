package com.example.mychatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mychatapp.data.remote.ApiClient
import com.example.mychatapp.data.remote.ApiConfig
import com.example.mychatapp.ui.AppNavigation
import com.example.mychatapp.ui.AppViewModel
import com.example.mychatapp.ui.theme.MychatappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(applicationContext, ApiConfig.getBaseUrl(applicationContext))
        enableEdgeToEdge()
        setContent {
            MychatappTheme {
                val appViewModel: AppViewModel = viewModel()
                AppNavigation(viewModel = appViewModel)
            }
        }
    }
}
