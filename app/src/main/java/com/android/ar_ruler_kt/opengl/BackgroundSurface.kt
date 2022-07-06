package com.android.ar_ruler_kt.opengl

import android.content.Context
import android.opengl.Matrix
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.android.ar_ruler_kt.helper.DisplayRotationHelper
import com.google.ar.core.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author：TianLong
 * @date：2022/6/27 23:32
 * @detail：背景渲染类
 */
class BackgroundSurface: GLSurface ,SessionImpl{
    lateinit var backgroundRenderer:BackgroundRenderer
    lateinit var bitmapRenderer: BitmapRenderer
    var viewMatrix = FloatArray(16)
    var projectMatrix = FloatArray(16)
    var motionEvent:MotionEvent? = null

    private val displayRotationHelper by lazy { DisplayRotationHelper(context) }
    override var session : Session? = null
    constructor(context: Context) : super(context,null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
        initialize(context)
    }

    private fun initialize(context: Context){
        backgroundRenderer = BackgroundRenderer(context)
        bitmapRenderer= BitmapRenderer(context)

        // 设置为单位矩阵
        Matrix.setIdentityM(viewMatrix,0)
        Matrix.setIdentityM(projectMatrix,0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)
        backgroundRenderer.onSurfaceCreated()
        bitmapRenderer.onSurfaceCreated()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl,width,height)
        displayRotationHelper.onSurfaceChanged(width, height)
        backgroundRenderer.onSurfaceChanged(width,height)
        bitmapRenderer.onSurfaceChanged(width,height)
    }

    override fun onDrawFrame(gl: GL10?) {
        super.onDrawFrame(gl)
        session?.run {
            displayRotationHelper.updateSessionIfNeeded(session)
            this.setCameraTextureName(backgroundRenderer.textureIds[0])
            val frame = this.update()

            if (frame.hasDisplayGeometryChanged()) {
                frame.transformCoordinates2d(
                    Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
                    backgroundRenderer.vertexBuffer,
                    Coordinates2d.TEXTURE_NORMALIZED,
                    backgroundRenderer.textureBuffer)
            }

            if (frame.timestamp == 0L) {
                return
            }

            backgroundRenderer.onDrawFrame()
//            bitmapRenderer.onDrawFrame()
            val tt = System.currentTimeMillis()
            val camera = frame.camera

            if (camera.trackingState!=TrackingState.TRACKING){
                Log.e(TAG,"It's error  because camera trackingState is ${camera.trackingState.name}")
                return
            }


            camera.getViewMatrix(viewMatrix,0)
            camera.getProjectionMatrix(projectMatrix,0,0.01f,10f)

            val pointX=width/2F
            val pointY=height/2F
            val hitResults =frame.hitTest(pointX,pointY)

            // 锚点不知道是否准确
            if (hitResults.isNotEmpty() and (hitResults.size>0)){
                val hitResult = hitResults.last()
                trackable(hitResult.trackable)
                val trackable = hitResult.trackable
                if (trackable is Point ) Log.w(TAG,"trackable is Point:${trackable.orientationMode.name}")

                if (trackable is Plane ) Log.w(TAG,"trackable is Plane:${trackable.type.name}")

                if ((trackable is Plane ) && (trackable.isPoseInPolygon(hitResult.hitPose))){
                    val anchor = hitResult.createAnchor()
                    if (anchor.trackingState == TrackingState.TRACKING){
                        // 获取点的位置
                        val pose = FloatArray(16)
                        anchor.pose.toMatrix(pose ,0)
                        bitmapRenderer.upDateMatrix(pose,viewMatrix,projectMatrix)
                        bitmapRenderer.onDrawFrame()
                        Log.e(TAG,"bitmapRenderer.onDrawFrame():${hitResult.distance}")
                    }
                }
            }

            Log.w(TAG,"耗时：${System.currentTimeMillis()-tt}")
        }
    }

    fun trackable(trackable: Trackable){
        val msg = when (trackable) {
            is Point -> "Point"
            is Plane -> "Plane"
            is InstantPlacementPoint -> "InstantPlacementPoint"
            is Earth -> "Earth"
            is DepthPoint -> "DepthPoint"
            is AugmentedFace -> "AugmentedFace"
            is AugmentedImage -> "AugmentedImage"
            else -> "Other"
        }
        Log.e(TAG,"trackable is $msg")
    }
}