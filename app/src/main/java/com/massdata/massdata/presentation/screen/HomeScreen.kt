package com.massdata.massdata.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.massdata.massdata.R
import com.massdata.massdata.data.DataStoreKeys
import com.massdata.massdata.data.DataStoreUtil
import com.massdata.massdata.network.LogInResponse
import com.massdata.massdata.presentation.viewModel.HomeViewModel
import com.massdata.massdata.ui.theme.large
import com.massdata.massdata.ui.theme.medium
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.androidx.compose.inject

private const val TAG = "HomeScreen"
class HomeScreen : AndroidScreen() {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel by inject<HomeViewModel>()
        val dataStore by inject<DataStore<Preferences>>()
        val coroutineScope = rememberCoroutineScope()

        // check for tokens
        // if expires, send to log in screen
        // else keep in this screen
        LaunchedEffect(key1 = true) {
            // show intro
            delay(500)

            // go to log in screen if previous token not found
            val pref = dataStore.data.firstOrNull()
            if (pref == null) {
                navigator.push(LogInScreen())
                return@LaunchedEffect
            } else {
                val token = pref[DataStoreKeys.TOKEN] ?: ""
                val refreshToken = pref[DataStoreKeys.REFRESH_TOKEN] ?: ""
                val tokenExpireTime = pref[DataStoreKeys.TOKEN_EXPIRE_TIME] ?: ""
                val refreshTokenExpireTime = pref[DataStoreKeys.REFRESH_TOKEN_EXPIRE_TIME] ?: ""

                if (
                    token.isBlank()
                    || refreshToken.isBlank()
                    || tokenExpireTime.isBlank()
                    || refreshTokenExpireTime.isBlank()
                ) {
                    navigator.push(LogInScreen())
                    return@LaunchedEffect
                }

                viewModel.validateCredentials(
                    token = token,
                    refreshToken = refreshToken,
                    onRefreshToken = {
                        coroutineScope.launch {
                            DataStoreUtil.saveData(dataStore, it)
                        }
                    },
                    onSuccess = {
                        viewModel.startExpireClock(tokenExpireTime)
                    },
                    onExpire = {
                        navigator.push(LogInScreen())
                    }
                )
            }
        }

        Scaffold(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to",
                    style = MaterialTheme.typography.h6
                )

                Text(
                    text = "Mass Data",
                    style = MaterialTheme.typography.h5,
                    color = MaterialTheme.colors.primary
                )

                Spacer(modifier = Modifier.height(large))
                AnimatedVisibility (viewModel.playerID.isNotBlank()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "User is currently Logged in.",
                            style = MaterialTheme.typography.body2
                        )

                        Text(
                            text = "player ID: ${viewModel.playerID}",
                            style = MaterialTheme.typography.caption
                        )

                        Spacer(modifier = Modifier.height(medium))
                        Text(
                            text = "token expire after",
                            style = MaterialTheme.typography.caption
                        )
                        Text(
                            text = viewModel.tokenTimeRemaining,
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.primary
                        )

                        Spacer(modifier = Modifier.height(large))
                        TextButton(onClick = {
                            coroutineScope.launch {
                                DataStoreUtil.saveData(dataStore, LogInResponse.getEmptyResponse())
                                navigator.push(LogInScreen())
                            }
                        }) {
                            Text(text = stringResource(id = R.string.sign_out))
                        }

                        Spacer(modifier = Modifier.height(large + large))
                        Button(
                            onClick = {
                                navigator.push(AddUserScreen())
                            }
                        ) {
                            Text(text = stringResource(id = R.string.add_user))
                        }
                    }
                }
            }
        }
    }


}