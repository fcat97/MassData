package com.massdata.massdata.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.ScaleTransition
import com.massdata.massdata.presentation.screen.HomeScreen
import com.massdata.massdata.ui.theme.MassDataTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MassDataTheme {
                Navigator(screen = HomeScreen()) {
                    ScaleTransition(navigator = it)
                }
            }
        }
    }
}