package com.android.ar_ruler_kt.opengl

import android.content.Context
import android.opengl.GLES30
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.*

/**
 * @author：TianLong
 * @date：2022/7/7 17:52
 * @detail：
 */
class LineRenderer(context:Context): BaseRenderer(context) {
    override var vertexPath: String = "shader/dottedline_shader.vert"
    override var fragmentPath: String = "shader/dottedline_shader.frag"
    var a_Position = -1
    val vertex = floatArrayOf(
        -0.5f,-1f,
        0.5f,1f
    )

    val vertexBuffer :FloatBuffer by lazy {
        val buffer:ByteBuffer = ByteBuffer.allocateDirect(Float.SIZE_BYTES * vertex.size)
        buffer.order(ByteOrder.nativeOrder())
        val tempBuffer = buffer.asFloatBuffer()
        tempBuffer.put(vertex)
        tempBuffer.position(0)

        return@lazy tempBuffer
    }

    override fun initShaderParameter() {
        a_Position = GLES30.glGetAttribLocation(program,"a_Position")
        GLError.maybeThrowGLException("LineRender", "initShaderParameter")
    }

    override fun onSurfaceCreated() {
        initProgram()
        val uv = FloatArray(48)
        for (i in 0..23) {
            if (i % 2 == 0) {
                uv[i * 2] = 0f
                uv[i * 2 + 1] = -1f
            } else {
                uv[i * 2] = 3f
                uv[i * 2 + 1] = -1f
            }
        }
        Log.e(TAG,"uv:${uv.contentToString()}")
        GLError.maybeThrowGLException("LineRender", "onSurfaceCreated")
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    override fun onDrawFrame() {
        GLES30.glUseProgram(program)
        GLES30.glEnableVertexAttribArray(a_Position)

        GLES30.glVertexAttribPointer(a_Position,2,GLES30.GL_FLOAT,false,0,vertexBuffer)
        GLES30.glDrawArrays(GLES30.GL_LINES,0,2)
        GLES30.glDrawArrays(GLES30.GL_POINTS,0,2)
        GLES30.glLineWidth(2f)
        GLES30.glDisableVertexAttribArray(a_Position)
        GLES30.glUseProgram(0)
        GLError.maybeThrowGLException("LineRender", "onDrawFrame")
    }
}