package io.github.domi04151309.alwayson.actions.alwayson

import android.app.Activity
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.custom.CustomFrameLayout

class AlwaysOnViewHolder(activity: Activity) {
    @JvmField
    val frame: CustomFrameLayout = activity.findViewById(R.id.frame)

    @JvmField
    val customView: AlwaysOnCustomView = activity.findViewById(R.id.customView)
}
