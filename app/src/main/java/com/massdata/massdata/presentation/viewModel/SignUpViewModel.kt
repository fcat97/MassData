package com.massdata.massdata.presentation.viewModel

import android.util.Log
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.massdata.massdata.BuildConfig
import com.massdata.massdata.network.SignUpCredential
import com.massdata.massdata.network.SignUpServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.awaitResponse
import java.util.*
import kotlin.random.Random

private const val TAG = "SignUpViewModel"
class SignUpViewModel(
    private val retrofit: Retrofit
): ViewModel() {
    private val signUpServices by lazy { retrofit.create(SignUpServices::class.java) }

    var taskRunning by mutableStateOf(false)

    // only for test purpose.
    private val randomNum: Int get() = Random(1).nextInt(until = 9)

    private var accountID by mutableStateOf(UUID.randomUUID().toString())
    var name by mutableStateOf(if (BuildConfig.DEBUG) "test $randomNum" else "")
    var email by mutableStateOf(if (BuildConfig.DEBUG) "$randomNum@$randomNum.$randomNum" else "")
    var phone by mutableStateOf(if (BuildConfig.DEBUG) "01" + randomNum + "1" + randomNum + "1" + randomNum + "0" + randomNum + "21" else "")
    var password by mutableStateOf(if (BuildConfig.DEBUG) "1a2B3$40" else "")

    var showPassword by mutableStateOf(false)

    var errorText by mutableStateOf("")

    val isEmailValid: Boolean get() = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isPhoneValid: Boolean get() = phone.length == 11 && "(^01[0-9]\\w+)".toRegex().matches(phone)
    val isPasswordValid: Boolean get() = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[\\W]).{8,32})".toRegex().matches(password)

    private val buttonClickable: Boolean get() = name.isNotBlank() && isEmailValid && isPhoneValid && isPasswordValid

    fun onSignUpButtonClick() {
        if (! buttonClickable) {
            errorText = if (name.isEmpty()) "Enter Your Name"
            else if (! isEmailValid) "Invalid Email Address!"
            else if (! isPhoneValid) "Phone Number must be from Bangladesh!"
            else if (! isPasswordValid) "Password must be of 8-32 characters and a combination of [0-1], [a-z], [A-Z] & [!@#$%^&*]"
            else ""

            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            taskRunning = true

            signUpServices.isEmailTaken(email)
                .awaitResponse()
                .body()?.let {
                    if (it.value) {
                        errorText = "Email Already exists!"
                        taskRunning = false
                        return@launch
                    }
                    Log.d(TAG, "onSignUpButtonClick: $it")
                }

            signUpServices.isNumberTaken(phone)
                .awaitResponse()
                .body()?.let {
                    if (it.value) {
                        errorText = "Number Already exists!"
                        taskRunning = false
                        return@launch
                    }
                    Log.d(TAG, "onSignUpButtonClick: $it")
                }

            signUpServices.isAccountIDExists(accountID)
                .awaitResponse()
                .body()?.let {
                    if (it.value) {
                        errorText = "Internal Error! Try Again!"
                        taskRunning = false
                        return@launch
                    }
                    Log.d(TAG, "onSignUpButtonClick: $it")
                }

            // everything worked fine
            // now create user
            val response = signUpServices.createUser(SignUpCredential(
                name = name,
                email = email,
                password = password,
                accountId = accountID,
                phoneNumber = phone
            )).awaitResponse()

            if (response.isSuccessful) {
                response.body()?.let {
                    when(it.statusCode) {
                        200 -> Log.d(TAG, "onSignUpButtonClick: registration complete")

                        500 -> errorText = "Registration Failed. Please try again!"

                        else -> Log.d(TAG, "onSignUpButtonClick: ${it.message}")
                    }
                    Log.d(TAG, "onSignUpButtonClick: $it")
                    taskRunning = false
                    return@launch
                }
            } else {
                Log.d(TAG, "onSignUpButtonClick: $response")
            }

            Log.d(TAG, "onSignUpButtonClick: passed")
            taskRunning = false
        }
    }
}