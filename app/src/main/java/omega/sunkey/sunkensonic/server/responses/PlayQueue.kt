package omega.sunkey.sunkensonic.server.responses

import omega.sunkey.sunkensonic.server.toIsoTime
import kotlin.collections.set
import kotlin.time.Clock

class SubsonicPlayQueue {
    private val _songQueues = mutableMapOf<String, MutableList<SongData>>()
    private val _queues = mutableMapOf<String, PlayQueue>()

    fun getQueue(username: String): PlayQueue {
        if(_queues.containsKey(username)) {
            return _queues[username]!!
        } else {
            _songQueues[username] = mutableListOf()
            _queues[username] = PlayQueue(
                current = null,
                position = null,
                username = username,
                changed = null,
                changedBy = null,
                entry = _songQueues[username]!!
            )
            return _queues[username]!!
        }
    }

    fun addToQueueByUsername(songData: SongData, username: String, client: String) {
        if (_songQueues.containsKey(username) && _queues.containsKey(username)) {
            _songQueues[username]!!.add(songData)
            _queues[username] = _queues[username]!!.copy(
                changed = Clock.System.now().epochSeconds.toIsoTime(),
                changedBy = client
            )
        }
    }

    fun removeByIndexAndUsername(index: Int, username: String, client: String) {
        if (_songQueues.containsKey(username) && _queues.containsKey(username)) {
            _songQueues[username]!!.removeAt(index)
            _queues[username] = _queues[username]!!.copy(
                changed = Clock.System.now().epochSeconds.toIsoTime(),
                changedBy = client
            )
        }
    }

    fun setCurrentByUsername(current: String, position: Long, username: String) {
        if(_queues.containsKey(username)) {
            _queues[username] = _queues[username]!!.copy(
                current = current,
                position = position,
            )
        }
    }

    fun clearQueueByUsername(username: String, client: String) {
        if (_songQueues.containsKey(username) && _queues.containsKey(username)) {
            _songQueues[username]!!.removeAll(_songQueues[username]!!)
            _queues[username] = _queues[username]!!.copy(
                current = null,
                position = null,
                changed = Clock.System.now().epochSeconds.toIsoTime(),
                changedBy = client
            )
        }
    }
}

data class PlayQueue(
    val current: String?,
    val position: Long?,
    val username: String,
    val changed: String?,
    val changedBy: String?,
    val entry: List<SongData>
)

data class PlayQueueView(
    val playQueue: PlayQueue
)