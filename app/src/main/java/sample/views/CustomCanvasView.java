package sample.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class CustomCanvasView extends View {

    final private Paint paint = new Paint();

    public CustomCanvasView(Context context) {
        super(context);
    }

    public CustomCanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomCanvasView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(300.0f, 300.0f, 100.0f, paint);
    }

}