package com.example.myfilimapp.dataStore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "data_store")

class UserPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val appContext = context.applicationContext

    val isLogin: Flow<Boolean?>
        get() = appContext.dataStore.data.map { preferences ->
            preferences[IS_LOGGED_IN]
        }



    suspend fun saveLogged(login: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = login
        }
    }



    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("IS_LOGGED_IN")

    }
}