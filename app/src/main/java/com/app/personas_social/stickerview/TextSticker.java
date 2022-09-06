package com.app.personas_social.stickerview;

import static com.app.personas_social.utlis.ConstantsKt.BLANK_BORDER;
import static com.app.personas_social.utlis.ConstantsKt.DARK_BORDER;
import static com.app.personas_social.utlis.ConstantsKt.DOT_BORDER;
import static com.app.personas_social.utlis.ConstantsKt.FILTER;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.app.personas_social.R;
import com.app.personas_social.videoEditing.Callback;


/**
 * Customize your sticker with text and image background.
 * You can place some text into a given region, however,
 * you can also add a plain text sticker. To support text
 * auto resizing , I take most of the code from AutoResizeTextView.
 * See https://adilatwork.blogspot.com/2014/08/android-textview-which-resizes-its-text.html
 * Notice: It's not efficient to add long text due to too much of
 * StaticLayout object allocation.
 * Created by liutao on 30/11/2016.
 */

public class TextSticker extends Sticker{

    /**
     * Our ellipsis string.
     */
    private static final String mEllipsis = "\u2026";
    private final Context context;
    private Rect realBounds;
    private Rect textRect;
    private final TextPaint textPaint;
    private Drawable drawable;
    private StaticLayout staticLayout;
    private Layout.Alignment alignment;
    private AppCompatTextView textView;
    public String TAG = java.lang.Class.class.getSimpleName();

    private String text;
    private String strTag;
    private String fontStyle;
    private Long startTime = 0L;
    private Long endTime = 0L;
    private int borderColor = 0;
    private String mType = "";
    private int[] color;
    private int pattern;

    private Float textX;

    private Float strokeWidth = 0f;

    public Float getPadding() {
        return padding;
    }

    public void setPadding(Float padding) {
        this.padding = padding;
    }

    private Float padding;

    private Integer paddingText;

    public String getFontStyle() {
        return fontStyle;
    }

    public Layout.Alignment alignment() {
        return alignment;
    }

    public void setFontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
    }

    public Long getStartTime() {
        return startTime;
    }

    public String getstrtag() {
        return strTag;
    }

    public void setStrTag(String  strTag) {
        this.strTag = strTag;
    }
   public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }


    /**
     * Upper bounds for text size.
     * This acts as a starting point for resizing.
     */
    private float maxTextSizePixels;

    /**
     * Lower bounds for text size.
     */
    private float minTextSizePixels;

    /**
     * Line spacing multiplier.
     */
    private float lineSpacingMultiplier = 1.0f;

    /**
     * Additional line spacing.
     */
    private float lineSpacingExtra = 0.0f;


    public TextSticker(@NonNull Context context) {


        this(context, null);
    }

    private TextSticker(@NonNull Context context, @Nullable Drawable drawable) {
        this.context = context;
        this.drawable = drawable;
        if (drawable == null) {
            this.drawable = ContextCompat.getDrawable(context, R.drawable.sticker_transparent_background);
        }
        textPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        realBounds = new Rect(0, 0, this.drawable.getIntrinsicWidth(), this.drawable.getIntrinsicHeight());
        textRect =new Rect(0, 0, this.drawable.getIntrinsicWidth(), this.drawable.getIntrinsicHeight());
        minTextSizePixels = convertSpToPx(12);
        maxTextSizePixels = convertSpToPx(30);
        //  maxTextSizePixels =(int) CommanUtils.INSTANCE.dpToPx(200);
        alignment = Layout.Alignment.ALIGN_CENTER;
        Log.e(TAG, "TextSticker: "+maxTextSizePixels);
        textPaint.setTextSize(maxTextSizePixels);
    }

    public float gerestLineSpacingMultiplier() {
        return lineSpacingMultiplier;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Matrix matrix = getMatrix();
        canvas.save();
        canvas.concat(matrix);
        if (drawable != null) {
            drawable.setBounds(realBounds);
            drawable.draw(canvas);
        } else {
            this.drawable = ContextCompat.getDrawable(context, R.drawable.sticker_transparent_background);
            realBounds = new Rect(0, 0, getWidth(), getHeight());
            textRect = new Rect(0, 0, getWidth(), getHeight());
        }

        canvas.restore();
        canvas.save();
        canvas.concat(matrix);



/*
        val strokePaint = TextPaint(staticLayout!!.paint)
        strokePaint.style = Paint.Style.STROKE;
        strokePaint.color = strokeColor
        strokePaint.strokeWidth = strokeWidth*/

        if (strokeWidth > 0) {
            TextPaint strokePaint = new TextPaint(textPaint);
            strokePaint.setStyle(Paint.Style.STROKE);
            if (borderColor != 0) {
                strokePaint.setColor(borderColor);
            }
            if (mType.equals(DOT_BORDER)) {
                strokePaint.setPathEffect(new DashPathEffect(new float[]{15f, 10f}, 0f));
            } else if (mType.equals(DARK_BORDER)) {
                strokePaint.setStrokeWidth(20f);
            }
            else if (mType.equals(BLANK_BORDER)) {
                strokePaint.setStrokeWidth(0f);
            }else{
                strokePaint.setStrokeWidth(strokeWidth);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout strokeLayout = StaticLayout.Builder
                        .obtain(this.text, 0, this.text.length(), strokePaint, staticLayout.getWidth())
                        .setAlignment(alignment)
                        .setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)
                        .setIncludePad(true)
                        .build();

                strokeLayout.draw(canvas);

            } else {
                StaticLayout strokeLayout = new StaticLayout(
                        this.text, strokePaint, staticLayout.getWidth(), alignment,
                        lineSpacingMultiplier, lineSpacingExtra, true
                );
                strokeLayout.draw(canvas);

            }
        }

        staticLayout.draw(canvas);

        Log.e(TAG, "staticLayout.getHeight(): "+staticLayout.getHeight());
        canvas.restore();




    }


    private float width, height;


    public void setTextWidth(float tWidth) {
        width = tWidth;
    }

    public void setTextHeight(float tHeight) {
        height = tHeight;
    }

    public float getTextWidth() {
        return width;
    }

    public float getTextHeight() {
        return height;
    }

    public float getTransX() {
        return (float) (getMatrixValue(getMatrix(), Matrix.MTRANS_X));
    }

    public float getTransY() {
        return (float) (getMatrixValue(getMatrix(), Matrix.MTRANS_Y));
    }


    public double getX() {
        float rAngle = getCurrentAngle();
        double x1 = getTransX();
        double x2 = x1 + (width * Math.cos(getRadian(rAngle)));
        double x3 = x2 + (height * Math.cos(getRadian(rAngle + 90)));
        double x4 = x3 + (width * Math.cos(getRadian(rAngle + 180)));
        return Math.min(x1, Math.min(x2, Math.min(x3, x4)));
    }



    public double getY() {
        float rAngle = getCurrentAngle();
        double y1 = getTransY();
        double y2 = y1 + (width * Math.sin(getRadian(rAngle)));
        double y3 = y2 + (height * Math.sin(getRadian(rAngle + 90)));
        double y4 = y3 + (width * Math.sin(getRadian(rAngle + 180)));
        return Math.min(y1, Math.min(y2, Math.min(y3, y4)));
    }

    public double getRadian(float angle) {
        return angle * Math.PI / 180f;
    }


    @Override
    public int getWidth() {
        return staticLayout.getWidth();
    }

    @Override
    public int getHeight() {
        return staticLayout.getHeight();
    }

    @Override
    public void release() {
        super.release();
        if (drawable != null) {
            drawable = null;
        }
    }

    @NonNull
    @Override
    public TextSticker setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        textPaint.setAlpha(alpha);
        return this;
    }


    @NonNull
    @Override
    public Drawable getDrawable() {
        return drawable;
    }

    @Override
    public TextSticker setDrawable(@NonNull Drawable drawable) {
        this.drawable = drawable;
        realBounds.set(0, 0, getWidth(), getHeight());
        textRect.set(0, 0, getWidth(), getHeight());
        return this;
    }


    public TextSticker setGradient(int color1, int color2) {
        Log.e(TAG, "getGradient: "+color1 + " ,, "+color2 );
        if (color1 != 0 && color2 != 0){
         this.color = new int[]{color1, color2};
        Log.e(TAG, "getGradient: "+color );
        textPaint.setShader(new LinearGradient(0f, 0f, width, height, this.color, null,
                Shader.TileMode.CLAMP));
        }
        return this;
    }
     public int getPattern(){
        int textPattern = 0;
        if (pattern != 0){
            textPattern = this.pattern;
        }
        return textPattern;
     }
    public int[] getGradient() {
        return this.color;
    }

        public TextSticker setPattern(int pattern) {
        if (pattern != 0) {
            this.pattern = pattern;

            Bitmap textPatternBitmap = BitmapFactory.decodeResource(context.getResources(), pattern);
            Bitmap scaleBitmap = Bitmap.createScaledBitmap(textPatternBitmap, 1000, 1000, false);
            Shader shader = new BitmapShader(scaleBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            textPaint.setShader(shader);
//            textPaint.setShadowLayer(2f, 0, 100,ContextCompat.getColor(context,R.color.white));


        }
        return this;
    }

    @Override
    public Sticker setDrawable(@NonNull Drawable drawable, float scale) {
        return null;
    }

    @NonNull
    public TextSticker setDrawable(@NonNull Drawable drawable, @Nullable Rect region) {
        this.drawable = drawable;
        realBounds.set(0, 0, getWidth(), getHeight());
        if (region == null) {
            textRect.set(0, 0, getWidth(), getHeight());
        } else {
            textRect.set(region.left, region.top, region.right, region.bottom);
        }
        return this;
    }

    @NonNull
    public TextSticker setTypeface(@Nullable Typeface typeface) {
        textPaint.setTypeface(typeface);
        return this;
    }

    @NonNull
    public Typeface getTypeface() {
        return textPaint.getTypeface();
    }

    @NonNull
    public TextSticker setPaintToOutline(@ColorInt int color, String type) {
        strokeWidth = 10f;
        borderColor = color;
        mType = type;

        return this;
    }
    @NonNull
    public int getBorderColor() {
        return borderColor;
    }
    @NonNull
    public String getBorderType() {
        return this.mType;
    }

    @NonNull
    public TextSticker setTextColor(@ColorInt int color) {

        strokeWidth = 0f;
        textPaint.setColor(color);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
        textPaint.setShader(null);
//       textPaint.setStrokeWidth(strokeWidth);

        return this;
    }


    @NonNull
    public TextSticker setTextAlign(@NonNull Layout.Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    @NonNull
    public TextSticker setMaxTextSize(@Dimension(unit = Dimension.SP) float size) {
//        textPaint.setTextSize(convertSpToPx(size));
        maxTextSizePixels = size;
        textPaint.setTextSize(size);

        return this;
    }

    /**
     * Sets the lower text size limit
     *
     * @param minTextSizeScaledPixels the minimum size to use for text in this view,
     *                                in scaled pixels.
     */
    @NonNull
    public TextSticker setMinTextSize(float minTextSizeScaledPixels) {
        minTextSizePixels = convertSpToPx(minTextSizeScaledPixels);
        return this;
    }

    @NonNull
    public TextSticker setLineSpacing(float multiplier, float add) {
        lineSpacingMultiplier = multiplier;
        lineSpacingExtra = add;
        return this;
    }

    @NonNull
    public TextSticker setText(@Nullable String text) {
        this.text = text;
        return this;
    }

    @Nullable
    public String getText() {
        return text;
    }

    @Nullable
    public AppCompatTextView getview() {
        return textView;
    }



    public StaticLayout getStaticLayout() {
        return staticLayout;
    }

    /**
     * Resize this view's text size with respect to its width and height
     * (minus padding). You should always call this method after the initialization.
     */
    @NonNull
    public TextSticker resizeText() {
        final int availableHeightPixels = textRect.height();

        final int availableWidthPixels = textRect.width();

        final CharSequence text = getText();

        // Safety check
        // (Do not resize if the view does not have dimensions or if there is no text)
        if (text == null
                || text.length() <= 0
                || availableHeightPixels <= 0
                || availableWidthPixels <= 0
                || maxTextSizePixels <= 0) {
            return this;
        }


        float targetTextSizePixels = maxTextSizePixels;
        Log.e(TAG, "maxTextSizePixels: "+maxTextSizePixels );
        int targetTextHeightPixels =
                getTextHeightPixels(text, availableWidthPixels, targetTextSizePixels);
        Log.e(TAG, "maxTextext "+text.length() );

        // Until we either fit within our TextView
        // or we have reached our minimum text size,
        // incrementally try smaller sizes
     while (targetTextHeightPixels > availableHeightPixels
                && targetTextSizePixels > minTextSizePixels) {
         targetTextSizePixels = Math.max(targetTextSizePixels - 2, minTextSizePixels);

         targetTextHeightPixels =
                 getTextHeightPixels(text, availableWidthPixels, targetTextSizePixels);

     }
        int height = 0;
        int width = 0;
        textPaint.setTextSize(targetTextSizePixels);


        String[] value = text.toString().split("\n");
        Log.e(TAG, "resizeTextvalue: "+value );
        Log.e(TAG, "resizeText: "+value.length   + " >>> "+text.length()  );
        Log.e(TAG, "resizeTextvalue: "+value[0]   );
        for (int i = 0; i < value.length; i++) {
            Rect bounds =new Rect();
            textPaint.getTextBounds(value[i], 0, value[i].length(), bounds);
            height = bounds.height() + height;
            if (bounds.width() > width) width = (int) textPaint.measureText(value[i]);
        }

        textPaint.setTextSize(targetTextSizePixels);

        staticLayout = new StaticLayout(this.text, textPaint, textRect.width(), alignment, lineSpacingMultiplier,
                        lineSpacingExtra, true);
        textView =new AppCompatTextView(context);

        return this;
    }


    @NonNull
    public TextSticker resizeTexttt() {
        final int availableHeightPixels = textRect.height();

        final int availableWidthPixels = textRect.width();

        final CharSequence text = getText();

        // Safety check
        // (Do not resize if the view does not have dimensions or if there is no text)
        if (text == null
                || text.length() <= 0
                || availableHeightPixels <= 0
                || availableWidthPixels <= 0
                || maxTextSizePixels <= 0) {
            return this;
        }


        float targetTextSizePixels = maxTextSizePixels;
        Log.e(TAG, "maxTextSizePixels: "+maxTextSizePixels );
        int targetTextHeightPixels =
                getTextHeightPixels(text, availableWidthPixels, targetTextSizePixels);
        Log.e(TAG, "maxTextext "+text.length() );

        // Until we either fit within our TextView
        // or we have reached our minimum text size,
        // incrementally try smaller sizes
     /*   while (targetTextHeightPixels > availableHeightPixels
                && targetTextSizePixels > minTextSizePixels) {

            targetTextSizePixels = Math.max(targetTextSizePixels - 2, minTextSizePixels);

            targetTextHeightPixels =
                    getTextHeightPixels(text, availableWidthPixels, targetTextSizePixels);

        }*/
        int height = 0;
        int width = 0;
        textPaint.setTextSize(targetTextSizePixels);


        String[] value = text.toString().split("\n");
        Log.e(TAG, "resizeTextvalue: "+value );
        Log.e(TAG, "resizeText: "+value.length   + " >>> "+text.length()  );
        Log.e(TAG, "resizeTextvalue: "+value[0]   );
        for (int i = 0; i < value.length; i++) {
            Rect bounds =new Rect();
            textPaint.getTextBounds(value[i], 0, value[i].length(), bounds);
            height = bounds.height() + height;
            if (bounds.width() > width) width = (int) textPaint.measureText(value[i]);
        }

        textPaint.setTextSize(targetTextSizePixels);

        staticLayout = new StaticLayout(this.text, textPaint, textRect.width(), alignment, lineSpacingMultiplier,
                lineSpacingExtra, true);
        textView =new AppCompatTextView(context);

        return this;
    }

    public TextPaint getTextPaint() {
        return textPaint;
    }

    public void resizeDrawable() {
        Rect bounds = new Rect();
        textPaint.getTextBounds(this.text, 0, this.text.length(), bounds);
        float scale = getCurrentScale();
        Bitmap bitmap = Bitmap.createBitmap((int) (bounds.width() * scale), (int) (bounds.height() * scale), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        c.drawARGB(100, 0, 0, 255);
        drawable = new BitmapDrawable(context.getResources(), bitmap);
    }

    public Integer getPaddingText() {
        return paddingText;
    }

    private void setPaddingText(Integer paddingText) {
        this.paddingText = paddingText;
    }

    public Float getTextX() {
        return textX;
    }

    private void setTextX(Float textX) {
        this.textX = textX;
    }


    /**
     * @return lower text size limit, in pixels.
     */
    public float getMinTextSizePixels() {
        return minTextSizePixels;
    }

    /**
     * Sets the text size of a clone of the view's {@link TextPaint} object
     * and uses a {@link StaticLayout} instance to measure the height of the text.
     *
     * @return the height of the text when placed in a view
     * with the specified width
     * * and when the text has the specified size.
     */
    protected int getTextHeightPixels(@NonNull CharSequence source, int availableWidthPixels,
                                      float textSizePixels) {
        textPaint.setTextSize(textSizePixels);
        // It's not efficient to create a StaticLayout instance
        // every time when measuring, we can use StaticLayout.Builder
        // since api 23.
        StaticLayout staticLayout =
                new StaticLayout(source, textPaint, availableWidthPixels, Layout.Alignment.ALIGN_NORMAL,
                        lineSpacingMultiplier, lineSpacingExtra, true);
        return staticLayout.getHeight();
    }

    /**
     * @return the number of pixels which scaledPixels corresponds to on the device.
     */
     float convertSpToPx(float scaledPixels) {
        return scaledPixels * context.getResources().getDisplayMetrics().scaledDensity;
    }

    public int getColor() {
        return textPaint.getColor();
    }

    public Typeface getTypeFace() {
        return textPaint.getTypeface();
    }

    public float getSize() {
        return textPaint.getTextSize();
    }

    @NonNull
    public TextSticker setLetterSpacing(float spacing) {
        textPaint.setLetterSpacing(spacing);
        return this;
    }
    @NonNull
    public Float getLetterLining() {
        return
                lineSpacingMultiplier;
    }

    @NonNull
    public Float getLetterSpacing() {
        return
                textPaint.getLetterSpacing();
    }
}


