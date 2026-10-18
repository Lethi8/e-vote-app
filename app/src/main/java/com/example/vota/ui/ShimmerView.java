package com.example.vota.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class ShimmerView extends View {

    private Paint paint;
    private Matrix gradientMatrix;
    private LinearGradient gradient;
    private float translateFraction = -1f;
    private ValueAnimator animator;

    public ShimmerView(Context context) {
        super(context);
        init();
    }

    public ShimmerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        gradientMatrix = new Matrix();
        setWillNotDraw(false);
    }
//rebuilds the gradient whenever the view sie changes
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0) return;
        gradient = new LinearGradient(
                0, 0, w, 0,
                new int[]{
                        Color.argb(0, 255, 255, 255),
                        Color.argb(100, 255, 255, 255),
                        Color.argb(0, 255, 255, 255)
                },
                new float[]{0f, 0.5f, 1f},
                Shader.TileMode.CLAMP);
        paint.setShader(gradient);
    }
//starts shimmer animation
    public void startShimmer() {
        stopShimmer();
        animator = ValueAnimator.ofFloat(-1f, 2f);
        animator.setDuration(1300);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(a -> {
            translateFraction = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }
// stops and discards animation if one is running
    public void stopShimmer() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }
//draws the gradient band at its current animation position
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (gradient == null) return;
        gradientMatrix.reset();
        gradientMatrix.setTranslate(translateFraction * getWidth(), 0);
        gradient.setLocalMatrix(gradientMatrix);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopShimmer();
        // prevents the animator from leaking or crashing if the screen is closed while a network call is still running
    }
}