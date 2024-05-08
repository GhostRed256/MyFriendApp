package com.maincode.myfriend.navigation

import android.webkit.WebView
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.maincode.myfriend.data.MainViewModel
import com.maincode.myfriend.screens.AboutScreen
import com.maincode.myfriend.screens.SingleTurnScreen
import com.maincode.myfriend.screens.ImageChatScreen
import com.maincode.myfriend.screens.MultiTurnScreen
import com.maincode.myfriend.screens.SetApiScreen
import com.maincode.myfriend.screens.SettingsScreen


@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@Composable
fun MyNavigation(
    viewModel: MainViewModel,
    startDestination: String = SingleTurnMode.route
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination)
    {
        composable(MultiTurnMode.route) {
            MultiTurnScreen(viewModel, navController)
        }
        composable(ImageMode.route) {
            ImageChatScreen(viewModel, navController)
        }

        composable(SingleTurnMode.route)
        {
            SingleTurnScreen(viewModel, navController)
        }
        composable(Settings.route) {
            SettingsScreen(navController)
        }
        composable(SetApi.route) {
            SetApiScreen(viewModel, navController)
        }
        composable(About.route) {
            AboutScreen(navController)
        }
    }
}