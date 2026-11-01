package omega.sunkey.sunkensonic.settings

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

// I used https://github.com/omaawr/tuisku as reference for this. He has a much more complete implementation, I'm only making something simpler.
@Serializable
data class Settings(
    val port: Int = 4040
)

object SetSerializer : Serializer<Settings> {
    override val defaultValue = Settings()

    override suspend fun readFrom(input: InputStream): Settings = try {
        Json.decodeFromString(
            Settings.serializer(),
            input.readBytes().decodeToString()
        )
    } catch (e: Exception) {
        Log.e("SettingsSerializer", "explode with ${e.message}")
        defaultValue
    }

    override suspend fun writeTo(t: Settings, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(Json.encodeToString(Settings.serializer(),t).toByteArray())
        }
    }
}

class SettingsRepo(settings: DataStore<Settings>) : BasePrefManager(settings) {
    val port = preference(Settings::port)
}