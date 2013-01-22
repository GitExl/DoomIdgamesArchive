/**
 * Copyright (c) 2012, Dennis Meuwissen
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the FreeBSD Project.
 */

package nl.exl.doomidgamesarchive;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
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
	public class RatingBarException extends Exception {
		private static final long serialVersionUID = -3952282621756694962L;
		
		public RatingBarException(String error) {
			super(error);
		}
	}
	
	
	public RatingView(Context context, AttributeSet attrs) throws RatingBarException {
		super(context, attrs);
		
		// Retrieve view parameters.
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RatingView, 0, 0);
		try {
			mRatingMax = a.getInt(R.styleable.RatingView_ratingMax, 5);
			mRating = a.getFloat(R.styleable.RatingView_rating, (float)2.5);
			mRatingSpacing = a.getInt(R.styleable.RatingView_ratingSpacing, 1);
			
			// Get default drawables so this view is displayed in edit mode.
			if (isInEditMode()) {
				mDrawableEmpty = getResources().getDrawable(R.drawable.rating_skull_empty);
				mDrawableHalf = getResources().getDrawable(R.drawable.rating_skull_half);
				mDrawableFull = getResources().getDrawable(R.drawable.rating_skull_full);
				
			// Get drawables defined in XML.
			} else {
				mDrawableEmpty = a.getDrawable(R.styleable.RatingView_drawableEmpty);
				mDrawableHalf = a.getDrawable(R.styleable.RatingView_drawableHalf);
				mDrawableFull = a.getDrawable(R.styleable.RatingView_drawableFull);
			}
		} finally {
			a.recycle();
		}
		
		// Test if all icon drawables are specified.
		if (mDrawableEmpty == null) {
			throw new RatingBarException("No empty icon drawable specified.");
		}
		if (mDrawableHalf == null) {
			throw new RatingBarException("No half icon drawable specified.");
		}
		if (mDrawableFull == null) {
			throw new RatingBarException("No full icon drawable specified.");
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
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Calculate icon width as minimum width.
		int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth() + (mRatingMax * (mIconWidth + mRatingSpacing));
		int w = resolveSize(minw, widthMeasureSpec);
	   
		// Calculate icon height as minimum height.
		int minh = getPaddingTop() + getPaddingBottom() + getSuggestedMinimumHeight() + mIconHeight;
		int h = resolveSize(minh, heightMeasureSpec);

		setMeasuredDimension(w, h);
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
		Drawable icon = null;
		
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
		mRatingSpacing = Math.max(ratingSpacing, 0);
		
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
}