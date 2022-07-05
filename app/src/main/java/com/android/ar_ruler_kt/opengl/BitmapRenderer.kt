package com.android.ar_ruler_kt.opengl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * @author：TianLong
 * @date：2022/6/30 22:28
 * @detail：
 */
class BitmapRenderer(context: Context) : BaseRenderer(context) ,IBaseRenderer,IMatrix{
    override var fragmentPath: String = "shader/bitmap_shader.frag"
    override var vertexPath: String = "shader/bitmap_shader.vert"
    val bitmap:Bitmap by lazy { BitmapFactory.decodeStream(context.assets.open("test.webp")) }
//    val bitmap:Bitmap by lazy { BitmapFactory.decodeStream(context.assets.open("pointCircle.png")) }
    init {
        textureTarget =GLES30.GL_TEXTURE_2D
    }

    var u_ColorTexture = -1
    var u_MvpMatrix = -1
    var a_Position = -1
    var a_ColorTexCoord = -1
    override var matrix = FloatArray(4 * 4)

    /**
     * 顶点坐标
     */
//    private val vertexCoords = floatArrayOf(
//        -1.0f, -1.0f,
//        -1.0f, +1.0f,
//        +1.0f, -1.0f,
//        +1.0f, +1.0f,
//    )
    private val threshold = 0.3f
    private val vertexCoords = floatArrayOf(
        -threshold, -threshold,
        +threshold, -threshold,
        -threshold, +threshold,
        +threshold, +threshold,
    )

    private val index = floatArrayOf(

    )

    /**
     * 纹理坐标
     */
    private val textureCoord = floatArrayOf(
        0.0f, 1.0f,
        1.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f,
    )
//    private val textureCoord = floatArrayOf(
//        0.0f, 0.0f,
//        1.0f, 0.0f,
//        0.0f, 1.0f,
//        1.0f, 1.0f,
//    )
//    private val textureCoord = floatArrayOf(
//        0.0f, 1.0f,
//        0.0f, 0.0f,
//        1.0f, 1.0f,
//        1.0f, 0.0f,
//    )
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
        u_ColorTexture =  GLES30.glGetUniformLocation(program,"u_ColorTexture")
        a_Position =  GLES30.glGetAttribLocation(program,"a_Position")
        a_ColorTexCoord =  GLES30.glGetAttribLocation(program,"a_ColorTexCoord")
        u_MvpMatrix =  GLES30.glGetUniformLocation(program,"u_MvpMatrix")

        GLError.maybeThrowGLException("initShaderParameter", "initShaderParameter：$program")

        Log.w(TAG,"$TAG,  a_Position:$a_Position    a_ColorTexCoord:$a_ColorTexCoord    u_ColorTexture:$u_ColorTexture")
    }

    override fun upDateMatrix(pose: FloatArray, viewMatrix: FloatArray, projectMatrix: FloatArray) {
        Matrix.setIdentityM(matrix,0)
        Matrix.multiplyMM(matrix,0,viewMatrix,0,pose,0)
        Matrix.multiplyMM(matrix,0,projectMatrix,0,matrix,0)
    }

    override fun onSurfaceCreated() {
        Log.w(TAG,"onSurfaceCreated")
        initProgram()
        initTexture()
        GLES30.glBindTexture(textureTarget,textureIds[0])
        GLUtils.texImage2D(textureTarget, 0, bitmap, 0)
        GLES30.glBindTexture(textureTarget,0)
        GLError.maybeThrowGLException(TAG, "onSurfaceCreated")
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
        if (width > height) {
            val x = width / (height.toFloat() / bitmap.height * bitmap.width)
            Matrix.orthoM(matrix, 0, -x, x, -1f, 1f, -1f, 1f)
        } else {
            val y = height / (width.toFloat() / bitmap.width * bitmap.height)
            Matrix.orthoM(matrix, 0, -1f, 1f, -y, y, -1f, 1f)
        }
    }

    override fun onDrawFrame() {
        GLError.maybeThrowGLException(TAG, "onDrawFrame")
        GLES30.glUseProgram(program)
        GLES30.glBindTexture(textureTarget,textureIds[0])
        GLES30.glUniform1i(u_ColorTexture, 0)
        GLES30.glUniformMatrix4fv(u_MvpMatrix, 1, false, matrix, 0)
        GLES30.glEnableVertexAttribArray(a_Position)
        GLES30.glEnableVertexAttribArray(a_ColorTexCoord)
        GLES30.glVertexAttribPointer(a_Position, 2, GLES30.GL_FLOAT, false, 0, this.vertexBuffer)
        GLES30.glVertexAttribPointer(a_ColorTexCoord, 2, GLES30.GL_FLOAT, false, 0, textureBuffer)

        GLES30.glDepthMask(false)

        // 开启背面剔除，默认的逆时针绘制为正面，开启后只绘制正面节省性能
        GLES30.glEnable(GLES30.GL_CULL_FACE)
        GLES30.glCullFace(GLES30.GL_BACK)

        // 开启混色,混色在glDarwArrays之前不能关闭，否则不生效
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)

        // 绘制三角带，
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)

        GLES30.glDisable(GLES30.GL_CULL_FACE)
        GLES30.glDisable(GLES30.GL_BLEND)

        GLES30.glDisableVertexAttribArray(a_Position)
        GLES30.glDisableVertexAttribArray(a_ColorTexCoord)

        GLES30.glBindTexture(textureTarget, 0)
        GLES30.glUseProgram(0)
        GLError.maybeThrowGLException("BitmapRenderer", "onDrawFrame")
    }
}