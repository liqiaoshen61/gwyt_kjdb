package com.jwch.gwyt_project.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class CompassVerticalView extends androidx.appcompat.widget.AppCompatImageView {
	private float mDirection;
	private Drawable compass;
	private final float GRADUATION_INTERVAL = 7.5f;
	private final int GRADUATION_LENGTH = 2700;

	public CompassVerticalView(Context context) {
		super(context);
		mDirection = 0.0f;
		compass = null;
	}

	public CompassVerticalView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDirection = 0.0f;
		compass = null;
	}

	public CompassVerticalView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mDirection = 0.0f;
		compass = null;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (compass == null) {
			compass = getDrawable();// get the resource of view
			compass.setBounds(0, 0, getWidth(), getHeight());
		}

		canvas.save();
		canvas.translate(-mDirection, 0);
		compass.draw(canvas);
		canvas.restore();
	}

	/**
	 * define the function of direction's update
	 * 
	 * @param direction
	 * 
	 */
	public void updateDirection(float direction) {
		mDirection = direction;
		mDirection = (mDirection * GRADUATION_INTERVAL) % GRADUATION_LENGTH;
		invalidate();
	}

}
