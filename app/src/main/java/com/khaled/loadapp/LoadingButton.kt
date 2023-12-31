package com.khaled.loadapp

import android.animation.AnimatorInflater
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var progress = 0.0
    private var valueAnimator = ValueAnimator()

    private var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { p, old, new ->
    }

    private val rect = RectF()
    private val textBoundRect = Rect()

    private val updateListener = ValueAnimator.AnimatorUpdateListener {
        progress = (it.animatedValue as Float).toDouble()
        invalidate()
        if (progress == 100.0) {
            onDownloadComplete()
        }
    }

    fun onDownloadComplete() {
        valueAnimator.cancel()
        buttonState = ButtonState.Completed
        invalidate()
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 64.0f
        typeface = Typeface.create("", Typeface.BOLD)
        color = resources.getColor(R.color.colorPrimary)
    }


    init {
        isClickable = true
        valueAnimator = AnimatorInflater.loadAnimator(
            context,
            R.animator.loading_animation
        ) as ValueAnimator
        valueAnimator.addUpdateListener(updateListener)
    }

    override fun performClick(): Boolean {
        super.performClick()
        if (buttonState == ButtonState.Completed) buttonState = ButtonState.Loading
        valueAnimator.start()
        return true
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        //set the text
        val buttonText =
            if (buttonState == ButtonState.Loading) resources.getString(R.string.button_loading)
            else resources.getString(R.string.download)

        //drawing base
        canvas?.drawRect(0.0f, 0.0f, width.toFloat(), height.toFloat(), paint)

        //drawing loading
        if (buttonState == ButtonState.Loading) {
            paint.color = resources.getColor(R.color.colorPrimaryDark)
            canvas?.drawRect(
                0f, 0f,
                (widthSize * (progress / 100)).toFloat(), height.toFloat(), paint
            )

            paint.getTextBounds(buttonText, 0, buttonText.length, textBoundRect)
            val centerX = measuredWidth.toFloat() / 2

            //drawing circle
            paint.color = resources.getColor(R.color.colorAccent)
            rect.set(
                centerX + textBoundRect.right / 2 + 50.0f,
                40.0f,
                centerX + textBoundRect.right / 2 + 100.0f,
                measuredHeight.toFloat() - 40.0f
            )
            canvas?.drawArc(
                rect,
                0f, (360 * (progress / 100)).toFloat(),
                true,
                paint
            )

        }
        //redraw base when loading done
        else if (buttonState == ButtonState.Completed) {
            paint.color = resources.getColor(R.color.colorPrimary)
            canvas?.drawRect(
                0f, 0f,
                (widthSize * (progress / 100)).toFloat(), heightSize.toFloat(), paint
            )
        }

        //draw text over button
        paint.color = Color.WHITE
        canvas?.drawText(buttonText, (width / 2).toFloat(), ((height + 30) / 2).toFloat(), paint)

        //reset paint color
        paint.color = resources.getColor(R.color.colorPrimary)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        widthSize = w
        heightSize = h
    }
}