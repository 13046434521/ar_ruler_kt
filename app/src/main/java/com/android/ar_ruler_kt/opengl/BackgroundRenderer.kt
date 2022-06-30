package com.android.ar_ruler_kt.opengl

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES30
import android.util.Log
import com.android.ar_ruler_kt.helper.DisplayRotationHelper
import com.google.ar.core.Coordinates2d
import com.google.ar.core.Session
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * @author：TianLong
 * @date：2022/6/27 23:31
 * @detail：背景 渲染 Renderer
 */
class BackgroundRenderer(context : Context) : BaseRenderer(context) {
    override var fragmentPath: String = "shader/background_show_camera.frag"
    override var vertexPath: String = "shader/background_show_camera.vert"
//    override var fragmentPath: String = "shader/RgbShader.frag"
//    override var vertexPath: String = "shader/RgbShader.vert"


    var u_CameraColorTexture = -1

    var a_Position = -1
    var a_CameraTexCoord = -1
    val vertexCoords: FloatBuffer by lazy {
         val vertexBuffer: ByteBuffer = ByteBuffer.allocateDirect(quadCoords.size * 4)
         vertexBuffer.order(ByteOrder.nativeOrder())
         val vertexCoord = vertexBuffer.asFloatBuffer()
         vertexCoord.put(quadCoords).position(0)

         return@lazy vertexCoord
     }
    val textureCoords: FloatBuffer by lazy {
        val textureBuffer: ByteBuffer = ByteBuffer.allocateDirect(8 * 4)
        textureBuffer.order(ByteOrder.nativeOrder())
        val textureCoord = textureBuffer.asFloatBuffer()
        textureCoord.position(0)
        return@lazy textureCoord
    }

    var width = -1
    var height = -1
    //默认顶点坐标
    private val quadCoords = floatArrayOf(
        -1.0f, -1.0f, -1.0f, +1.0f, +1.0f, -1.0f, +1.0f, +1.0f
    )

    init {
        textureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES
    }

    override fun initShaderParameter() {
        u_CameraColorTexture =  GLES30.glGetUniformLocation(program,"u_CameraColorTexture")
        a_Position =  GLES30.glGetAttribLocation(program,"a_Position")
        a_CameraTexCoord =  GLES30.glGetAttribLocation(program,"a_CameraTexCoord")

        Log.w(TAG,"$TAG,  a_Position:$a_Position    a_CameraTexCoord:$a_CameraTexCoord    u_CameraColorTexture:$u_CameraColorTexture")
        GLError.maybeThrowGLException("initShaderParameter", "initShaderParameter$program")
    }

    override fun onSurfaceCreated() {
        Log.w(TAG,"onSurfaceCreated")
        initProgram()
        initTexture()
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
        Log.w(TAG,"onSurfaceChanged:width$width   height:$height")
    }

    override fun onDrawFrame() {
        GLError.maybeThrowGLException("BackgroundRenderer", "onDrawFrame")
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glUseProgram(program)
        GLES30.glBindTexture(textureTarget,textureIds[0])
        GLES30.glUniform1i(u_CameraColorTexture, 0)
        GLES30.glEnableVertexAttribArray(a_Position)
        GLES30.glEnableVertexAttribArray(a_CameraTexCoord)


        GLES30.glVertexAttribPointer(a_Position, 2, GLES30.GL_FLOAT, false, 0, vertexCoords)
        GLES30.glVertexAttribPointer(a_CameraTexCoord, 2, GLES30.GL_FLOAT, false, 0, textureCoords)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)

        GLES30.glDisableVertexAttribArray(a_Position)
        GLES30.glDisableVertexAttribArray(a_CameraTexCoord)

        GLES30.glBindTexture(textureTarget, 0)
        GLES30.glUseProgram(0)
    }
}