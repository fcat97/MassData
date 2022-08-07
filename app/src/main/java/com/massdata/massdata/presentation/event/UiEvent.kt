package com.massdata.massdata.presentation.event

sealed class UiEvent
data class Success(val msg: String): UiEvent()
data class Failed(val msg: String): UiEvent()
object None: UiEvent()