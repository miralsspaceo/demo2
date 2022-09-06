package com.app.personas_social.utlis;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;

import com.app.personas_social.R;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.StyledPlayerView;

public final class MyExoPlayerView extends StyledPlayerView {

    public MyExoPlayerView(Context context) {
        super(context);
    }

    public MyExoPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyExoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void draw(Canvas canvas) {
        switch (background) {
            case 1: {
                canvas.drawColor(Color.TRANSPARENT);
                break;
            }
            case 2: {
                Paint paint = new Paint();
                paint.setShader(new LinearGradient(0, 0, getWidth(), getHeight(), Color.RED, Color.GREEN, Shader.TileMode.MIRROR));
                canvas.drawPaint(paint);
                break;
            }
            case 3: {
                canvas.drawColor(Color.CYAN);
                break;
            }
            case 4: {
                Paint paint = new Paint();
                Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(),
                        R.drawable.dummy_image);
                Matrix m = new Matrix();
                m.setTranslate(0f, 0f);
                m.postScale(2.5f, 2.5f); // calculate scale according your dimens
                canvas.drawBitmap(bitmap, m, paint);
                break;
            }
        }
        if (null != rectF)
            canvas.clipRect(rectF);
        if (null != matrix)
            canvas.setMatrix(matrix);
        super.draw(canvas);
    }

    private Matrix matrix;

    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
        requestLayout();
    }

    private int background;

    public void setBackground(int background) {
        this.background = background;
    }

    private RectF rectF;

    public void setCropRect(RectF rectF) {
        this.rectF = rectF;
    }

    public void reset() {
        matrix = null;
        rectF = null;
    }
}