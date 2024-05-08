package com.maincode.myfriend

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.maincode.myfriend.data.MainViewModel
import com.maincode.myfriend.data.MessageDatabase
import com.maincode.myfriend.navigation.MultiTurnMode
import com.maincode.myfriend.navigation.MyNavigation
import com.maincode.myfriend.navigation.SetApi
import com.maincode.myfriend.ui.theme.MyFriendTheme
import com.maincode.util.datastore
import com.maincode.util.getApiKey
import kotlinx.coroutines.runBlocking

@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(applicationContext, MessageDatabase::class.java, "message.db").build()
    }

    private val viewModel by viewModels<MainViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(db.dao) as T
                }
            }
        }
    )

    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val handler = Handler(mainLooper)
        handler.postDelayed({
            setContent {
                MyFriendTheme {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        if (runBlocking {
                                val key = applicationContext.datastore.getApiKey()
                                if (key.isNotEmpty()) {
                                    viewModel.updateApiKey(key)
                                }
                                key.isEmpty()
                            }) {
                            MyNavigation(viewModel = viewModel, startDestination = SetApi.route)
                        } else {
                            MyNavigation(viewModel = viewModel, startDestination = MultiTurnMode.route)
                        }
                    }
                }
            }
        }, 2000) // Delay for 2 seconds
    }
}



enum class ApiType {
    SINGLE_CHAT,
    MULTI_CHAT,
    IMAGE_CHAT
}

enum class Mode {
    USER,
    GEMINI
}