package com.example.dogbreedsguessing.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dogbreedsguessing.ui.guessing.GuessingScreen
import com.example.dogbreedsguessing.ui.login.LoginScreen
import dagger.hilt.android.HiltAndroidApp
import kotlinx.serialization.Serializable

@Serializable
object LoginPage


@Serializable
object GuessingPage

@Composable
fun DogBreedsGuessingApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = LoginPage, modifier = modifier) {
        composable<LoginPage> {
            LoginScreen {
                navController.navigate(GuessingPage)
            }
        }
        composable<GuessingPage> { backStackEntry ->
            GuessingScreen()
        }

    }
}

@HiltAndroidApp
class DogBreedsGuessingApplication : Application()