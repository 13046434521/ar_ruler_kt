package com.android.ar_ruler_kt.opengl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLU
import android.opengl.GLUtils
import android.opengl.Matrix
import com.google.ar.core.Pose
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
    val vertex = FloatArray(12)

    var vertexBuffer  = ByteBuffer.allocateDirect(vertex.size * Float.SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer()

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
//        Matrix.setIdentityM(matrix,0)
        GLES30.glUniformMatrix4fv(u_MvpMatrix,1,false,matrix,0)
        GLError.maybeThrowGLException("PictureRenderer", "onDrawFrame")
        GLES30.glEnableVertexAttribArray(a_Position)
        GLError.maybeThrowGLException("PictureRenderer", "onDrawFrame")
        GLES30.glEnableVertexAttribArray(a_ColorTexCoord)
        GLError.maybeThrowGLException("PictureRenderer", "onDrawFrame")
        GLES30.glDisable(GLES30.GL_CULL_FACE)
        GLError.maybeThrowGLException("PictureRenderer", "onDrawFrame")
        GLES30.glVertexAttribPointer(a_Position,3,GLES30.GL_FLOAT,false,0,vertexBuffer)
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

    /**
     * 给bitmap绘制内容
     * @param content String
     */
    fun setLength2Bitmap(content:String){
        bitmap = drawBitmap(200,100,content)
    }

    /**
     * 设置顶点坐标
     * @param pose1 Pose
     * @param pose2 Pose
     * @param viewMatrix FloatArray
     */
    fun upDataVertex(pose1:Pose,pose2:Pose,viewMatrix:FloatArray) {
        val pos1_world = floatArrayOf(pose1.tx(),pose1.ty(),pose1.tz(),1f)
        val pos2_world = floatArrayOf(pose2.tx(),pose2.ty(),pose2.tz(),1f)

        // 转为相机坐标系下的两个点
        val pos1 = FloatArray(4)
        val pos2 = FloatArray(4)
        Matrix.multiplyMV(pos1, 0, viewMatrix, 0, pos1_world, 0)
        Matrix.multiplyMV(pos2, 0, viewMatrix, 0, pos2_world, 0)

        // 转为近剪切面上的两个点
        val newpose1 = FloatArray(4)
        val newpose2 = FloatArray(4)
        mappingNear(newpose1,pos1)
        mappingNear(newpose2,pos2)

        // newpose1，newpose2的中点
        val centerpose = floatArrayOf(
            (newpose2[0] + newpose1[0])/2,
            (newpose2[1] + newpose1[1])/2,
            -0.1f,
            +1.0f
        )

        // 求出这两个点 在 z = -0.1时，在这个平面上的二维向量
        val vector = FloatArray(2)
        vector[0] = newpose2[0] - newpose1[0]
        vector[1] = newpose2[1] - newpose1[1]

        // 垂直向量
        val vector90 = rotate(vector)
        // vector进行归一化
        val normal1 = normal(vector)
        // 垂直向量归一化
        val normal2 = normal(vector90)

        val pointA = FloatArray(3)
        val pointB = FloatArray(3)
        val pointC = FloatArray(3)
        val pointD = FloatArray(3)
        val th1 = 0.05f
        val th2 = 0.025f
        pointA[0] = centerpose[0] - th1 * normal1[0] - th1 * normal2[0]
        pointA[1] = centerpose[1] - th2 * normal1[1] - th2 * normal2[1]
        pointA[2] = - 0.1f
        pointB[0] = centerpose[0] + th1 * normal1[0] - th1 * normal2[0]
        pointB[1] = centerpose[1] + th2 * normal1[1] - th2 * normal2[1]
        pointB[2] = - 0.1f
        pointC[0] = centerpose[0] - th1*normal1[0] + th1*normal2[0]
        pointC[1] = centerpose[1] - th2*normal1[1] + th2*normal2[1]
        pointC[2] = - 0.1f
        pointD[0] = centerpose[0] + th1*normal1[0] + th1*normal2[0]
        pointD[1] = centerpose[1] + th2*normal1[1] + th2*normal2[1]
        pointD[2] = -0.1f

        vertex[0] = pointA[0]
        vertex[1] = pointA[1]
        vertex[2] = pointA[2]

        vertex[3] = pointB[0]
        vertex[4] = pointB[1]
        vertex[5] = pointB[2]

        vertex[6] = pointC[0]
        vertex[7] = pointC[1]
        vertex[8] = pointC[2]

        vertex[9] = pointD[0]
        vertex[10] = pointD[1]
        vertex[11] = pointD[2]

        vertexBuffer.put(vertex).position(0)
    }
}