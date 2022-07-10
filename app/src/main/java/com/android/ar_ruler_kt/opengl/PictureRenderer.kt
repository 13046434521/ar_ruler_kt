package com.android.ar_ruler_kt.opengl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLU
import android.opengl.GLUtils
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * @author：TianLong
 * @date：2022/7/9 12:12
 * @detail：
 */
class PictureRenderer(context: Context) : BaseRenderer(context),IMatrix ,IBitmapInterview,IMathInterview{
    override var fragmentPath: String = "shader/bitmap_shader.frag"
    override var vertexPath: String = "shader/bitmap_shader.vert"
    override var matrix: FloatArray = FloatArray(16)
    var bitmap :Bitmap
    init {
        bitmap = Bitmap.createBitmap(100,200,Bitmap.Config.ARGB_8888)
//        bitmap = BitmapFactory.decodeStream(context.assets.open("test.webp"))
    }

    var u_ColorTexture = -1
    var u_MvpMatrix = -1
    var a_Position = -1
    var a_ColorTexCoord = -1


    val threshold = 0.06f
    val vertex = floatArrayOf(
        -threshold,-threshold,
        +threshold,-threshold,
        -threshold,+threshold,
        +threshold,+threshold,
    )

    val vertexBuffer by lazy {
        val buffer = ByteBuffer.allocateDirect(vertex.size * Float.SIZE_BYTES).order(ByteOrder.nativeOrder())
        val tempBuffer = buffer.asFloatBuffer()
        tempBuffer.put(vertex)
        tempBuffer.position(0)
    }
    val texture = floatArrayOf(
        +0.0f,+1.0f,
        +1.0f,+1.0f,
        +0.0f,+0.0f,
        +1.0f,+0.0f,
    )

    val textureBuffer by lazy {
        val buffer = ByteBuffer.allocateDirect(texture.size * Float.SIZE_BYTES).order(ByteOrder.nativeOrder())
        val tempBuffer = buffer.asFloatBuffer()
        tempBuffer.put(texture)
        tempBuffer.position(0)
    }


    override fun initShaderParameter() {
        a_Position = GLES30.glGetAttribLocation(program,"a_Position")
        a_ColorTexCoord = GLES30.glGetAttribLocation(program,"a_ColorTexCoord")
        u_MvpMatrix = GLES30.glGetUniformLocation(program,"u_MvpMatrix")
        u_ColorTexture = GLES30.glGetUniformLocation(program,"u_ColorTexture")

        GLError.maybeThrowGLException("PictureRenderer", "initShaderParameter")
    }

    override fun onSurfaceCreated() {
        initProgram()
        initTexture()

        GLError.maybeThrowGLException("PictureRenderer", "onSurfaceCreated")
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        this.width = width
        this.height = height

        GLError.maybeThrowGLException("PictureRenderer", "onSurfaceChanged")
    }

    override fun onDrawFrame() {
        GLError.maybeThrowGLException("PictureRenderer", "onDrawFrame")
        GLES30.glUseProgram(program)
        GLES30.glBindTexture(textureTarget,textureIds[0])

        GLES30.glUniform1i(u_ColorTexture,0)
        Matrix.setIdentityM(matrix,0)
        GLES30.glUniformMatrix4fv(u_MvpMatrix,1,false,matrix,0)
        GLError.maybeThrowGLException("PictureRenderer", "onDrawFrame")
        GLES30.glEnableVertexAttribArray(a_Position)
        GLError.maybeThrowGLException("PictureRenderer", "onDrawFrame")
        GLES30.glEnableVertexAttribArray(a_ColorTexCoord)
        GLError.maybeThrowGLException("PictureRenderer", "onDrawFrame")
        GLES30.glEnable(GLES30.GL_CULL_FACE)
        GLError.maybeThrowGLException("PictureRenderer", "onDrawFrame")
        GLES30.glVertexAttribPointer(a_Position,2,GLES30.GL_FLOAT,false,0,vertexBuffer)
        GLES30.glVertexAttribPointer(a_ColorTexCoord,2,GLES30.GL_FLOAT,false,0,textureBuffer)
        GLUtils.texImage2D(textureTarget,0,bitmap,0)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP,0,4)
        GLError.maybeThrowGLException("PictureRenderer", "onDrawFrame")
        GLES30.glDisable(GLES30.GL_CULL_FACE)
        GLES30.glDisableVertexAttribArray(a_Position)
        GLES30.glDisableVertexAttribArray(a_ColorTexCoord)

        GLES30.glBindTexture(textureTarget,0)
        GLES30.glUseProgram(0)

        GLError.maybeThrowGLException("PictureRenderer", "onDrawFrame")
    }

    fun setLength2Bitmap(content:String){
        bitmap = drawBitmap(200,100,content)
    }
}