package com.android.ar_ruler_kt.opengl

/**
 * @author：TianLong
 * @date：2022/7/1 15:36
 * @detail：矩阵接口
 */
interface IMatrix {
    var matrix: FloatArray

    fun upDateMatrix(pose:FloatArray = FloatArray(16),viewMatrix:FloatArray = FloatArray(16),projectMatrix:FloatArray = FloatArray(16))
}