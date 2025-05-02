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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    companion object {
        var url = ""
        var label = ""
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
                                        database.dao.upsertQR(QRcodes(url = url, label = label))
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
    val scope = rememberCoroutineScope()

    if (!qrList.isEmpty()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            items(qrList) { qr ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QrCodeView(url = qr.url, label = qr.label)
                        DeleteQR(onClick = {
                            scope.launch {
                                database.dao.deleteQR(qr)
                            }
                        })
                    }
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
fun QrCodeView(url: String, label: String) {
    val bitmap = generateQrCodeBitmap(url)
    Column {
        Text(text = label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.padding(start = 28.dp))
        Image(bitmap = bitmap.asImageBitmap(), contentDescription = "QR Code")
    }
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
fun DeleteQR(onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Icon(
            Icons.Rounded.Delete,
            contentDescription = "Delete QR Code",
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
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .height(500.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                InputField()
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
fun InputField() {
    var textForURL by remember { mutableStateOf("") }
    var textForLabel by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp, max = 400.dp)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,

    ) {
        if (textForURL.isNotBlank()) {
            QrCodeView(url = textForURL, label = " ")
        } else {
            QrCodeView(url = " ", label = " ")
        }

        OutlinedTextField(
            value = textForURL,
            modifier = Modifier.padding(top = 8.dp),
            onValueChange = {
                textForURL = it
                MainActivity.url = it
            },
            label = { Text("Введіть URL") }
        )
        OutlinedTextField(
            value = textForLabel,
            modifier = Modifier.padding(top = 8.dp),
            onValueChange = {
                textForLabel = it
                MainActivity.label = it
            },
            label = { Text("Введіть назву") }
        )
    }
}
