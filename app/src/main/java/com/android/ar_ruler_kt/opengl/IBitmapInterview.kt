package com.android.ar_ruler_kt.opengl

import android.graphics.Canvas

/**
 * @author：TianLong
 * @date：2022/7/9 0:33
 * @detail：
 */
interface IBitmapInterview {
    val canvas: Canvas
        get() = Canvas()

    fun drawBitmap(){
//        canvas.drawText("")
    }
}