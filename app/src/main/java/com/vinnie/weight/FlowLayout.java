package com.vinnie.weight;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vinnie
 */
public class FlowLayout extends ViewGroup {

    protected List<List<View>> mAllViews = new ArrayList<>();
    protected List<Integer> mLineHeight = new ArrayList<>();
    private String mGravity;
    private float gapX = 0;
    private float gapY = 0;

    public FlowLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
        mGravity = a.getString(R.styleable.FlowLayout_FLGravity);
        gapX = a.getDimension(R.styleable.FlowLayout_FLGapX, 0);
        gapY = a.getDimension(R.styleable.FlowLayout_FLGapY, 0);
        if (mGravity == null) {
            mGravity = getResources().getString(R.string.gravity_left);
        }
        a.recycle();
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context) {
        this(context, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        int width = 0;
        int height = 0;

        int lineWidth = 0;
        int lineHeight = 0;

        int cCount = getChildCount();

        for (int i = 0; i < cCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                if (i == cCount - 1) {
                    width = Math.max(lineWidth, width);
                    height += lineHeight;
                }
                continue;
            }
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            MarginLayoutParams lp = (MarginLayoutParams) child
                    .getLayoutParams();

            int childWidth = child.getMeasuredWidth() + lp.leftMargin
                    + lp.rightMargin;

            int childHeight = child.getMeasuredHeight() + lp.topMargin
                    + lp.bottomMargin;

            if (lineWidth + childWidth + gapX > sizeWidth - getPaddingLeft() - getPaddingRight()) {
                width = Math.max(width, lineWidth);
                lineWidth = childWidth;
                if (height > 0) {
                    height += gapY;
                }
                height += lineHeight;
                lineHeight = childHeight;
            } else {
                lineWidth += childWidth;
                if (i > 0) {
                    lineWidth += gapX;
                }
                lineHeight = Math.max(lineHeight, childHeight);
            }
            if (i == cCount - 1) {
                width = Math.max(lineWidth, width);
                if (height > 0) {
                    height += gapY;
                }
                height += lineHeight;
            }
        }
        setMeasuredDimension(
                //
                modeWidth == MeasureSpec.EXACTLY ? sizeWidth : width + getPaddingLeft() + getPaddingRight(),
                modeHeight == MeasureSpec.EXACTLY ? sizeHeight : height + getPaddingTop() + getPaddingBottom()//
        );

    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mAllViews.clear();
        mLineHeight.clear();

        int width = getWidth();

        int lineWidth = 0;
        int lineHeight = 0;

        List<View> lineViews = new ArrayList<View>();

        int cCount = getChildCount();

        for (int i = 0; i < cCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }
            MarginLayoutParams lp = (MarginLayoutParams) child
                    .getLayoutParams();

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            if (childWidth + lineWidth + lp.leftMargin + lp.rightMargin + gapX > width - getPaddingLeft() - getPaddingRight() && lineViews.size() > 0) {
                mLineHeight.add(lineHeight);
                mAllViews.add(lineViews);

                lineWidth = 0;
                lineHeight = childHeight + lp.topMargin + lp.bottomMargin;
                lineViews = new ArrayList<>();
            }

            if (lineWidth > 0) {
                lineWidth += gapX;
            }

            lineWidth += childWidth + lp.leftMargin + lp.rightMargin;
            lineHeight = Math.max(lineHeight, childHeight + lp.topMargin
                    + lp.bottomMargin);
            lineViews.add(child);

        }
        mLineHeight.add(lineHeight);
        mAllViews.add(lineViews);


        int left = getPaddingLeft();
        int top = getPaddingTop();
        int gapHCount = 0;

        int lineNum = mAllViews.size();

        for (int i = 0; i < lineNum; i++) {
            lineViews = mAllViews.get(i);
            lineHeight = mLineHeight.get(i);

            left = getStartLeft(lineViews);
            for (int j = 0; j < lineViews.size(); j++) {
                View child = lineViews.get(j);
                if (child.getVisibility() == View.GONE) {
                    continue;
                }

                if (j > 0) {
                    left += gapX;
                }

                MarginLayoutParams lp = (MarginLayoutParams) child
                        .getLayoutParams();

                int lc = left + lp.leftMargin;
                int tc = top + lp.topMargin + gapHCount;
                int rc = lc + child.getMeasuredWidth();
                int bc = tc + child.getMeasuredHeight();

                child.layout(lc, tc, rc, bc);

                left += child.getMeasuredWidth() + lp.leftMargin
                        + lp.rightMargin;
            }

            gapHCount += gapY;
            top += lineHeight;
        }

    }

    private int getStartLeft(List<View> lineViews) {
        int left = getPaddingLeft();

        int needWidth = 0;
        for (int j = 0; j < lineViews.size(); j++) {
            View child = lineViews.get(j);
            if (child.getVisibility() == View.GONE) {
                continue;
            }

            MarginLayoutParams lp = (MarginLayoutParams) child
                    .getLayoutParams();
            needWidth += child.getMeasuredWidth() + lp.leftMargin
                    + lp.rightMargin;

            if (j > 0) {
                needWidth += gapX;
            }
        }

        needWidth += getPaddingLeft() + getPaddingRight();
        if (mGravity.equals(getResources().getString(R.string.gravity_center))) {
            if (getMeasuredWidth() > needWidth) {
                left += (getMeasuredWidth() - needWidth) / 2;
            }
        } else if (mGravity.equals(getResources().getString(R.string.gravity_right))) {
            if (getMeasuredWidth() > needWidth) {
                left += getMeasuredWidth() - needWidth;
            }
        }
        return left;
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }
}