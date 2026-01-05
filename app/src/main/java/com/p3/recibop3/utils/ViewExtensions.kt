package com.p3.recibop3.utils

import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator

/**
 * Applies a "Bounce" animation to the view on touch.
 * Scales down when pressed, and springs back when released.
 * Returns false in onTouch to allow existing OnClickListener to function normally.
 */
fun View.applyBounceAnimation() {
    this.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val scaleX = ObjectAnimator.ofFloat(v, "scaleX", 0.92f)
                val scaleY = ObjectAnimator.ofFloat(v, "scaleY", 0.92f)
                scaleX.duration = 100
                scaleY.duration = 100
                scaleX.start()
                scaleY.start()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1f)
                val scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1f)
                scaleX.duration = 300
                scaleY.duration = 300
                scaleX.interpolator = OvershootInterpolator(3f) // High tension for "spring" effect
                scaleY.interpolator = OvershootInterpolator(3f)
                scaleX.start()
                scaleY.start()
            }
        }
        // Return false to propagate the event to OnClickListener
        false
    }
}

/**
 * Applies a "Pop In" animation for entrance (Scale 0 -> 1 with overshoot).
 */
fun View.animatePopIn(delay: Long = 0) {
    this.alpha = 0f
    this.scaleX = 0f
    this.scaleY = 0f
    
    this.animate()
        .alpha(1f)
        .scaleX(1f)
        .scaleY(1f)
        .setStartDelay(delay)
        .setDuration(400)
        .setInterpolator(OvershootInterpolator(1.5f))
        .start()
}
