package com.android.ar_ruler_kt.opengl

import android.content.Context
import android.opengl.GLES30

/**
 * @author：TianLong
 * @date：2022/7/7 17:52
 * @detail：
 */
class LineRenderer(context:Context): BaseRenderer(context) {
    override var vertexPath: String = "shader/dottedline_shader.vert"
    override var fragmentPath: String = "shader/dottedline_shader.frag"

    var a_Position = -1
    override fun initShaderParameter() {
        a_Position = GLES30.glGetAttribLocation(program,"a_Position")
    }

    override fun onSurfaceCreated() {
        initProgram()
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    override fun onDrawFrame() {
        GLES30.glUseProgram(program)
        GLES30.glEnableVertexAttribArray(a_Position)



        GLES30.glDisableVertexAttribArray(a_Position)
        GLES30.glUseProgram(0)
    }
}