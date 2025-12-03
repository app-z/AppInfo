package com.drweb.appinfo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.drweb.appinfo.core.di.appModule
import com.drweb.appinfo.presentation.navigation.Screen
import com.drweb.appinfo.presentation.navigation.setupNavigation
import com.drweb.appinfo.ui.theme.AppInfoDrWebTheme
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KoinApplication(application = {
                androidContext(this@MainActivity)
                modules(appModule)
            }) {
                AppInfoDrWebTheme {
                    AppInfoViewerApp()
                }
            }
        }
    }
}

@Composable
fun AppInfoViewerApp() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = Screen.AppList.route,
            modifier = Modifier.fillMaxSize()
        ) {
            setupNavigation(navController)
        }
    }
}
