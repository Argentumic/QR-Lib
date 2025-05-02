package com.example.qrlib

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class QRcodes(
    val url: String,
    val label: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)
