package id.yuana.downloadmanager.demo.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val CACHE_NAME = "local_cache"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = CACHE_NAME)

private object CacheKeys {
    val LATEST_DOWNLOAD_ID = longPreferencesKey("latest_download_id")
}

class Cache(private val context: Context) {

    fun read(): Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[CacheKeys.LATEST_DOWNLOAD_ID] ?: 0
        }

    suspend fun write(downloadId: Long) {
        context.dataStore.edit { cache ->
            cache[CacheKeys.LATEST_DOWNLOAD_ID] = downloadId
        }
    }
}

