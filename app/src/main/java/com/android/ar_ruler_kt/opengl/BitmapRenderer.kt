package com.android.ar_ruler_kt.opengl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLUtils
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * @author：TianLong
 * @date：2022/6/30 22:28
 * @detail：
 */
class BitmapRenderer(context: Context) : BaseRenderer(context) ,IBaseRenderer{
    override var fragmentPath: String = "shader/background_show_camera.frag"
    override var vertexPath: String = "shader/background_show_camera.vert"
    val bitmap:Bitmap by lazy { BitmapFactory.decodeStream(context.assets.open("pointCircle.png")) }
    init {
        textureTarget =GLES30.GL_TEXTURE_2D
    }

    var u_CameraColorTexture = -1
    var a_Position = -1
    var a_CameraTexCoord = -1

    //默认顶点坐标
    private val vertexCoords = floatArrayOf(
        -1.0f, -1.0f, -1.0f, +1.0f, +1.0f, -1.0f, +1.0f, +1.0f
    )

    /**
     * 纹理坐标
     */
    private val textureCoord = floatArrayOf(
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 0.0f,

        1.0f, 1.0f
    )
    val vertexBuffer: FloatBuffer by lazy {
        val buffer: ByteBuffer = ByteBuffer.allocateDirect(this.vertexCoords.size * 4)
        buffer.order(ByteOrder.nativeOrder())
        val vertexCoord = buffer.asFloatBuffer()
        vertexCoord.put(this.vertexCoords).position(0)

        return@lazy vertexCoord
    }
    val textureBuffer: FloatBuffer by lazy {
        val buffer: ByteBuffer = ByteBuffer.allocateDirect(textureCoord.size * 4)
        buffer.order(ByteOrder.nativeOrder())
        val textureCoordBuffer = buffer.asFloatBuffer()
        textureCoordBuffer.put(textureCoord).position(0)
        return@lazy textureCoordBuffer
    }

    override fun initShaderParameter() {
        u_CameraColorTexture =  GLES30.glGetUniformLocation(program,"u_CameraColorTexture")
        a_Position =  GLES30.glGetAttribLocation(program,"a_Position")
        a_CameraTexCoord =  GLES30.glGetAttribLocation(program,"a_CameraTexCoord")
        GLError.maybeThrowGLException("initShaderParameter", "initShaderParameter$program")

        Log.w(TAG,"$TAG,  a_Position:$a_Position    a_CameraTexCoord:$a_CameraTexCoord    u_CameraColorTexture:$u_CameraColorTexture")

    }

    override fun onSurfaceCreated() {
        Log.w(TAG,"onSurfaceCreated")
        initProgram()
        initTexture()
        GLES30.glBindTexture(textureTarget,textureIds[0])
        GLError.maybeThrowGLException("BackgroundRenderer", "onDrawFrame")
        GLUtils.texImage2D(textureTarget, 0, bitmap, 0)
        GLError.maybeThrowGLException("BackgroundRenderer", "onDrawFrame")
        GLES30.glBindTexture(textureTarget,0)
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    override fun onDrawFrame() {
        GLError.maybeThrowGLException(TAG, "onDrawFrame")
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glUseProgram(program)
        GLES30.glBindTexture(textureTarget,textureIds[0])
        GLES30.glUniform1i(u_CameraColorTexture, 0)
        GLES30.glEnableVertexAttribArray(a_Position)
        GLES30.glEnableVertexAttribArray(a_CameraTexCoord)


        GLES30.glVertexAttribPointer(a_Position, 2, GLES30.GL_FLOAT, false, 0,
            this.vertexBuffer
        )
        GLES30.glVertexAttribPointer(a_CameraTexCoord, 2, GLES30.GL_FLOAT, false, 0, textureBuffer)
        GLUtils.texImage2D(textureTarget, 0, bitmap, 0)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)

        GLES30.glDisableVertexAttribArray(a_Position)
        GLES30.glDisableVertexAttribArray(a_CameraTexCoord)

        GLES30.glBindTexture(textureTarget, 0)
        GLES30.glUseProgram(0)
    }
}