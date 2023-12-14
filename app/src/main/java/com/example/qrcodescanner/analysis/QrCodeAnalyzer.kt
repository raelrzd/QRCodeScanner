package com.example.qrcodescanner.analysis

import android.graphics.ImageFormat.YUV_420_888
import android.graphics.ImageFormat.YUV_422_888
import android.graphics.ImageFormat.YUV_444_888
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.qrcodescanner.extension.toByteArray
import com.example.qrcodescanner.ui.activity.MainActivity.Companion.TAG
import com.google.android.material.tabs.TabLayout.TabGravity
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.Result

class QrCodeAnalyzer (
    private val onQrCodesDetected: (qrCode: Result) -> Unit
): ImageAnalysis.Analyzer {

    private val yuvFormats = mutableListOf(YUV_420_888, YUV_422_888, YUV_444_888)
    private val reader = MultiFormatReader().apply {
        val map = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to arrayListOf(BarcodeFormat.QR_CODE)
        )
        setHints(map)
    }


    override fun analyze(image: ImageProxy) {

        // Estamos usando o formato YUV porque o ImageProxy usa internamente o ImageReader para obter a imagem
        // por padrão, o ImageReader usa o formato YUV, a menos que seja alterado.
        if (image.format !in yuvFormats) {
            Log.e(TAG, "Expected YUV, now = ${image.format}")
            return
        }

        val data = image.planes[0].buffer.toByteArray()
        val source = PlanarYUVLuminanceSource(
            data,
            image.width,
            image.height,
            0,
            0,
            image.width,
            image.height,
            false
        )

        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        try {
            // Whenever reader fails to detect a QR code in image
            // it throws NotFoundException
            val result = reader.decode(binaryBitmap)
            onQrCodesDetected(result)
        } catch (e: NotFoundException) {
            e.printStackTrace()
        }
        image.close()
    }

}