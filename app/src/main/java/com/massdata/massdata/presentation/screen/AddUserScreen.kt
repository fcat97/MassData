package com.massdata.massdata.presentation.screen

import android.util.Log
import android.widget.Space
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.massdata.massdata.R
import com.massdata.massdata.data.DataStoreKeys
import com.massdata.massdata.data.toLocalDateTime
import com.massdata.massdata.presentation.viewModel.SignUpViewModel
import com.massdata.massdata.ui.theme.large
import com.massdata.massdata.ui.theme.medium
import kotlinx.coroutines.flow.firstOrNull
import org.koin.androidx.compose.inject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

private const val TAG = "AddUserScreen"
class AddUserScreen: AndroidScreen() {
    @Composable
    override fun Content() {
        val scaffoldState = rememberScaffoldState()
        val focusManager = LocalFocusManager.current
        val viewModel by inject<SignUpViewModel>()
        val dataStore by inject<DataStore<Preferences>>()
        val navigator = LocalNavigator.currentOrThrow

        val passwordVisibilityIcon = if (viewModel.showPassword) {
            painterResource(id = R.drawable.ic_twotone_visibility_24)
        } else {
            painterResource(id = R.drawable.ic_twotone_visibility_off_24)
        }

        var token by remember { mutableStateOf("") }
        var tokenExpireTime by remember { mutableStateOf("") }

        // check if user is valid
        LaunchedEffect(key1 = true) {
            val pref = dataStore.data.firstOrNull()
            if (pref == null) {
                navigator.pop()
                return@LaunchedEffect
            } else {
                token = pref[DataStoreKeys.TOKEN] ?: ""
                tokenExpireTime = pref[DataStoreKeys.TOKEN_EXPIRE_TIME] ?: ""

                if (token.isBlank() || tokenExpireTime.isBlank()) {
                    navigator.pop()
                    return@LaunchedEffect
                }

                val currentTime = LocalDateTime.now(ZoneId.of("UTC"))
                if (tokenExpireTime.toLocalDateTime().isBefore(currentTime)) {
                    navigator.pop()
                    return@LaunchedEffect
                }
                Log.d(TAG, "Content: passed")
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            scaffoldState = scaffoldState,
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
                    Text(
                        text = stringResource(id = R.string.add_user),
                        style = MaterialTheme.typography.h5,
                        color = MaterialTheme.colors.primary
                    )

                    // name
                    TextField(
                        value = viewModel.name,
                        onValueChange = { viewModel.name = it },
                        label = { Text(text = stringResource(id = R.string.name)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.TwoTone.AccountCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colors.onSurface
                            )
                        },
                        isError = viewModel.name.isEmpty(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(medium),
                    )

                    // email
                    TextField(
                        value = viewModel.email,
                        onValueChange = {
                            viewModel.email = it
                            viewModel.checkIfEmailTaken()
                        },
                        label = { Text(text = stringResource(id = R.string.email_address)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.TwoTone.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colors.onSurface
                            )
                        },
                        trailingIcon = {
                            if (viewModel.isEmailValid) {
                                if (viewModel.validatingEmail) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    Icon(
                                        imageVector = if (viewModel.emailAlreadyTaken) {
                                            Icons.TwoTone.Warning
                                        } else {
                                            Icons.TwoTone.Check
                                        },
                                        contentDescription = null,
                                        tint = if (viewModel.emailAlreadyTaken) MaterialTheme.colors.onSurface else MaterialTheme.colors.primary
                                    )
                                }
                            }
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

                    // phone no
                    TextField(
                        value = viewModel.phone,
                        onValueChange = {
                            viewModel.phone = it
                            viewModel.checkIfPhoneNumberUsed()
                        },
                        label = { Text(text = stringResource(id = R.string.phone_number)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.TwoTone.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colors.onSurface
                            )
                        },
                        trailingIcon = {
                            if (viewModel.isPhoneValid) {
                                if (viewModel.validatingPhoneNumber) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    Icon(
                                        imageVector = if (viewModel.phoneAlreadyUsed) {
                                            Icons.TwoTone.Warning
                                        } else {
                                            Icons.TwoTone.Check
                                        },
                                        contentDescription = null,
                                        tint = if (viewModel.phoneAlreadyUsed) MaterialTheme.colors.onSurface else MaterialTheme.colors.primary
                                    )
                                }
                            }
                        },
                        isError = ! viewModel.isPhoneValid,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(medium),
                    )

                    // type
                    TextField(
                        value = viewModel.type,
                        onValueChange = { viewModel.type = it },
                        label = { Text(text = stringResource(id = R.string.type)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.TwoTone.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colors.onSurface
                            )
                        },
                        isError = viewModel.type.isEmpty(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Text,
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
                        isError = ! viewModel.isPasswordValid,
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

                    AnimatedVisibility(visible = viewModel.successText.isNotBlank()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(modifier = Modifier.height(medium))

                            Text(
                                text = viewModel.successText,
                                color = MaterialTheme.colors.primary,
                                textAlign = TextAlign.Center,
                            )

                            Spacer(modifier = Modifier.height(medium))

                            OutlinedButton(
                                onClick = { viewModel.clearFields() },
                            ) {
                                Text(
                                    text = stringResource(id = R.string.add_another),
                                    color = MaterialTheme.colors.primary,
                                    textAlign = TextAlign.Center,
                                )
                            }

                            Spacer(modifier = Modifier.height(medium))
                        }
                    }

                    AnimatedVisibility(visible = viewModel.errorText.isNotBlank()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(0.8f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(medium))

                            Text(
                                text = viewModel.errorText,
                                color = MaterialTheme.colors.error,
                                textAlign = TextAlign.Center,
                            )

                            Spacer(modifier = Modifier.height(medium))
                        }
                    }

                    // add user button
                    Button(
                        onClick = {
                            if (token.isEmpty() || tokenExpireTime.isEmpty()) return@Button
                            if (LocalDateTime.now(ZoneId.of("UTC")).isAfter(tokenExpireTime.toLocalDateTime())) return@Button

                            try {
                                viewModel.onAddUserButtonClicked(token)
                            } catch (e: Exception) {

                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.6f),
                        enabled = viewModel.name.isNotBlank() && viewModel.type.isNotBlank() && viewModel.isEmailValid && viewModel.isPhoneValid && viewModel.isPasswordValid
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(id = R.string.add_user),
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