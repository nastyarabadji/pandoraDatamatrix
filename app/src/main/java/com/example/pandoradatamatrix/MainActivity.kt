package com.example.pandoradatamatrix

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.datamatrix.DataMatrixReader
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class MainActivity : AppCompatActivity() {
    lateinit var scanResult: TextView;
    lateinit var scanButton: Button
    lateinit var scanGallery: Button

    private val dataMatrixLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            scanResult.text = result.contents
        }
        else {
            scanResult.text = "Ошибка при сканировании"
        }
    }

    private val requestPermissionLauncher = registerForActivityResult( // проверка разрешений
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) openGallery()
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { processImage(it) }
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun processImage(uri: Uri) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val result = decodeDataMatrix(bitmap)
            scanResult.text = result ?: "Не удалось распознать DataMatrix"
        } catch (e: Exception) {
            scanResult.text = "Ошибка: ${e.localizedMessage}"
        }
    }

    private fun decodeDataMatrix(bitmap: Bitmap): String? {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        val source = RGBLuminanceSource(bitmap.width, bitmap.height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        return try {
            DataMatrixReader().decode(binaryBitmap)?.text
        } catch (e: Exception) {
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        scanResult = findViewById(R.id.scanResult)
        scanButton = findViewById(R.id.scanButton)
        scanGallery = findViewById(R.id.scanGallery)

        scanButton.setOnClickListener {
            val scanOptions = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.DATA_MATRIX)
                setPrompt("")
                setCameraId(0)
                setBeepEnabled(false)
                setOrientationLocked(true)
                setCaptureActivity(CaptureActivity::class.java)
            }
            dataMatrixLauncher.launch(scanOptions)
        }

        scanGallery.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_IMAGES
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                checkPermissions()
            }
        }
    }
}