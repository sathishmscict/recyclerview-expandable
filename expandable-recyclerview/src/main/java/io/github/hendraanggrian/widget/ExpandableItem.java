/***********************************************************************************
 * The MIT License (MIT)
 * Copyright (c) 2014 Robin Chutaux
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ***********************************************************************************/

package io.github.hendraanggrian.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class ExpandableItem extends RelativeLayout {

    static final String TAG = "io.github.hendraanggrian.widget.ExpandableItem";

    private Boolean isAnimationRunning = false;
    private Boolean isOpened = false;
    private Integer duration;
    private FrameLayout contentLayout;
    private FrameLayout headerLayout;
    private Boolean closeByUser = true;

    public ExpandableItem(Context context) {
        this(context, null);
    }

    public ExpandableItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandableItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        final View rootView = View.inflate(context, R.layout.view_expandable, this);
        headerLayout = (FrameLayout) rootView.findViewById(R.id.view_expandable_header);
        contentLayout = (FrameLayout) rootView.findViewById(R.id.view_expandable_content);
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableItem);
        final int headerID = typedArray.getResourceId(R.styleable.ExpandableItem_layoutHeader, -1);
        final int contentID = typedArray.getResourceId(R.styleable.ExpandableItem_layoutContent, -1);
        duration = typedArray.getInt(R.styleable.ExpandableItem_duration, getContext().getResources().getInteger(android.R.integer.config_shortAnimTime));
        typedArray.recycle();

        if (headerID == -1 || contentID == -1)
            throw new IllegalArgumentException("HeaderLayout and ContentLayout cannot be null!");

        if (isInEditMode())
            return;

        final View headerView = View.inflate(context, headerID, null);
        headerView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        headerLayout.addView(headerView);
        setTag(TAG);
        final View contentView = View.inflate(context, contentID, null);
        contentView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        contentLayout.addView(contentView);
        contentLayout.setVisibility(GONE);

        headerLayout.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isOpened() && event.getAction() == MotionEvent.ACTION_UP) {
                    hide();
                    closeByUser = true;
                }

                return isOpened() && event.getAction() == MotionEvent.ACTION_DOWN;
            }
        });
    }

    private void expand(final View v) {
        isOpened = true;
        v.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();
        v.getLayoutParams().height = 0;
        v.setVisibility(VISIBLE);

        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = (interpolatedTime == 1) ? LayoutParams.WRAP_CONTENT : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        animation.setDuration(duration);
        v.startAnimation(animation);
    }

    private void collapse(final View v) {
        isOpened = false;
        final int initialHeight = v.getMeasuredHeight();
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                    isOpened = false;
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        animation.setDuration(duration);
        v.startAnimation(animation);
    }

    public void hideNow() {
        contentLayout.getLayoutParams().height = 0;
        contentLayout.invalidate();
        contentLayout.setVisibility(View.GONE);
        isOpened = false;
    }

    public void showNow() {
        if (!isOpened) {
            contentLayout.setVisibility(VISIBLE);
            isOpened = true;
            contentLayout.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
            contentLayout.invalidate();
        }
    }

    public boolean isOpened() {
        return isOpened;
    }

    public void show() {
        if (!isAnimationRunning) {
            expand(contentLayout);
            isAnimationRunning = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isAnimationRunning = false;
                }
            }, duration);
        }
    }

    public FrameLayout getHeaderLayout() {
        return headerLayout;
    }

    public FrameLayout getContentLayout() {
        return contentLayout;
    }

    public void hide() {
        if (!isAnimationRunning) {
            collapse(contentLayout);
            isAnimationRunning = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isAnimationRunning = false;
                }
            }, duration);
        }
        closeByUser = false;
    }

    public boolean isClosedByUser() {
        return closeByUser;
    }
}