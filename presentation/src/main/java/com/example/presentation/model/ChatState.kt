package com.example.presentation.model

sealed class ChatState {
    object NewChat: ChatState()
    object Idle: ChatState()
    data class OldChat(val roomId: String): ChatState()
    object Loading: ChatState()
    data class Error(val message: String): ChatState()
}
