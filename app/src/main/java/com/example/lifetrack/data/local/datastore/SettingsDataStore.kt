package com.example.lifetrack.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile

object SettingsDataStore {

    private const val FILE_NAME = "lifetrack_settings"

    fun create(context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.applicationContext.preferencesDataStoreFile(FILE_NAME) }
        )
}
