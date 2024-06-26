package com.maincode.myfriend.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import coil.ImageLoader
import coil.request.ImageRequest
import com.maincode.myfriend.ApiType
import com.maincode.myfriend.components.ConversationArea
import com.maincode.myfriend.components.SelectedImageArea
import com.maincode.myfriend.components.TypingArea
import com.maincode.myfriend.data.MainViewModel
import com.maincode.myfriend.navigation.DrawerNav
import com.maincode.myfriend.navigation.MainTopBar
import com.maincode.myfriend.navigation.items
import com.maincode.util.ImageHelper
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ImageChatScreen(viewModel: MainViewModel, navController: NavHostController) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var selectedItemIndex by rememberSaveable { mutableIntStateOf(0) }

    val currentRoute = navController.currentBackStackEntry?.destination?.route ?: ""
    viewModel.makeHomeVisit()

    selectedItemIndex = items.indexOfFirst { it.title == currentRoute }

    val bitmaps: SnapshotStateList<Bitmap> = remember {
        mutableStateListOf()
    }
    val context = LocalContext.current

    val imageRequestBuilder = ImageRequest.Builder(context)
    val imageLoader = ImageLoader.Builder(context).build()

    val coroutineScope = rememberCoroutineScope()

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) {
        if (it != null) {
            bitmaps.add(it)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
    ) {
        it.forEach { uri ->
            coroutineScope.launch {
                ImageHelper.scaleDownBitmap(uri, imageRequestBuilder, imageLoader)?.let { bitmap ->
                    bitmaps.add(bitmap)
                }
            }
        }
    }
    ModalNavigationDrawer(
        drawerContent = {
            DrawerNav(
                selectedItemIndex = selectedItemIndex,
                onItemSelect = {selectedItemIndex = it},
                onCloseDrawer = {scope.launch { drawerState.close() }},
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
                    modifier = Modifier
                        .weight(1f)
                ) {
                    ConversationArea(viewModel, apiType = ApiType.IMAGE_CHAT)
                }
                SelectedImageArea(bitmaps = bitmaps)
                TypingArea(
                    viewModel = viewModel,
                    apiType = ApiType.IMAGE_CHAT,
                    bitmaps = bitmaps,
                    galleryLauncher = galleryLauncher,
                    permissionLauncher = permissionLauncher
                )
            }
        }
    }
}
