package com.massdata.massdata.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.AccountCircle
import androidx.compose.material.icons.twotone.Email
import androidx.compose.material.icons.twotone.Lock
import androidx.compose.material.icons.twotone.Phone
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
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.massdata.massdata.R
import com.massdata.massdata.presentation.viewModel.SignUpViewModel
import com.massdata.massdata.ui.theme.large
import com.massdata.massdata.ui.theme.medium
import org.koin.androidx.compose.inject

class SignUpScreen: AndroidScreen() {
    @Composable
    override fun Content() {
        val scaffoldState = rememberScaffoldState()
        val focusManager = LocalFocusManager.current
        val viewModel by inject<SignUpViewModel>()
        val navigator = LocalNavigator.currentOrThrow

        val passwordVisibilityIcon = if (viewModel.showPassword) {
            painterResource(id = R.drawable.ic_twotone_visibility_24)
        } else {
            painterResource(id = R.drawable.ic_twotone_visibility_off_24)
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            scaffoldState = scaffoldState
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

                    // phone no
                    TextField(
                        value = viewModel.phone,
                        onValueChange = { viewModel.phone = it },
                        label = { Text(text = stringResource(id = R.string.phone_number)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.TwoTone.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colors.onSurface
                            )
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

                    // sign up button
                    Button(
                        onClick = { viewModel.onSignUpButtonClick() },
                        modifier = Modifier
                            .fillMaxWidth(0.6f),
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(id = R.string.sign_up),
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

                    // log in button
                    Spacer(modifier = Modifier.height(large))
                    Text(text = stringResource(id = R.string.already_have_an_account))
                    TextButton(onClick = {
                        navigator.pop()
                    }) {
                        Text(text = stringResource(id = R.string.log_in))
                    }
                }
            }
        }
    }
}