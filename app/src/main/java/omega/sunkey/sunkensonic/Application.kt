package omega.sunkey.sunkensonic

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import okhttp3.OkHttpClient
import omega.sunkey.sunkensonic.settings.SetSerializer
import omega.sunkey.sunkensonic.settings.SettingsRepo

class SunkensonicApplication : android.app.Application() {
    val dataStore = DataStoreFactory.create(serializer = SetSerializer, produceFile = { applicationContext.dataStoreFile("settings")})
    val settings = SettingsRepo(dataStore)
    val httpClient = OkHttpClient()
}