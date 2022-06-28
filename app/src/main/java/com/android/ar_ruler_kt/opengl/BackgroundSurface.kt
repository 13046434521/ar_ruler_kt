package com.android.ar_ruler_kt.opengl

import android.content.Context
import android.util.AttributeSet
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author：TianLong
 * @date：2022/6/27 23:32
 * @detail：背景渲染类
 */
class BackgroundSurface: GLSurface {
    lateinit var backgroundRenderer:BackgroundRenderer

    constructor(context: Context) : super(context,null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
        initialize(context)
    }

    private fun initialize(context: Context){
        backgroundRenderer = BackgroundRenderer(context)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        backgroundRenderer.onSurfaceCreated()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        backgroundRenderer.onSurfaceChanged(width,height)
    }

    override fun onDrawFrame(gl: GL10?) {
        backgroundRenderer.onDrawFrame()
    }
}