package com.massdata.massdata.di

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.massdata.massdata.BuildConfig
import com.massdata.massdata.presentation.viewModel.HomeViewModel
import com.massdata.massdata.presentation.viewModel.LogInViewModel
import com.massdata.massdata.presentation.viewModel.SignUpViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {
    /**
     * Provides [Retrofit] singleton instance
     */
    single {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provides [SignUpViewModel] instance
     */
    factory { SignUpViewModel(get()) }

    /**
     * Provides [LogInViewModel] instance
     */
    factory { LogInViewModel(get()) }

    /**
     * Provides [HomeViewModel] instance
     */
    factory { HomeViewModel(get()) }
}