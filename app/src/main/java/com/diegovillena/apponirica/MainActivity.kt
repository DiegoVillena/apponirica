package com.diegovillena.apponirica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.diegovillena.apponirica.ui.AppNavHost
import com.diegovillena.apponirica.ui.theme.AppOniricaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppOniricaTheme {
                AppNavHost()
            }
        }
    }
}