package moe.ouom.neriplayer.ui.viewmodel.agent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.ouom.neriplayer.core.api.search.MusicPlatform
import moe.ouom.neriplayer.core.api.search.SongSearchInfo
import moe.ouom.neriplayer.core.di.AppContainer
import moe.ouom.neriplayer.ui.viewmodel.playlist.SongItem
import moe.ouom.neriplayer.util.SearchManager

enum class AgentMessageRole {
    USER,
    ASSISTANT
}

data class AgentChatMessage(
    val role: AgentMessageRole,
    val text: String
)

data class MusicAgentUiState(
    val messages: List<AgentChatMessage> = listOf(
        AgentChatMessage(
            role = AgentMessageRole.ASSISTANT,
            text = "想听什么可以直接告诉我，也可以说说你现在的心情，我会帮你选一首合适的歌。"
        )
    ),
    val input: String = "",
    val working: Boolean = false
)

data class MusicAgentPlaybackEvent(
    val songs: List<SongItem>,
    val startIndex: Int
)

class MusicAgentViewModel(application: Application) : AndroidViewModel(application) {
    private val agentClient = AppContainer.deepSeekMusicAgentClient

    private val _uiState = MutableStateFlow(MusicAgentUiState())
    val uiState: StateFlow<MusicAgentUiState> = _uiState

    private val playbackEvents = Channel<MusicAgentPlaybackEvent>(Channel.BUFFERED)
    val playbackEventFlow = playbackEvents.receiveAsFlow()

    fun updateInput(value: String) {
        _uiState.value = _uiState.value.copy(input = value)
    }

    fun send() {
        val userText = _uiState.value.input.trim()
        if (userText.isBlank() || _uiState.value.working) return

        val previousMessages = _uiState.value.messages
        _uiState.value = _uiState.value.copy(
            input = "",
            working = true,
            messages = previousMessages + AgentChatMessage(AgentMessageRole.USER, userText)
        )

        viewModelScope.launch {
            val history = previousMessages.map { message ->
                val role = when (message.role) {
                    AgentMessageRole.USER -> "user"
                    AgentMessageRole.ASSISTANT -> "assistant"
                }
                role to message.text
            }

            val decision = agentClient.decide(userText, history)
            val reply = if (decision.shouldPlay && decision.searchQueries().isNotEmpty()) {
                playBestMatch(decision.searchQueries(), decision.reply)
            } else {
                decision.reply
            }

            _uiState.value = _uiState.value.copy(
                working = false,
                messages = _uiState.value.messages + AgentChatMessage(AgentMessageRole.ASSISTANT, reply)
            )
        }
    }

    private suspend fun playBestMatch(queries: List<String>, agentReply: String): String {
        for (query in queries) {
            val first = withContext(Dispatchers.IO) {
                SearchManager.search(query, MusicPlatform.CLOUD_MUSIC).firstOrNull()
            } ?: continue

            playbackEvents.send(
                MusicAgentPlaybackEvent(
                    songs = listOf(first.toSongItem()),
                    startIndex = 0
                )
            )
            return "$agentReply\n正在播放：${first.songName} - ${first.singer}"
        }

        return "$agentReply\n我理解你现在的感受，但暂时没有找到合适的可播放结果。"
    }
}

private fun SongSearchInfo.toSongItem(): SongItem {
    return SongItem(
        id = id.toLongOrNull() ?: id.hashCode().toLong(),
        name = songName,
        artist = singer,
        album = albumName.orEmpty(),
        albumId = 0L,
        durationMs = parseDurationMillis(duration),
        coverUrl = coverUrl?.replace("http://", "https://"),
        channelId = "netease",
        audioId = id
    )
}

private fun parseDurationMillis(duration: String): Long {
    val parts = duration.split(":")
    if (parts.size != 2) return 0L
    val minutes = parts[0].toLongOrNull() ?: return 0L
    val seconds = parts[1].toLongOrNull() ?: return 0L
    return (minutes * 60 + seconds) * 1000L
}
