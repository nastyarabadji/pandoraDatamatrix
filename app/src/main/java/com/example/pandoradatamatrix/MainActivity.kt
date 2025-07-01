package com.example.pandoradatamatrix

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class MainActivity : AppCompatActivity() {
    lateinit var scanResult: TextView;
    lateinit var scanButton: Button;

    private val dataMatrixLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            scanResult.text = "${result.contents}"
        }
        else {
            scanResult.text = "Ошибка при сканировании"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        scanResult = findViewById(R.id.scanResult)
        scanButton = findViewById(R.id.scanButton)
        scanButton.setOnClickListener {
            val scanOptions = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.DATA_MATRIX)
                setPrompt("")
                setCameraId(0)
                setBeepEnabled(false)
                setOrientationLocked(true)
                setCaptureActivity(CaptureActivity::class.java) // явно задаём стандартный интерфейс
            }
            dataMatrixLauncher.launch(scanOptions) // запуск сканера и обработка результата сканирования
        }
    }
}