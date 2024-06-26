package com.maincode.myfriend.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.maincode.myfriend.ApiType
import com.maincode.myfriend.components.ConversationArea
import com.maincode.myfriend.components.TypingArea
import com.maincode.myfriend.data.MainViewModel
import com.maincode.myfriend.navigation.DrawerNav
import com.maincode.myfriend.navigation.MainTopBar
import com.maincode.myfriend.navigation.items
import kotlinx.coroutines.launch

@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SingleTurnScreen(
    viewModel: MainViewModel,
    navController: NavHostController
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }

    val currentRoute = navController.currentBackStackEntry?.destination?.route ?: ""
    viewModel.makeHomeVisit()

    selectedItemIndex = items.indexOfFirst { it.title == currentRoute }


    ModalNavigationDrawer(
        drawerContent = {
            DrawerNav(
                selectedItemIndex = selectedItemIndex,
                onItemSelect = { selectedItemIndex = it },
                onCloseDrawer = { scope.launch { drawerState.close() } },
                navController = navController
            )
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = { MainTopBar(scope, drawerState) }
        ) {
            Column(
                modifier = Modifier
                    .padding(top = it.calculateTopPadding())
                    .fillMaxSize()
                    .fillMaxHeight(1f)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    ConversationArea(viewModel, apiType = ApiType.SINGLE_CHAT)
                }
                TypingArea(
                    viewModel = viewModel,
                    apiType = ApiType.SINGLE_CHAT,
                    bitmaps = null,
                    galleryLauncher = null,
                    permissionLauncher = null
                )
            }
        }
    }
}

