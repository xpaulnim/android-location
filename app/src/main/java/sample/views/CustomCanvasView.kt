package sample.views;

import android.content.Context;
import android.graphics.*
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import sample.R;

class CustomCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null, // Required when view is inflated from XML
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        val TAG: String = this::class.java.simpleName
    }

    private val paint = Paint()
    private val customMatrix = Matrix()
    private val options = BitmapFactory.Options();

    private var bitmapPath: String? = null

    private var imageBitmap: Bitmap? = null
    private var myCanvas: Canvas? = null
    var drawingBitmap: Bitmap? = null

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CustomCanvasView, 0, 0
        ).apply {
            try {
                bitmapPath = getString(R.styleable.CustomCanvasView_bitmapPath)
            } finally {
                recycle()
            }
        }

        //        options.inJustDecodeBounds = true;
        options.inSampleSize = 1

        if (bitmapPath.isNullOrEmpty()) {
            imageBitmap = BitmapFactory.decodeResource(resources, R.drawable.fifty_grand, options)
        }
    }

    fun setBitmapPath(path: String) {
        bitmapPath = path

        imageBitmap = BitmapFactory.decodeFile(bitmapPath, options)

        invalidate()
        requestLayout()
    }


    var pointX = 300.0f
    var pointY = 300.0f
    var isHighlighting = false;

    var initHighlightPosX: Float = 0.0f
    var initHighlightPosY: Float = 0.0f

    var moveHighlightPosX: Float = 0.0f
    var moveHighlightPosY: Float = 0.0f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas);
        drawStuff(canvas)
    }

    private fun drawStuff(canvas: Canvas) {
        paint.alpha = 255
        paint.strokeWidth = 0f
        paint.isAntiAlias = true
        paint.color = Color.RED
        paint.style = Paint.Style.FILL

//        customMatrix.postRotate(20.0f);
        canvas.drawBitmap(imageBitmap!!, customMatrix, paint)

        if (isHighlighting) {
            paint.alpha = 50
            paint.strokeWidth = 30f
            canvas.drawLine(
                initHighlightPosX,
                initHighlightPosY,
                moveHighlightPosX,
                moveHighlightPosY,
                paint
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isHighlighting = true
                initHighlightPosX = event.x
                initHighlightPosY = event.y

                // start with all at inital click point
                moveHighlightPosX = event.x
                moveHighlightPosY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                moveHighlightPosX = event.x
                moveHighlightPosY = event.y
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                Log.v(TAG, "Drag finished. Anchoring highlight at last moveHighlightPosX")

                isHighlighting = false
            }
        }

        pointX = event.x
        pointY = event.y

        if (isHighlighting) {
            invalidate()
            Log.v(TAG, "This is ${this.width} by ${this.height}")

            drawingBitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
            myCanvas = Canvas(drawingBitmap!!)

            drawStuff(myCanvas!!)

            this.draw(myCanvas)
        }

        return true
    }
}