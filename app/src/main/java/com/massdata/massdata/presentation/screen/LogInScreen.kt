package com.massdata.massdata.presentation.screen

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Email
import androidx.compose.material.icons.twotone.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.massdata.massdata.R
import com.massdata.massdata.data.DataStoreUtil
import com.massdata.massdata.presentation.event.Failed
import com.massdata.massdata.presentation.event.None
import com.massdata.massdata.presentation.event.Success
import com.massdata.massdata.presentation.viewModel.LogInViewModel
import com.massdata.massdata.ui.theme.large
import com.massdata.massdata.ui.theme.medium
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.inject

class LogInScreen : AndroidScreen() {

    @Composable
    override fun Content() {
        val scaffoldState = rememberScaffoldState()
        val focusManager = LocalFocusManager.current
        val viewModel by inject<LogInViewModel>()
        val snackBarHostState = remember { SnackbarHostState() }
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()
        val dataStore by inject<DataStore<Preferences>>()
        val activity = LocalContext.current as? Activity

        val passwordVisibilityIcon = if (viewModel.showPassword) {
            painterResource(id = R.drawable.ic_twotone_visibility_24)
        } else {
            painterResource(id = R.drawable.ic_twotone_visibility_off_24)
        }

        // close app
        BackHandler(enabled = true) {
            activity?.finishAffinity()
        }

        // show snack bar
        LaunchedEffect(key1 = viewModel.uiEvent) {
            when(viewModel.uiEvent) {
                is Success -> {
                    snackBarHostState.showSnackbar((viewModel.uiEvent as Success).msg)

                    viewModel.uiEvent = None
                }

                is Failed -> {
                    snackBarHostState.showSnackbar((viewModel.uiEvent as Failed).msg)

                    viewModel.uiEvent = None
                }

                else -> {
                    // do nothing
                }
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            scaffoldState = scaffoldState,
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // email
                    TextField(
                        value = viewModel.email,
                        onValueChange = { viewModel.email = it },
                        label = { Text(text = stringResource(id = R.string.email_address)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.TwoTone.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colors.onSurface
                            )
                        },
                        isError = ! viewModel.isEmailValid,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(medium),
                    )

                    // password
                    TextField(
                        value = viewModel.password,
                        onValueChange = { viewModel.password = it },
                        label = { Text(text = stringResource(id = R.string.password)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.TwoTone.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colors.onSurface
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.showPassword = ! viewModel.showPassword }) {
                                Icon(
                                    painter = passwordVisibilityIcon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colors.onSurface
                                )
                            }
                        },
                        visualTransformation = if (viewModel.showPassword) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation(mask = '*')
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                        }),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(medium),
                    )

                    // log in button
                    Button(
                        onClick = {
                            viewModel.onLogInButtonClicked(
                                saveToken = { response ->
                                    coroutineScope.launch {
                                        DataStoreUtil.saveData(dataStore, response)
                                    }
                                },
                                onSuccess = {
                                    coroutineScope.launch {
                                        delay(1000)
                                        navigator.pop()
                                    }
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(0.6f),
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(id = R.string.log_in),
                                modifier = Modifier.align(Alignment.Center)
                            )

                            if (viewModel.taskRunning) CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .size(20.dp),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}