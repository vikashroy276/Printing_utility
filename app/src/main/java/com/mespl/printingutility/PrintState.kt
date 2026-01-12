package com.mespl.printingutility

sealed class PrintState {
    object Idle : PrintState()
    object Loading : PrintState()
    data class Completed(val result: PrintResult) : PrintState()
}