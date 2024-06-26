package com.maincode.myfriend.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.maincode.myfriend.R
import com.maincode.myfriend.data.MainViewModel
import com.maincode.myfriend.navigation.MultiTurnMode
import com.maincode.myfriend.navigation.SetApi
import com.maincode.myfriend.navigation.TopBar
import com.maincode.myfriend.ui.theme.DecentBlue
import com.maincode.myfriend.ui.theme.DecentGreen
import com.maincode.myfriend.ui.theme.DecentRed
import com.maincode.util.datastore
import com.maincode.util.getApiKey
import kotlinx.coroutines.runBlocking

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SetApiScreen(
    viewModel: MainViewModel,
    navController: NavHostController
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val validationState =
        viewModel.validationState.observeAsState().value
    val context = LocalContext.current

    var text by remember { mutableStateOf(TextFieldValue(runBlocking { context.datastore.getApiKey() })) }
    Scaffold(
        topBar = {
            TopBar(
                name = stringResource(id = R.string.set_api),
                navController = navController,
                showNavigationIcon = viewModel.isHomeVisit.value
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                singleLine = true,
                value = text,
                onValueChange = { newText ->
                    text = newText
                    viewModel.resetValidationState()
                },
                placeholder = {
                    Text(
                        color = MaterialTheme.colorScheme.inversePrimary,
                        text = "Enter your api key"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(10.dp),
                shape = RoundedCornerShape(28),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.LightGray,
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.W500,
                    fontSize = 18.sp
                )
            )

            Button(
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    if (validationState == MainViewModel.ValidationState.Valid) {
                        if (viewModel.isHomeVisit.value == true) {
                            navController.navigateUp()
                        } else {
                            navController.popBackStack(SetApi.route, true)
                            navController.navigate(MultiTurnMode.route)
                        }
                    } else if (validationState == MainViewModel.ValidationState.Idle) {
                        viewModel.validate(context, text.text)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (validationState) {
                        MainViewModel.ValidationState.Checking -> Color.DarkGray
                        MainViewModel.ValidationState.Idle -> DecentBlue
                        MainViewModel.ValidationState.Invalid -> DecentRed
                        MainViewModel.ValidationState.Valid -> DecentGreen
                        null -> DecentBlue
                    },
                    contentColor = Color.White
                )
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = when (validationState) {
                        MainViewModel.ValidationState.Checking -> "Validating..."
                        MainViewModel.ValidationState.Idle -> "Validate"
                        MainViewModel.ValidationState.Invalid -> "Invalid Key"
                        MainViewModel.ValidationState.Valid -> "Continue"
                        else -> "NULL"
                    },
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.padding(30.dp))

            ApiSetupHelper()

            if (viewModel.isHomeVisit.value != true) {
                Spacer(modifier = Modifier.padding(10.dp))
                DemoApiButton(viewModel, navController)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DemoApiButton(viewModel: MainViewModel, navController: NavHostController) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val demoApiState =
        viewModel.demoApiState.observeAsState().value
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            color = MaterialTheme.colorScheme.primary,
            text = "or,",
            style = MaterialTheme.typography.titleMedium,
        )
        Button(
            onClick = {
                keyboardController?.hide()
                focusManager.clearFocus()
                if (demoApiState == MainViewModel.ValidationState.Valid) {
                    if (viewModel.isHomeVisit.value != true) {
                        navController.popBackStack(SetApi.route, true)
                        navController.navigate(MultiTurnMode.route)
                    }
                } else if (demoApiState == MainViewModel.ValidationState.Idle ||
                    demoApiState == MainViewModel.ValidationState.Invalid
                ) {
                    viewModel.makeDemoApiRequest(context)
                }
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = when (demoApiState) {
                    MainViewModel.ValidationState.Checking -> Color.DarkGray
                    MainViewModel.ValidationState.Idle -> Color(0xFFE29627)
                    MainViewModel.ValidationState.Invalid -> DecentRed
                    MainViewModel.ValidationState.Valid -> DecentGreen
                    null -> DecentBlue
                },
                contentColor = Color.Black
            )
        ) {
            Text(
                color = Color.White,
                modifier = Modifier.padding(8.dp),
                text = when (demoApiState) {
                    MainViewModel.ValidationState.Checking -> "Fetching..."
                    MainViewModel.ValidationState.Idle -> "Use Demo API"
                    MainViewModel.ValidationState.Invalid -> "Try again"
                    MainViewModel.ValidationState.Valid -> "Continue"
                    else -> "NULL"
                },
                fontSize = 15.sp
            )
        }
        Text(
            text = stringResource(R.string.testing_purpose),
            style = MaterialTheme.typography.titleSmall,
        )
    }
}


@Composable
fun ApiSetupHelper() {
    val uriHandler = LocalUriHandler.current

    val apiSetup = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary
            )
        ) {
            append("Learn how to set up your own API key. ")
        }
        pushStringAnnotation(
            tag = "click",
            annotation = stringResource(id = R.string.api_setup_link)
        )
        withStyle(
            style = SpanStyle(
                color = Color(0xFF267BC4),
                textDecoration = TextDecoration.Underline
            )
        ) {
            append("Click here")
        }
        pop()
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary
            )
        ) {
            append(" for details.")
        }
    }

    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 10.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Icon(
            modifier = Modifier.size(50.dp),
            painter = painterResource(id = R.drawable.about_icon),
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = "help"
        )
        ClickableText(
            modifier = Modifier.padding(10.dp),
            text = apiSetup,
            style = MaterialTheme.typography.titleMedium,
            onClick = { offset ->
                apiSetup.getStringAnnotations(
                    tag = "click",
                    start = offset,
                    end = offset
                ).firstOrNull()?.let {
                    uriHandler.openUri(it.item)
                }
            }
        )
    }
}

