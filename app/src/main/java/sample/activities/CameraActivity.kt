package sample.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_camerax.*
import sample.R
import sample.showToast
import java.io.File
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    companion object {
        val TAG: String = CameraActivity::class.java.simpleName

        val IMAGE_CAPTURED_INTENT = "image_captured"

        // This is an arbitrary number we are using to keep track of the permission
        // request. Where an app has multiple context for requesting permission,
        // this can help differentiate the different contexts.
        private const val MY_PERMISSION_REQUEST_CAMERA = 10

        // This is an array of all the permission specified in the manifest.
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camerax)

        if (allPermissionsGranted()) {
            viewFinderTv.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                MY_PERMISSION_REQUEST_CAMERA
            )
        }

        viewFinderTv.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }
    }

    private fun startCamera() {
        val cameraPreview = Preview(
            PreviewConfig.Builder()
                .setTargetResolution(Size(640, 640))
                .build()
        )

        // recompute layout when viewfinder is updated
        cameraPreview.setOnPreviewOutputUpdateListener {
            // Update surfaceTexture by removing and re-adding viewfinder
            (viewFinderTv.parent as ViewGroup).let { parent ->
                parent.removeView(viewFinderTv)
                parent.addView(viewFinderTv, 0)
            }

            viewFinderTv.surfaceTexture = it.surfaceTexture
            updateTransform()
        }

        val imageCapture = ImageCapture(
            ImageCaptureConfig.Builder()
                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .build()
        )

        val intent = Intent(this, DrawingCanvasActivity::class.java)

        captureButton.setOnClickListener {
            val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")

            imageCapture.takePicture(file, executor,
                object : ImageCapture.OnImageSavedListener {
                    override fun onImageSaved(file: File) {
                        val msg = "Photo capture succeeded: ${file.absolutePath}"
                        Log.d(TAG, msg)

                        captureButton.post {
                            intent.putExtra(IMAGE_CAPTURED_INTENT, file.absolutePath)
                            startActivity(intent)
                            showToast(msg)
                        }
                    }

                    override fun onError(
                        imageCaptureError: ImageCapture.ImageCaptureError,
                        message: String,
                        cause: Throwable?
                    ) {
                        val msg = "Photo capture failed: $message"
                        Log.e(TAG, msg, cause)

                        captureButton.post {
                            showToast(msg)
                        }
                    }
                })
        }

        CameraX.bindToLifecycle(this, cameraPreview, imageCapture)
    }

    private fun updateTransform() {
        val centerX = viewFinderTv.width / 2f
        val centerY = viewFinderTv.height / 2f

        val rotationDegrees = when (viewFinderTv.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }

        val matrix = Matrix()
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        viewFinderTv.setTransform(matrix)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            MY_PERMISSION_REQUEST_CAMERA -> {
                if (allPermissionsGranted()) {
                    viewFinderTv.post { startCamera() }
                } else {
                    Toast.makeText(this, "Permissions not granted.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
}