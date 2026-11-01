package io.github.domi04151309.alwayson.helpers

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.icu.util.Calendar
import io.github.domi04151309.alwayson.R

class Rules(context: Context) {
    private var start = 0L
    private var end = 0L

    init {
        val startString = P.getPreferences(context).getString("rules_time_start", "0:00") ?: "0:00"
        val endString = P.getPreferences(context).getString("rules_time_end", "0:00") ?: "0:00"
        val startCalendar =
            Calendar.getInstance().apply {
                set(Calendar.MILLISECOND, 0)
                set(Calendar.MILLISECOND, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MINUTE, startString.substringAfter(":").toInt())
                set(Calendar.HOUR_OF_DAY, startString.substringBefore(":").toInt())
            }
        val endCalendar =
            Calendar.getInstance().apply {
                set(Calendar.MILLISECOND, 0)
                set(Calendar.MILLISECOND, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MINUTE, endString.substringAfter(":").toInt())
                set(Calendar.HOUR_OF_DAY, endString.substringBefore(":").toInt())
            }
        if (startCalendar.after(endCalendar)) endCalendar.add(Calendar.DATE, 1)
        start = startCalendar.timeInMillis
        end = endCalendar.timeInMillis
    }

    private fun isInTimePeriod(): Boolean =
        if (start == end) {
            true
        } else {
            System.currentTimeMillis() in start + 1 until end
        }

    fun millisTillEnd(): Long =
        if (start == end) {
            -1
        } else {
            end - System.currentTimeMillis()
        }

    fun canShow(context: Context): Boolean =
        isAlwaysOnDisplayEnabled(context) &&
            matchesDoNotDisturbState(context) &&
            isInTimePeriod()

    companion object {
        fun matchesDoNotDisturbState(context: Context): Boolean {
            val ruleDisableInDoNotDisturb =
                P.getPreferences(context).getBoolean(
                    P.RULES_DISABLE_IN_DO_NOT_DISTURB,
                    P.RULES_DISABLE_IN_DO_NOT_DISTURB_DEFAULT,
                )
            if (!ruleDisableInDoNotDisturb) return true
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL
        }

        fun isAlwaysOnDisplayEnabled(context: Context): Boolean =
            P.getPreferences(context).getBoolean(
                P.ALWAYS_ON,
                P.ALWAYS_ON_DEFAULT,
            )

        fun isAmbientMode(context: Context): Boolean =
            P.getPreferences(context).getBoolean(
                "rules_ambient_mode",
                false,
            )
    }
}
