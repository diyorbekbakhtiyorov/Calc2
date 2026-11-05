package com.example.arifmetikamallar.data

data class HistoryItem(
    val id: Long = 0,
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)
