package nl.exl.doomidgamesarchive;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

/**
 * Displays a number of icons based on what current rating is given.
 */
public class RatingView extends View {

    // The total rating icons that this view will display.
    private int mRatingMax;
    
    // The amount of filled rating icons that this view will display.
    private float mRating;
    
    // The space that is kept in between rating icons.
    private int mRatingSpacing;

    // Rating icons.
    private Bitmap mBitmapEmpty;
    private Bitmap mBitmapHalf;
    private Bitmap mBitmapFull;

    // Y coordinate to render rating icons at. This is cached when the view's size changes.
    private int mRenderY;

    // Paint used for rendering.
    private Paint mPaint;

    // Rectangles for rendering.
    private Rect mRectSrc = new Rect();
    private Rect mRectDest = new Rect();

    // Cached icon sizes.
    private int mIconDrawableWidth;
    private int mIconDrawableHeight;

    private float mIconScale = 1.0f;

    private boolean mBlend;

    
    /**
     * General exception raised by the RatingView class.
     */
    private class RatingBarException extends Exception {
        private static final long serialVersionUID = -3952282621756694962L;
        
        private RatingBarException(String error) {
            super(error);
        }
    }
    
    
    public RatingView(Context context, AttributeSet attrs) throws RatingBarException {
        super(context, attrs);

        Drawable drawableEmpty;

        // Retrieve view parameters.
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RatingView, 0, 0);
        try {
            mRatingMax = a.getInt(R.styleable.RatingView_ratingMax, 5);
            mRating = a.getFloat(R.styleable.RatingView_rating, 2.5f);
            mRatingSpacing = a.getDimensionPixelSize(R.styleable.RatingView_ratingSpacing, 1);
            mIconScale = a.getFloat(R.styleable.RatingView_scale, mIconScale);
            mBlend = a.getBoolean(R.styleable.RatingView_blend, false);

            mBitmapEmpty = BitmapFactory.decodeResource(getResources(), a.getResourceId(R.styleable.RatingView_drawableEmpty, R.drawable.rating_skull_empty));
            mBitmapHalf = BitmapFactory.decodeResource(getResources(), a.getResourceId(R.styleable.RatingView_drawableHalf, R.drawable.rating_skull_half));
            mBitmapFull = BitmapFactory.decodeResource(getResources(), a.getResourceId(R.styleable.RatingView_drawableFull, R.drawable.rating_skull_full));

            drawableEmpty = ResourcesCompat.getDrawable(getResources(), a.getResourceId(R.styleable.RatingView_drawableEmpty, R.drawable.rating_skull_empty), null);

        } finally {
            a.recycle();
        }
        
        // Test if all icon drawables are specified.
        if (mBitmapEmpty == null || drawableEmpty == null) {
            throw new RatingBarException("No empty icon drawable.");
        }
        if (mBitmapHalf == null) {
            throw new RatingBarException("No half icon drawable.");
        }
        if (mBitmapFull == null) {
            throw new RatingBarException("No full icon drawable.");
        }

        mIconDrawableWidth = (int)(drawableEmpty.getIntrinsicWidth() * mIconScale);
        mIconDrawableHeight = (int)(drawableEmpty.getIntrinsicHeight() * mIconScale);

        mPaint = new Paint();
        if (mBlend) {
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
        } else {
            mPaint.setXfermode(null);
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {     
        final int desiredWidth = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth() + (mRatingMax * (mIconDrawableWidth + mRatingSpacing));
        final int desiredHeight = getPaddingTop() + getPaddingBottom() + getSuggestedMinimumHeight() + mIconDrawableHeight;
        
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        
        int width = 0;
        int height = 0;
        
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else if (widthMode == MeasureSpec.UNSPECIFIED) {
            width = desiredWidth;
        }
        
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        // Render icons vertically centered.
        mRenderY = h / 2 - mIconDrawableHeight / 2;
    };
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Bitmap icon;

        int iconWidth = (int)(mBitmapEmpty.getScaledWidth(canvas) * mIconScale);
        int iconHeight = (int)(mBitmapEmpty.getScaledHeight(canvas) * mIconScale);

        mRectSrc.left = 0;
        mRectSrc.top = 0;
        mRectSrc.right = mBitmapEmpty.getWidth();
        mRectSrc.bottom = mBitmapEmpty.getHeight();

        mRectDest.left = 0;
        mRectDest.top = mRenderY;
        mRectDest.right = iconWidth;
        mRectDest.bottom = mRenderY + iconHeight;

        for (int i = 0; i < mRatingMax; i++) {
            
            // Select what icon drawable to use depending on the rating.
            if (mRating - i <= 0) {
                icon = mBitmapEmpty;
            } else if (mRating - i <= 0.5) {
                icon = mBitmapHalf;
            } else {
                icon = mBitmapFull;
            }

            canvas.drawBitmap(icon, mRectSrc, mRectDest, mPaint);

            mRectDest.left += iconWidth + mRatingSpacing;
            mRectDest.right = mRectDest.left + iconWidth;
        }
    }
    
    public void setRating(float rating) {
        mRating = rating;
        
        invalidate();
        requestLayout();
    }
    
    /**
     * Sets the spacing to use between rating icons. A spacing below 0 will be treated as 0.
     */
    public void setRatingSpacing(int ratingSpacing) {
        mRatingSpacing = dpToPx(Math.max(ratingSpacing, 0));
        
        invalidate();
        requestLayout();
    }
    
    /**
     * Sets the maximum rating value. This determines how many rating icons will be drawn. A ratingMax below 0 will be treated as 0.
     */
    public void setRatingMax(int ratingMax) {
        mRatingMax = ratingMax;
        
        // Maximum rating is 0 or higher.
        mRating = Math.min(ratingMax, mRating);
        
        invalidate();
        requestLayout();
    }
    
    /**
     * Returns a Device Pixels value as pixels.
     * 
     * @param dp The device pixels value to convert.
     * 
     * @return The dp parameter converted to pixels.
     */
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float)dp * density);
    }
}