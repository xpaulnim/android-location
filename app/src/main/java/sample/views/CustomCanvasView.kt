package sample.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintSet;

import sample.R;

class CustomCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        val TAG: String = this::class.java.simpleName
    }

    val paint = Paint()
    val customMatrix = Matrix()
    val options = BitmapFactory.Options();

    var bitmap: Bitmap?

    init {
        //        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.fifty_grand, options);
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas);

//        customMatrix.postRotate(20.0f);
        canvas.drawBitmap(bitmap, customMatrix, paint);

        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(300.0f, 300.0f, 10.0f, paint);
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            printSamples(event);
        } else {
            Log.v(TAG, "actionEvent " + event.getActionMasked());
        }

        return true
    }

    fun printSamples(ev: MotionEvent) {
        val historySize = ev.getHistorySize();
        val pointerCount = ev.getPointerCount();

        for (h in 0 until historySize) {
            System.out.printf("At time %d:", ev.getHistoricalEventTime(h));

            for (p in 0 until pointerCount) {
                Log.v(
                    TAG,
                    "  pointer ${ev.getPointerId(p)}: (${ev.getHistoricalX(
                        p,
                        h
                    )},${ev.getHistoricalY(p, h)})"
                )
            }
        }

        Log.v(TAG, "At time ${ev.getEventTime()}:")
        for (p in 0 until pointerCount) {
            Log.v(TAG, "  pointer ${ev.getPointerId(p)}: (${ev.getX(p)},${ev.getY(p)})")
        }
    }
}