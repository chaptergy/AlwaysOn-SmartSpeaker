package io.github.domi04151309.alwayson.helpers

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.preference.PreferenceManager

class AnimationHelper() {
    private val animationHandler = Handler(Looper.getMainLooper())

    fun animate(
        view: View,
        positionX: Float,
        positionY: Float,
        duration: Int,
    ) {
        if (positionX == view.translationX && positionY == view.translationY) return

        if (
            PreferenceManager.getDefaultSharedPreferences(view.context)
                .getBoolean(P.ANIMATE_MOTION, P.ANIMATE_MOTION_DEFAULT)
        ) {
            var realFrame = 1
            val startPositionX = view.translationX
            val startPositionY = view.translationY
            val numberOfFrames =
                (duration / MILLISECONDS_PER_SECOND * FRAME_RATE).coerceAtLeast(1)
            val movementXPerFrame =
                (positionX - startPositionX) / numberOfFrames
            val movementYPerFrame =
                (positionY - startPositionY) / numberOfFrames

            for (preparedFrame in 1..numberOfFrames) {
                animationHandler.postDelayed({
                    view.translationX = startPositionX + realFrame * movementXPerFrame
                    view.translationY = startPositionY + realFrame * movementYPerFrame
                    realFrame++
                }, MILLISECONDS_PER_SECOND / FRAME_RATE * preparedFrame)
            }
        } else {
            view.translationX = positionX
            view.translationY = positionY
        }
    }

    companion object {
        private const val MILLISECONDS_PER_SECOND: Long = 1000
        private const val FRAME_RATE: Int = 15
    }
}
