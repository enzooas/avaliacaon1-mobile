package com.ucsal.avaliacaon1;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import android.graphics.Color;

public class WaterProgressView extends View {

    private int progressColor = Color.BLUE;
    private int textColor = Color.BLACK;
    private int maxValue = 2000;
    private int consumedMl = 0;
    private float progress = 0f;
    private boolean showAbsolute = false;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public WaterProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(
                attrs,
                R.styleable.WaterProgressView
        );

        try {
            progressColor = a.getColor(
                    R.styleable.WaterProgressView_progressColor,
                    Color.BLUE
            );

            textColor = a.getColor(
                    R.styleable.WaterProgressView_textColor,
                    Color.BLACK
            );

            maxValue = a.getInt(
                    R.styleable.WaterProgressView_maxValue,
                    2000
            );
        } finally {
            a.recycle();
        }

        setOnClickListener(v -> {
            showAbsolute = !showAbsolute;
            invalidate();
        });
    }

    public void setProgress(int value) {
        animateProgress(value);
    }

    public void setConsumption(int consumedMl, int newMaxValue) {
        this.consumedMl = Math.max(0, consumedMl);
        setMaxValue(newMaxValue);

        int percentage = maxValue > 0
                ? (int) Math.round(
                consumedMl * 100.0 / maxValue
        )
                : 0;

        animateProgress(percentage);
    }

    public void setMaxValue(int value) {
        if (value <= 0) {
            return;
        }

        maxValue = value;

        int percentage = (int) Math.round(
                consumedMl * 100.0 / maxValue
        );

        animateProgress(percentage);
    }

    private void animateProgress(int target) {
        ValueAnimator animator = ValueAnimator.ofFloat(
                progress,
                target
        );

        animator.setDuration(500);

        animator.addUpdateListener(animation -> {
            progress = (float) animation.getAnimatedValue();
            invalidate();
        });

        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        float radius = Math.min(getWidth(), getHeight()) * 0.32f;
        float stroke = Math.max(8f, radius * 0.12f);

        boolean exceeded = maxValue > 0 && consumedMl > maxValue;

        // Título
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(Math.max(12f, radius * 0.20f));
        paint.setStyle(Paint.Style.FILL);

        canvas.drawText(
                "Consumo diário de água",
                centerX,
                centerY - radius - stroke * 1.5f,
                paint
        );

        // Parte restante da meta
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        paint.setColor(Color.LTGRAY);

        RectF oval = new RectF(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
        );

        canvas.drawArc(oval, -90, 360, false, paint);

        // Progresso realizado
        paint.setColor(exceeded ? Color.RED : progressColor);

        float sweep = Math.min(progress, 100f) * 3.6f;

        canvas.drawArc(oval, -90, sweep, false, paint);

        // Texto central
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(exceeded ? Color.RED : textColor);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(Math.max(16f, radius * 0.38f));

        String centerText;

        if (showAbsolute) {
            centerText = consumedMl + "/" + maxValue + " ml";
        } else {
            centerText = Math.round(progress) + "%";
        }

        canvas.drawText(
                centerText,
                centerX,
                centerY + paint.getTextSize() / 3f,
                paint
        );

        // Alerta visual
        if (exceeded) {
            paint.setTextSize(Math.max(12f, radius * 0.19f));

            canvas.drawText(
                    "META ULTRAPASSADA!",
                    centerX,
                    centerY + radius + stroke * 2f,
                    paint
            );
        }
    }
}
