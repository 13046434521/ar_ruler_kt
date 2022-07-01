package com.android.ar_ruler_kt.opengl

import android.content.Context
import android.opengl.Matrix
import android.util.AttributeSet
import android.util.Log
import com.android.ar_ruler_kt.helper.DisplayRotationHelper
import com.google.ar.core.Coordinates2d
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import java.lang.Exception
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
                    backgroundRenderer.vertexCoords,
                    Coordinates2d.TEXTURE_NORMALIZED,
                    backgroundRenderer.textureCoords)
            }

            if (frame.timestamp == 0L) {
                return
            }

            backgroundRenderer.onDrawFrame()
            bitmapRenderer.onDrawFrame()
            val tt = System.currentTimeMillis()
            val camera = frame.camera

            if (camera.trackingState!=TrackingState.TRACKING){
                Log.e(TAG,"It's error  because camera trackingState is ${camera.trackingState.name}")
                return
            }


            camera.getViewMatrix(viewMatrix,0)
            camera.getProjectionMatrix(projectMatrix,0,0f,100f)

            val hitResults =frame.hitTest(width/2F,height/2F)
            // 锚点不知道是否准确
            try {
                for (hitResult in hitResults){
                    if (hitResult.trackable.trackingState==TrackingState.TRACKING){
                        val anchor = hitResult.createAnchor()
                        if (anchor.trackingState == TrackingState.TRACKING){
                            Log.w(TAG,"anchor:${anchor.pose.tx()} ${camera.pose.tx()} ${anchor.pose.ty()} ${camera.pose.ty()}  ${anchor.pose.tz()} ${camera.pose.tz()}")
                        }
                        Log.w(TAG,"hitResult.distance:${hitResult.distance}")
                    }
                }
            }catch (e:Exception){
                Log.e(TAG,"exception:${e.message}")
            }


            Log.w(TAG,"耗时：${System.currentTimeMillis()-tt}")
        }
    }
}