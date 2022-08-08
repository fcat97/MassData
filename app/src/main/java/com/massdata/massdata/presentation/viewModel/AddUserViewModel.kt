package com.massdata.massdata.presentation.viewModel

import android.util.Log
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.massdata.massdata.BuildConfig
import com.massdata.massdata.network.ShortResponse
import com.massdata.massdata.network.SignUpCredential
import com.massdata.massdata.network.SignUpServices
import com.massdata.massdata.network.SignUpUnsuccessfulResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.awaitResponse
import java.nio.charset.Charset
import java.util.*
import kotlin.random.Random

private const val TAG = "SignUpViewModel"
class SignUpViewModel(
    private val retrofit: Retrofit
): ViewModel() {
    private val signUpServices by lazy { retrofit.create(SignUpServices::class.java) }

    var taskRunning by mutableStateOf(false)

    // only for test purpose.
    private val randomNum: Int get() = Random.nextInt(until = 9)
    private val randChar: String get() = (97..122).random().toChar().toString()
    private val randomMail: String get() = "${randChar.repeat(5)}@${randChar.repeat(4)}.com"

    private var accountID by mutableStateOf(UUID.randomUUID().toString())
    var name by mutableStateOf(if (BuildConfig.DEBUG) "test $randomNum" else "")
    var email by mutableStateOf(if (BuildConfig.DEBUG) randomMail else "")
    var phone by mutableStateOf(if (BuildConfig.DEBUG) "01" + randomNum + "1" + randomNum + "1" + randomNum + "0" + randomNum + "21" else "")
    var password by mutableStateOf(if (BuildConfig.DEBUG) "1a2B3$40" else "")
    var type by mutableStateOf(if (BuildConfig.DEBUG) "NormalUser" else "")

    var showPassword by mutableStateOf(false)

    var successText by mutableStateOf("")
    var errorText by mutableStateOf("")
    var hints by mutableStateOf("")

    val isEmailValid: Boolean get() = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isPhoneValid: Boolean get() = phone.length == 11 && "(^01[0-9]+\$)".toRegex().matches(phone)
    val isPasswordValid: Boolean get() = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[\\W]).{8,32})".toRegex().matches(password)

    var validatingEmail by mutableStateOf(false)
    var emailAlreadyTaken by mutableStateOf(false)
    private var emailValidationJob: Job? = null
    fun checkIfEmailTaken() {
        if (! isEmailValid) return

        emailValidationJob?.cancel()
        emailValidationJob = viewModelScope.launch(Dispatchers.IO) {
            validatingEmail = true
            val response = signUpServices.isEmailTaken(email).awaitResponse()
            if (response.isSuccessful) {
                response.body()?.let {
                    emailAlreadyTaken = it.value
                }
            } else {
                emailAlreadyTaken = false
            }
            validatingEmail = false
        }
    }

    var validatingPhoneNumber by mutableStateOf(false)
    var phoneAlreadyUsed by mutableStateOf(false)
    private var phoneValidationJob: Job? = null
    fun checkIfPhoneNumberUsed() {
        if (! isPhoneValid) return

        phoneValidationJob?.cancel()
        phoneValidationJob = viewModelScope.launch(Dispatchers.IO) {
            validatingPhoneNumber = true
            val response = signUpServices.isNumberTaken(email).awaitResponse()
            if (response.isSuccessful) {
                response.body()?.let {
                    phoneAlreadyUsed = it.value
                }
            } else {
                phoneAlreadyUsed = false
            }
            validatingPhoneNumber = false
        }
    }

    fun onAddUserButtonClicked(token: String) {
        errorText = ""

        hints = if (name.isEmpty()) "Enter Your Name"
        else if (! isEmailValid) "Invalid Email Address!"
        else if (! isPhoneValid) "Phone Number must be from Bangladesh!"
        else if (! isPasswordValid) "Password must be of 8-32 characters and a combination of [0-1], [a-z], [A-Z] & [!@#$%^&*]"
        else if (type.isBlank()) "Please give a user type"
        else ""

        viewModelScope.launch(Dispatchers.IO) {
            try {
                taskRunning = true

                signUpServices.isEmailTaken(email)
                    .awaitResponse()
                    .body()?.let {
                        if (it.value) {
                            errorText = "Email Already exists!"
                            taskRunning = false
                            return@launch
                        }
                    }

                signUpServices.isNumberTaken(phone)
                    .awaitResponse()
                    .body()?.let {
                        if (it.value) {
                            errorText = "Number Already exists!"
                            taskRunning = false
                            return@launch
                        }
                    }

                signUpServices.isAccountIDExists(accountID)
                    .awaitResponse()
                    .body()?.let {
                        if (it.value) {
                            errorText = "Internal Error! Try Again!"
                            taskRunning = false
                            return@launch
                        }
                    }

                // everything worked fine
                // now create user
                val tok = token.toCharArray().shuffle() // for test purpose
                val response = signUpServices.addUser(
                    token = "Bearer $token",
                    credential = SignUpCredential(
                        name = name,
                        email = email,
                        password = password,
                        accountId = accountID,
                        phoneNumber = phone,
                        type = type
                    )
                ).awaitResponse()

                if (response.isSuccessful && response.code() == 200) {
                    if (response.body()?.statusCode == 200) {
                        successText = response.body()?.message ?: ""
                    } else {
                        errorText = response.body()?.message ?: ""
                    }
                } else if (response.code() == 401) {
                    errorText = "Unauthorized access"
                } else {
                    val errorBody = response.errorBody()?.string()
                    errorText = try {
                        val error = Gson().fromJson(errorBody, SignUpUnsuccessfulResponse::class.java)
                        if (error.errors.Email.isNotEmpty()) error.errors.Email.joinToString(postfix = "\n") { it } else ""+
                                if (error.errors.Password.isNotEmpty()) error.errors.Password.joinToString(postfix = "\n") { it } else "" +
                                        if (error.errors.PhoneNumber.isNotEmpty()) error.errors.PhoneNumber.joinToString { it } else ""
                    } catch (e: Exception) {
                        try {
                            val error = Gson().fromJson(errorBody, ShortResponse::class.java)
                            error.message
                        } catch (e: Exception) {
                            "Unknown error"
                        }
                    }
                }
                taskRunning = false
            } catch (e: Exception) {
                Log.e(TAG, "onAddUserButtonClicked: timeout", e)
                taskRunning = false
            }
        }
    }

    fun clearFields() {
        name = ""
        email = ""
        phone = ""
        type = ""
        password = ""
        accountID = UUID.randomUUID().toString()

        successText = ""
    }
}