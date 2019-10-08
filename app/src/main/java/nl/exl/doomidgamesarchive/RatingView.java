package nl.exl.doomidgamesarchive;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.core.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.View;

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

    // Rating icon drawables.
    private Drawable mDrawableEmpty;
    private Drawable mDrawableHalf;
    private Drawable mDrawableFull;
    
    // Dimensions of the rating icons. This is cached when the view is constructed.
    private int mIconWidth;
    private int mIconHeight;
    
    // Y coordinate to render rating icons at. This is cached when the view's size changes.
    private int mRenderY;

    
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

        float iconScale = 1.0f;
        
        // Retrieve view parameters.
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RatingView, 0, 0);
        try {
            mRatingMax = a.getInt(R.styleable.RatingView_ratingMax, 5);
            mRating = a.getFloat(R.styleable.RatingView_rating, 2.5f);
            mRatingSpacing = a.getDimensionPixelSize(R.styleable.RatingView_ratingSpacing, 1);
            iconScale = a.getFloat(R.styleable.RatingView_scale, iconScale);
            
            mDrawableEmpty = a.getDrawable(R.styleable.RatingView_drawableEmpty);
            mDrawableHalf = a.getDrawable(R.styleable.RatingView_drawableHalf);
            mDrawableFull = a.getDrawable(R.styleable.RatingView_drawableFull);

            // Use default drawables if none were specified.
            if (mDrawableEmpty == null) {
                mDrawableEmpty = ResourcesCompat.getDrawable(getResources(), R.drawable.rating_skull_empty, null);
            }
            if (mDrawableHalf == null) {
                mDrawableHalf = ResourcesCompat.getDrawable(getResources(), R.drawable.rating_skull_half, null);
            }
            if (mDrawableFull == null) {
                mDrawableFull = ResourcesCompat.getDrawable(getResources(), R.drawable.rating_skull_full, null);
            }

        } finally {
            a.recycle();
        }
        
        // Test if all icon drawables are specified.
        if (mDrawableEmpty == null) {
            throw new RatingBarException("No empty icon drawable.");
        }
        if (mDrawableHalf == null) {
            throw new RatingBarException("No half icon drawable.");
        }
        if (mDrawableFull == null) {
            throw new RatingBarException("No full icon drawable.");
        }
        
        // Cache drawables size.
        mIconWidth = mDrawableEmpty.getIntrinsicWidth();
        mIconHeight = mDrawableEmpty.getIntrinsicHeight();
        
        // Test whether all icon drawables are of the same width and height.
        if (mIconWidth != mDrawableHalf.getIntrinsicWidth() ||
                mIconHeight != mDrawableHalf.getIntrinsicHeight() ||
                mIconWidth != mDrawableFull.getIntrinsicWidth() ||
                mIconHeight != mDrawableFull.getIntrinsicHeight()) {
            throw new RatingBarException("Icon drawables are not of equal width and height.");
        }

        mIconWidth *= iconScale;
        mIconHeight *= iconScale;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {     
        final int desiredWidth = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth() + (mRatingMax * (mIconWidth + mRatingSpacing));
        final int desiredHeight = getPaddingTop() + getPaddingBottom() + getSuggestedMinimumHeight() + mIconHeight;
        
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
        mRenderY = h / 2 - mIconHeight / 2;
    };
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int x = 0;
        Drawable icon;
        
        // Draw all rating icons.
        for (int i = 0; i < mRatingMax; i++) {
            
            // Select what icon drawable to use depending on the rating.
            if (mRating - i <= 0) {
                icon = mDrawableEmpty;
            } else if (mRating - i <= 0.5) {
                icon = mDrawableHalf;
            } else {
                icon = mDrawableFull;
            }
            
            // Place and render the icon.
            icon.setBounds(x, mRenderY, x + mIconWidth, mRenderY + mIconHeight);
            icon.draw(canvas);
            
            // Increase x position by icon width and spacing.
            x += mIconWidth + mRatingSpacing;
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