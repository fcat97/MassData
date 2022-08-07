package com.massdata.massdata.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.massdata.massdata.network.LogInResponse
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

object DataStoreKeys {
    val TOKEN = stringPreferencesKey("token")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    val TOKEN_EXPIRE_TIME = stringPreferencesKey("token_expire_time")
    val REFRESH_TOKEN_EXPIRE_TIME = stringPreferencesKey("refresh_token_expire_time")
}

object DataStoreUtil {
    suspend fun saveData(dataStore: DataStore<Preferences>, response: LogInResponse) {
        dataStore.edit { preference ->
            preference[DataStoreKeys.TOKEN] = response.value.token
            preference[DataStoreKeys.REFRESH_TOKEN] = response.value.refreshToken
            preference[DataStoreKeys.TOKEN_EXPIRE_TIME] = response.value.tokenExpireTime.toUTC()
            preference[DataStoreKeys.REFRESH_TOKEN_EXPIRE_TIME] = response.value.refreshTokenExpireTime.toUTC()
        }
    }
}

fun String.toLocalDateTime(): LocalDateTime {
    val dft = DateTimeFormatter.ISO_DATE_TIME
    return LocalDateTime.parse(this, dft)
}

fun Date.toUTC(): String {
    return this.toInstant()
        .atZone(ZoneId.of("UTC"))
        .toString()
}

fun LocalDateTime.toUTC(): String {
    return "${this}Z"
}

fun Date.toLocalDateTime(): LocalDateTime {
    return this.toInstant()
        .atZone(ZoneId.of("UTC"))
        .toLocalDateTime()
}

fun LocalDateTime.toDate(): Date {
    return Date.from(
        this.atZone(ZoneId.of("UTC"))
            .toInstant()
    )
}