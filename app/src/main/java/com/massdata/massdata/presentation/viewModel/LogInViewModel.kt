package com.massdata.massdata.presentation.viewModel

import android.util.Log
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.massdata.massdata.BuildConfig
import com.massdata.massdata.network.LogInCredential
import com.massdata.massdata.network.LogInResponse
import com.massdata.massdata.network.LogInService
import com.massdata.massdata.presentation.event.Failed
import com.massdata.massdata.presentation.event.None
import com.massdata.massdata.presentation.event.Success
import com.massdata.massdata.presentation.event.UiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.awaitResponse
import kotlin.random.Random

private const val TAG = "LogInViewModel"
class LogInViewModel(
    private val retrofit: Retrofit
): ViewModel() {
    private val service by lazy { retrofit.create(LogInService::class.java) }

    var taskRunning by mutableStateOf(false)
    var uiEvent by mutableStateOf<UiEvent>(None)

    var email by mutableStateOf(if (BuildConfig.DEBUG) BuildConfig.API_SU_EMAIL else "")
    var password by mutableStateOf(if (BuildConfig.DEBUG) BuildConfig.API_SU_PASSWORD else "")

    var showPassword by mutableStateOf(false)
    val isEmailValid: Boolean get() = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun onLogInButtonClicked(
        saveToken: (LogInResponse) -> Unit,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRunning = true

            try {
                val response =
                    service.logIn(credential = LogInCredential(email, password)).awaitResponse()
                if (response.isSuccessful) {
                    response.body()?.let {
                        when (it.statusCode) {
                            200 -> {
                                saveToken(it)
                                uiEvent = Success("Logged in with $email")
                                onSuccess()
                            }

                            401 -> uiEvent = Failed("Invalid email or password!")

                            else -> uiEvent = Failed("Failed to log in. Unknown error!")
                        }
                    }
                } else {
                    uiEvent = Failed("Failed to log in. Unknown error!")
                }
            } catch (e: Exception) {
                Log.e(TAG, "onLogInButtonClicked: ", e)
            }

            taskRunning = false
        }
    }
}