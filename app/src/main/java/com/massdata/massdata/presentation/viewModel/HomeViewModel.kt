package com.massdata.massdata.presentation.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.massdata.massdata.data.toLocalDateTime
import com.massdata.massdata.network.HomeScreenService
import com.massdata.massdata.network.LogInResponse
import com.massdata.massdata.network.RefreshTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.awaitResponse
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

private const val TAG = "HomeViewModel"
class HomeViewModel(
    private val retrofit: Retrofit
): ViewModel() {
    private val service by lazy { retrofit.create(HomeScreenService::class.java) }

    var playerID by mutableStateOf("")
    var tokenTimeRemaining by mutableStateOf("")

    fun validateCredentials(
        token: String,
        refreshToken: String,
        onRefreshToken: (LogInResponse) -> Unit,
        onSuccess: () -> Unit,
        onExpire: () -> Unit
    ) {
        try {
            val testToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                    "MjQwLCJleHAiOjE2NTk4Nzc4NDAsImlhdCI6MTY1OTg1NjI0MCwiaXNzIjoiKG1kbC5uZXRkZXZlbG9wZXJzKSIsImF1ZCI6IlRQR19BUE" +
                    "eyJpZCI6ImQ1ZWE5MTkzLWQ0NTktNGNmZC1hZDkzLTNmZjRhMWY5NmI2NSIsInVzZXJFbWFpbCI6InRwZ2FkbWluQG1hc3NkYXRhLmNvbS" +
                    "IsIm5hbWUiOiJTdXBlciBBZG1pbiIsIlBsYXllcklkIjoiMjU0MTM5NzAyNSIsInJvbGUiOiJTdXBlckFkbWluIiwibmJmIjoxNjU5ODU2" +
                    "lfVXNlciJ9.pX0aXk1CMBSwa1YCwudCRoqPZEQM0y2gu94wJju_-Xc"

            viewModelScope.launch(Dispatchers.IO) {
                val response = service.isTokenValid("Bearer $token").awaitResponse()
                if (response.isSuccessful) {
                    playerID = response.body()?.playerId ?: ""

                    if (response.code() == 200) {
                        Log.d(TAG, "validateCredentials: 1")
                        onSuccess()
                        return@launch
                    }

                    // token expired
                    if (response.code() == 401) {
                        Log.d(TAG, "validateCredentials: 2")
                        refreshToken(RefreshTokenCredential(token, refreshToken)).let {
                            Log.d(TAG, "validateCredentials: 3")
                            if (it.isSuccessful && it.code() == 200) {
                                Log.d(TAG, "validateCredentials: 4")
                                it.body()?.let { newToken ->
                                    Log.d(TAG, "validateCredentials: 5")
                                    onRefreshToken(newToken)
                                }
                            } else {
                                Log.d(TAG, "validateCredentials: 6")
                                onExpire()
                            }
                        }
                    }
                    Log.d(TAG, "validateCredentials: ss ${response.raw()}")
                } else {
                    // no such token
                    Log.d(TAG, "validateCredentials: 7")
                    onExpire()
                }
                Log.d(TAG, "validateCredentials: $response")
            }
        } catch (e: Exception) {
            Log.e(TAG, "validateCredentials: ", e)
        }
    }

    private suspend fun refreshToken(refreshTokenCredential: RefreshTokenCredential): Response<LogInResponse> {
        return service.refreshToken(refreshTokenCredential).awaitResponse()
    }

    fun startExpireClock(tokenExpire: String) {
        viewModelScope.launch {
            val tokenExpireDate = tokenExpire.toLocalDateTime()

            while(true) {
                val currentTime = LocalDateTime.now(ZoneId.of("UTC"))
                calculateTimeDifference(currentTime, tokenExpireDate)
                delay(1000)
            }
        }
    }

    private fun calculateTimeDifference(start: LocalDateTime, end: LocalDateTime) {
        val diff = end.toEpochSecond(ZoneOffset.UTC) - start.toEpochSecond(ZoneOffset.UTC)

        val diffSeconds = (diff % 60)
        val diffMinutes = (diff / 60 % 60)
        val diffHours = (diff / (60 * 60) % 24)
        val diffDays = (diff / (24 * 60 * 60))

        tokenTimeRemaining = "${diffDays}d:${diffHours}h:${diffMinutes}m:${diffSeconds}s"
    }
}