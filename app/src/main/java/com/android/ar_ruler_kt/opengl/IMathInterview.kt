package com.android.ar_ruler_kt.opengl

import android.util.Log
import com.google.ar.core.Pose
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * @author：TianLong
 * @date：2022/7/9 11:57
 * @detail：Math 辅助 接口
 */
interface IMathInterview {
    fun length(pose1: Pose, pose2: Pose): Double {
        val tempX = (pose1.tx() - pose2.tx()).toDouble().pow(2.0)
        val tempY = (pose1.ty() - pose2.ty()).toDouble().pow(2.0)
        val tempZ = (pose1.tz() - pose2.tz()).toDouble().pow(2.0)

        val length = sqrt(tempX + tempY + tempZ)

        Log.w("IMathInterview","$length")
        return length
    }
}