package com.example.qrlib

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [QRcodes::class],
    version = 1
)
abstract class QRcodesDatabase: RoomDatabase() {

    abstract val dao: QRcodesDao
}