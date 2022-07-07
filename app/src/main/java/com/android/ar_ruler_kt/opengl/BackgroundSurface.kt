package com.android.ar_ruler_kt.opengl

import android.content.Context
import android.opengl.Matrix
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.android.ar_ruler_kt.IViewInterface
import com.android.ar_ruler_kt.helper.DisplayRotationHelper
import com.google.ar.core.*
import java.util.concurrent.ConcurrentLinkedQueue
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
    lateinit var pointRenderer: PointRenderer
    var viewMatrix = FloatArray(16)
    var projectMatrix = FloatArray(16)
    var motionEvent:MotionEvent? = null
    var iViewInterface:IViewInterface? = null
    private val anchorQueue:ConcurrentLinkedQueue<MotionEvent> by lazy {ConcurrentLinkedQueue<MotionEvent>() }
    private val limitsSize = 2 // 点的上限个数
    private val anchorList = ArrayList<Anchor>(limitsSize)
    private val displayRotationHelper by lazy { DisplayRotationHelper(context) }
    override var session : Session? = null
    var detectPointOrPlane = false

    constructor(context: Context) : super(context,null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
        initialize(context)
    }

    private fun initialize(context: Context){
        backgroundRenderer = BackgroundRenderer(context)
        bitmapRenderer= BitmapRenderer(context)
        pointRenderer = PointRenderer(context)
        // 设置为单位矩阵
        Matrix.setIdentityM(viewMatrix,0)
        Matrix.setIdentityM(projectMatrix,0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)
        backgroundRenderer.onSurfaceCreated()
        bitmapRenderer.onSurfaceCreated()
        pointRenderer.onSurfaceCreated()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl,width,height)
        displayRotationHelper.onSurfaceChanged(width, height)
        backgroundRenderer.onSurfaceChanged(width,height)
        bitmapRenderer.onSurfaceChanged(width,height)
        pointRenderer.onSurfaceChanged(width,height)
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
               detectFailed()
                return
            }

            backgroundRenderer.onDrawFrame()
            val tt = System.currentTimeMillis()
            val camera = frame.camera

            if (camera.trackingState!=TrackingState.TRACKING){
               detectFailed()
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

//                if ((trackable is Plane ) && (trackable.isPoseInPolygon(hitResult.hitPose))){

                if ((trackable is Plane ) or (trackable is Point) or (trackable is DepthPoint )){
                    val anchor = hitResult.createAnchor()
                    if (anchor.trackingState == TrackingState.TRACKING){
                        detectSuccess()
                        // 获取点的位置
                        val pose = FloatArray(16)
                        anchor.pose.toMatrix(pose ,0)
                        bitmapRenderer.upDateMatrix(pose,viewMatrix,projectMatrix)
                        bitmapRenderer.onDrawFrame()

                        // 填加锚点
                        anchorQueue.poll()?.run {
                            anchorList.takeIf {anchorList.size==limitsSize }?.apply {
                                anchorList.first().detach()
                                anchorList.removeFirst()
                            }
                            anchorList.add(anchor)
                        }
                        Log.e(TAG,"bitmapRenderer.onDrawFrame():${hitResult.distance}")
                    }else{
                       detectFailed()
                    }
                }
            }else{
               detectFailed()
                return
            }


            for (anchor in anchorList){
                val pose = FloatArray(16)
                anchor.pose.toMatrix(pose ,0)
                pointRenderer.upDateMatrix(pose,viewMatrix,projectMatrix)
                pointRenderer.onDrawFrame()
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

    fun add(){
        if (detectPointOrPlane){
            anchorQueue.clear()
            anchorQueue.add(motionEvent)
            Log.w(TAG,"add: ${anchorQueue.size}")
        }
    }

    fun delete(){
        if (detectPointOrPlane && anchorList.isNotEmpty()){
            anchorList.last().detach()
            anchorList.removeLast()
            Log.w(TAG,"delete: ${anchorList.size}")
        }
    }
    
    private fun detectSuccess(){
        detectPointOrPlane=true
        iViewInterface?.detectSuccess()
    }

    private fun detectFailed(){
        detectPointOrPlane=false
        iViewInterface?.detectFailed()
    }
}