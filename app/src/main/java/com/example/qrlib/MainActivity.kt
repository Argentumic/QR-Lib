package com.example.qrlib

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.qrlib.ui.theme.QRБібліотекаTheme
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState

class MainActivity : ComponentActivity() {
    companion object {
        var url = ""
    }

    private lateinit var database: QRcodesDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = Room.databaseBuilder(
            applicationContext,
            QRcodesDatabase::class.java,
            "qrcodes.db"
        ).build()

        setContent {
            QRБібліотекаTheme {
                var showDialog by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .background(Color.Black)
                        .fillMaxSize()
                ) {
                    QRsList(database)

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        AddNewQRButton(onClick = { showDialog = true })

                        if (showDialog) {
                            DialogWithImage(
                                onDismissRequest = { showDialog = false },
                                onConfirmation = {
                                    showDialog = false
                                    lifecycleScope.launch {
                                        database.dao.upsertQR(QRcodes(url = url))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QRsList(database: QRcodesDatabase) {
    val qrList by database.dao.getQR().collectAsState(initial = emptyList())

    if (!qrList.isEmpty()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            items(qrList) { qr ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) { QrCodeView(url = qr.url) }
            }
        }
    }
}

fun generateQrCodeBitmap(content: String, size: Int = 512): Bitmap {
    val bitMatrix: BitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size, mapOf(EncodeHintType.CHARACTER_SET to "UTF-8"))
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.WHITE else android.graphics.Color.BLACK)
        }
    }
    return bitmap
}

@Composable
fun QrCodeView(url: String) {
    val bitmap = generateQrCodeBitmap(url)
    Image(bitmap = bitmap.asImageBitmap(), contentDescription = "QR Code")
}

@Composable
fun AddNewQRButton(onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.size(72.dp)) {
        Icon(
            Icons.Rounded.Add,
            contentDescription = "Add QR Code",
            tint = Color.White,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
fun DialogWithImage(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SimpleOutlinedTextFieldSample()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Скасувати")
                    }
                    TextButton(
                        onClick = onConfirmation,
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Підтвердити")
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleOutlinedTextFieldSample() {
    var text by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp, max = 300.dp)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (text.isNotBlank()) {
            QrCodeView(url = text)
        } else {
            QrCodeView(url = " ")
        }

        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
                MainActivity.url = it
            },
            label = { Text("Введіть URL") }
        )
    }
}
