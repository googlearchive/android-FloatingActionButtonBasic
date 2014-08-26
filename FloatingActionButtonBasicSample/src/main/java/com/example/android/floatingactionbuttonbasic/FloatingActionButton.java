/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.floatingactionbuttonbasic;

import com.example.android.common.logger.Log;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Outline;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.FrameLayout;

/**
 * A Floating Action Button is a {@link android.widget.Checkable} view distinguished by a circled
 * icon floating above the UI, with special motion behaviors.
 */
public class FloatingActionButton extends FrameLayout implements Checkable {

    /**
     * An array of states.
     */
    private static final int[] CHECKED_STATE_SET = {
            android.R.attr.state_checked
    };

    private static String TAG = "FloatingActionButton";

    /**
     * A boolean that tells if the FAB is checked or not.
     */
    protected boolean mChecked;

    /*/
     * The {@link View} that is revealed.
     */
    protected View mRevealView;

    /**
     * The coordinates of a touch action.
     */
    protected Point mTouchPoint;

    /**
     * A {@link android.view.GestureDetector} to detect touch actions.
     */
    private GestureDetector mGestureDetector;

    /**
     * A listener to communicate that the FAB has changed its state.
     */
    private OnCheckedChangeListener mOnCheckedChangeListener;

    public FloatingActionButton(Context context) {
        this(context, null, 0, 0);
    }

    public FloatingActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr);

        // When a view is clickable it will change its state to "pressed" on every click.
        setClickable(true);

        // Create a {@link GestureDetector} to detect single taps.
        mGestureDetector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        mTouchPoint = new Point((int) e.getX(), (int) e.getY());
                        Log.d(TAG, "Single tap captured.");
                        toggle();
                        return true;
                    }
                }
        );

        // A new {@link View} is created
        mRevealView = new View(context);
        addView(mRevealView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
    }

    /**
     * Sets the checked/unchecked state of the FAB.
     * @param checked
     */
    public void setChecked(boolean checked) {
        // If trying to set the current state, ignore.
        if (checked == mChecked) {
            return;
        }
        mChecked = checked;

        // Create and start the {@link ValueAnimator} that shows the new state.
        ValueAnimator anim = createAnimator();
        anim.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        anim.start();

        // Set the new background color of the {@link View} to be revealed.
        mRevealView.setBackgroundColor(
                mChecked ? getResources().getColor(R.color.fab_color_2)
                        : getResources().getColor(R.color.fab_color_1)
        );

        // Show the {@link View} to be revealed. Note that the animation has started already.
        mRevealView.setVisibility(View.VISIBLE);

        if (mOnCheckedChangeListener != null) {
            mOnCheckedChangeListener.onCheckedChanged(this, checked);
        }
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
        setClickable(listener != null);
    }

    /**
     * Interface definition for a callback to be invoked when the checked state
     * of a compound button changes.
     */
    public static interface OnCheckedChangeListener {

        /**
         * Called when the checked state of a FAB has changed.
         *
         * @param fabView   The FAB view whose state has changed.
         * @param isChecked The new checked state of buttonView.
         */
        void onCheckedChanged(FloatingActionButton fabView, boolean isChecked);
    }

    protected ValueAnimator createAnimator() {

        // Calculate the longest distance from the hot spot to the edge of the circle.
        int endRadius = getWidth() / 2 +
                (int) Math.hypot(getWidth() / 2 - mTouchPoint.y, getWidth() / 2 - mTouchPoint.x);

        // Make sure the touch point is defined or set it to the middle of the view.
        if (mTouchPoint == null) {
            mTouchPoint = new Point(getWidth() / 2, getHeight() / 2);
        }
        ValueAnimator anim = ViewAnimationUtils.createCircularReveal(
                mRevealView, mTouchPoint.x, mTouchPoint.y, 0, endRadius);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Now we can refresh the drawable state
                refreshDrawableState();

                mRevealView.setVisibility(View.GONE);
                // Reset the touch point as the next call to {@link setChecked} might not come
                // from a tap.
                mTouchPoint = null;
            }
        });
        return anim;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        Outline outline = new Outline();
        outline.setOval(0, 0, w, h);
        setOutline(outline);
        setClipToOutline(true);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }
}
