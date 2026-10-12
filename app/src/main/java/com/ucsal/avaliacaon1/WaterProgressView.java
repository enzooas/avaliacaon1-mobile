
package com.ucsal.avaliacaon1;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

public class WaterProgressView extends View {

    private final int progressColor;
    private final int textColor;
    private int maxValue;

    private int consumedMl = 0;
    private float progress = 0f;

    private ValueAnimator progressAnimator;
    private boolean showAbsolute = false;

    private final RectF oval = new RectF();
    private final Paint paint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    public WaterProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);

        try (TypedArray a = context.obtainStyledAttributes(
                attrs,
                R.styleable.WaterProgressView
        )) {
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

            if (maxValue <= 0) {
                maxValue = 2000;
            }
        }

        setOnClickListener(v -> {
            showAbsolute = !showAbsolute;
            invalidate();
        });
    }

    // Recebe o percentual e anima o progresso.
    public void setProgress(int value) {
        int percentage = Math.max(0, value);

        // Mantém o modo absoluto e o alerta coerentes
        // quando este metodo é chamado diretamente.
        consumedMl = (int) Math.round(
                maxValue * (percentage / 100.0)
        );

        animateProgress(percentage);
    }

    // Atualiza o consumo real e a meta diária.
    public void setConsumption(int consumedMl, int newMaxValue) {
        if (newMaxValue <= 0) {
            return;
        }

        this.consumedMl = Math.max(0, consumedMl);
        this.maxValue = newMaxValue;

        int percentage = (int) Math.round(
                this.consumedMl * 100.0 / this.maxValue
        );

        animateProgress(percentage);
    }

    public void setMaxValue(int value) {
        if (value <= 0) {
            return;
        }

        this.maxValue = value;

        int percentage = (int) Math.round(
                consumedMl * 100.0 / maxValue
        );

        animateProgress(percentage);
    }

    private void animateProgress(int target) {
        if (progressAnimator != null) {
            progressAnimator.cancel();
        }

        progressAnimator = ValueAnimator.ofFloat(
                progress,
                target
        );

        progressAnimator.setDuration(500);

        progressAnimator.addUpdateListener(animation -> {
            progress = (float) animation.getAnimatedValue();
            invalidate();
        });

        progressAnimator.start();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        float centerX = width / 2f;
        float centerY = height / 2f;

        float titleSpace = height * 0.12f;
        float alertSpace = height * 0.12f;

        float availableHeight =
                Math.max(0f, height - titleSpace - alertSpace);

        float radius = Math.max(0f, Math.min(
                width * 0.32f,
                availableHeight * 0.38f
        ));

        float stroke = Math.max(4f, radius * 0.12f);

        boolean exceeded = consumedMl > maxValue;

        // Título.
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(Math.max(8f, radius * 0.20f));
        paint.setStyle(Paint.Style.FILL);

        String title = "Consumo diário de água";

        // Reduz o texto se ele não couber na largura.
        float maxTextWidth = Math.max(0f, width - 16f);
        float titleWidth = paint.measureText(title);

        if (titleWidth > maxTextWidth && titleWidth > 0f) {
            paint.setTextSize(
                    paint.getTextSize() * maxTextWidth / titleWidth
            );
        }

        canvas.drawText(
                title,
                centerX,
                titleSpace / 2f + paint.getTextSize() / 2f + 10f,
                paint
        );

        // Parte restante da meta.
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        paint.setColor(Color.LTGRAY);

        oval.set(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
        );

        canvas.drawArc(oval, -90, 360, false, paint);

        // Progresso realizado.
        paint.setColor(exceeded ? Color.RED : progressColor);

        float sweep = Math.min(Math.max(progress, 0f), 100f) * 3.6f;

        canvas.drawArc(oval, -90, sweep, false, paint);

        // Texto central.
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(exceeded ? Color.RED : textColor);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(Math.max(8f, radius * 0.38f));

        String centerText;

        if (showAbsolute) {
            centerText = consumedMl + "/" + maxValue + " ml";
        } else {
            centerText = Math.round(progress) + "%";
        }


        // Ajusta o texto para caber dentro do círculo.
        float centerTextWidth = paint.measureText(centerText);

        // Usa 70% do diâmetro como largura máxima disponível.
        float maxCenterWidth = radius * 1.4f;

        if (centerTextWidth > maxCenterWidth
                && centerTextWidth > 0f) {
            float novoTamanho = paint.getTextSize()
                    * maxCenterWidth / centerTextWidth;

            paint.setTextSize(Math.max(8f, novoTamanho));
        }

        canvas.drawText(
                centerText,
                centerX,
                centerY + paint.getTextSize() / 3f,
                paint
        );

        // Alerta visual.
        if (exceeded) {
            paint.setColor(Color.RED);
            paint.setTextSize(Math.max(8f, radius * 0.19f));

            String alert = "META ULTRAPASSADA!";
            float alertWidth = paint.measureText(alert);

            if (alertWidth > maxTextWidth && alertWidth > 0f) {
                paint.setTextSize(
                        paint.getTextSize() * maxTextWidth / alertWidth
                );
            }

            canvas.drawText(
                    alert,
                    centerX,
                    height - alertSpace / 2f,
                    paint
            );
        }
    }
}
