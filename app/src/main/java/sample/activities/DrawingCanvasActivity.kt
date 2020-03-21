package sample.activities

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_drawing_canvas.*
import sample.R
import java.io.File
import java.io.FileOutputStream

class DrawingCanvasActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_drawing_canvas)

        val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.png")

        btnSaveBitmap.setOnClickListener {
            customCanvasView.drawingBitmap!!.compress(Bitmap.CompressFormat.PNG,  100, FileOutputStream(file))
        }
    }
}

