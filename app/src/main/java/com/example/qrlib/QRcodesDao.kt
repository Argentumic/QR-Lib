package com.example.qrlib

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface QRcodesDao {

    @Upsert
    suspend fun upsertQR(qr: QRcodes)

    @Delete
    suspend fun deleteQR(qr: QRcodes)

    @Query("SELECT * FROM QRcodes")
    fun getQR(): Flow<List<QRcodes>>
}