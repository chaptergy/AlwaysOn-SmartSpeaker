package io.github.domi04151309.alwayson.actions.alwayson

import android.app.Activity
import android.util.Log
import kotlin.math.abs
import kotlin.random.Random
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.helpers.AnimationHelper
import io.github.domi04151309.alwayson.helpers.Global
import io.github.domi04151309.alwayson.helpers.P

class AlwaysOnAnimationThread(
    private val activity: Activity,
    private val viewHolder: AlwaysOnViewHolder,
) : Thread() {
    private val animationHelper = AnimationHelper()
    private val animationDelay =
        P.getPreferences(activity).getInt(
            "ao_animation_delay",
            2,
        ) * MILLISECONDS_PER_MINUTE + ANIMATION_DURATION + MILLISECONDS_PER_SECOND
    private val screenEdgeMarginPx: Float
        get() = activity.resources.getDimension(R.dimen.screen_edge_margin)
    private var screenSizeX: Float = 0F
    private var screenSizeY: Float = 0F
    private var minX: Float = 0F
    private var minY: Float = 0F
    private var maxX: Float = 0F
    private var maxY: Float = 0F
    private var currentX: Float = 0F
    private var currentY: Float = 0F
    private var directionX: Float = 0F
    private var directionY: Float = 0F

    fun updateScreenSize() {
        screenSizeX = viewHolder.frame.width.toFloat()
        screenSizeY = viewHolder.frame.height.toFloat()

        val view = viewHolder.customView
        minX = screenEdgeMarginPx - (view.left + view.pivotX +
                (view.contentOffsetX - view.pivotX) * view.scaleX)
        minY = screenEdgeMarginPx - (view.top + view.pivotY +
                (view.contentOffsetY - view.pivotY) * view.scaleY)
        maxX = screenSizeX - screenEdgeMarginPx - (view.left + view.pivotX + (
                view.contentOffsetX + view.contentWidth - view.pivotX
                ) * view.scaleX)
        maxY = screenSizeY - screenEdgeMarginPx - (view.top + view.pivotY + (
                view.contentHeight - view.pivotY
                ) * view.scaleY)
    }

    private fun preAnimation() {
        while (viewHolder.customView.height == 0 || viewHolder.customView.contentWidth == 0f) {
            sleep(TINY_DELAY)
        }
        updateScreenSize()
        currentX = (minX + maxX) / 2f
        currentY = (minY + maxY) / 2f
        chooseRandomDirection()
        activity.runOnUiThread {
            viewHolder.customView.translationX = currentX
            viewHolder.customView.translationY = currentY
        }
    }

    private fun animationCycle() {
        sleep(animationDelay)
        val nextPosition = nextPosition()
        currentX = nextPosition.first
        currentY = nextPosition.second
        activity.runOnUiThread {
            animationHelper.animate(
                viewHolder.customView,
                currentX,
                currentY,
                ANIMATION_DURATION,
            )
        }
    }

    private fun chooseRandomDirection() {
        do {
            directionX = if (maxX - minX > MIN_DIRECTION_COMPONENT) {
                Random.nextFloat() * 2f - 1f
            } else {
                0f
            }
            directionY = if (maxY - minY > MIN_DIRECTION_COMPONENT) {
                Random.nextFloat() * 2f - 1f
            } else {
                0f
            }
        } while (
            ((maxX - minX > MIN_DIRECTION_COMPONENT ||
                maxY - minY > MIN_DIRECTION_COMPONENT) &&
                abs(directionX) < MIN_DIRECTION_COMPONENT &&
                abs(directionY) < MIN_DIRECTION_COMPONENT) ||
            (currentX <= minX && directionX < 0f) ||
            (currentX >= maxX && directionX > 0f) ||
            (currentY <= minY && directionY < 0f) ||
            (currentY >= maxY && directionY > 0f)
        )
    }

    private fun nextPosition(): Pair<Float, Float> {
        currentX = currentX.coerceIn(minX, maxX)
        currentY = currentY.coerceIn(minY, maxY)

        if (
            (currentX <= minX && directionX < 0f) ||
            (currentX >= maxX && directionX > 0f) ||
            (currentY <= minY && directionY < 0f) ||
            (currentY >= maxY && directionY > 0f)
        ) {
            chooseRandomDirection()
        }

        val nextX = (currentX + directionX * MOVEMENT_STEP_PX).coerceIn(minX, maxX)
        val nextY = (currentY + directionY * MOVEMENT_STEP_PX).coerceIn(minY, maxY)
        return nextX to nextY
    }

    override fun run() {
        try {
            preAnimation()
            while (!isInterrupted) {
                animationCycle()
            }
        } catch (exception: InterruptedException) {
            Log.w(Global.LOG_TAG, exception.toString())
        }
    }

    companion object {
        private const val TINY_DELAY: Long = 10
        private const val MILLISECONDS_PER_SECOND: Long = 1_000
        private const val MILLISECONDS_PER_MINUTE: Long = 60_000
        private const val ANIMATION_DURATION: Int = 10_000
        private const val MIN_DIRECTION_COMPONENT: Float = 0.001f
        private const val MOVEMENT_STEP_PX: Float = 50F
    }
}
