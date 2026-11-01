package omega.sunkey.sunkensonic.settings

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// I used https://github.com/omaawr/tuisku as reference for this. He has a much more complete implementation, I'm only making something simpler.
abstract class BasePrefManager(protected val settings: DataStore<Settings>) {
    fun <T> preference(getter: (Settings) -> T): Pref<T> = Pref(getter)

    inner class Pref<T>(val getter: (Settings) -> T) {
        operator fun invoke() = settings.data.map(getter)
        suspend fun update(block: (Settings) -> Settings) = settings.updateData(block)

        suspend fun read(): T? = settings.data.map(getter).first()
    }
}