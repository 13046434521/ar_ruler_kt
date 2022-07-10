package com.android.ar_ruler_kt.opengl

import android.content.Context
import android.graphics.Canvas
import android.opengl.Matrix
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.android.ar_ruler_kt.IViewInterface
import com.android.ar_ruler_kt.helper.DisplayRotationHelper
import com.google.ar.core.*
import java.lang.Exception
import java.util.concurrent.ConcurrentLinkedQueue
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author：TianLong
 * @date：2022/6/27 23:32
 * @detail：背景渲染类
 */
class BackgroundSurface: GLSurface ,SessionImpl {
    lateinit var backgroundRenderer:BackgroundRenderer
    lateinit var bitmapRenderer: BitmapRenderer
    lateinit var pointRenderer: PointRenderer
    lateinit var lineRenderer: LineRenderer
    lateinit var pictureRenderer: PictureRenderer
    var viewMatrix = FloatArray(16)
    var projectMatrix = FloatArray(16)
    var motionEvent:MotionEvent? = null
    var iViewInterface:IViewInterface? = null
    private val anchorQueue:ConcurrentLinkedQueue<MotionEvent> by lazy {ConcurrentLinkedQueue<MotionEvent>() }
    private val limitsSize = 10 // 点的上限个数
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
        lineRenderer = LineRenderer(context)
        pictureRenderer = PictureRenderer(context)
        // 设置为单位矩阵
        Matrix.setIdentityM(viewMatrix,0)
        Matrix.setIdentityM(projectMatrix,0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)
        backgroundRenderer.onSurfaceCreated()
        bitmapRenderer.onSurfaceCreated()
        pointRenderer.onSurfaceCreated()
        lineRenderer.onSurfaceCreated()
        pictureRenderer.onSurfaceCreated()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl,width,height)
        displayRotationHelper.onSurfaceChanged(width, height)
        backgroundRenderer.onSurfaceChanged(width,height)
        bitmapRenderer.onSurfaceChanged(width,height)
        pointRenderer.onSurfaceChanged(width,height)
        pictureRenderer.onSurfaceChanged(width,height)
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
//            pictureRenderer.onDrawFrame()
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
//                (trackable is Plane ) && trackable.isPoseInPolygon(hitResult.hitPose)
//                (trackable is Plane ) or (trackable is Point) or (trackable is DepthPoint )
                if ((trackable is Plane ) && trackable.isPoseInPolygon(hitResult.hitPose)){
                    var anchor : Anchor? = null
                    try {
                        anchor = hitResult.createAnchor()
                    }catch (e:Exception){
                        Log.e(TAG,"Ececption:$e")
                    }

                    if (anchor?.trackingState == TrackingState.TRACKING){
                        detectSuccess()
                        // 获取点的位置
                        val pose = FloatArray(16)
                        anchor.pose.toMatrix(pose ,0)
                        bitmapRenderer.upDateMatrix(pose,viewMatrix,projectMatrix)
                        bitmapRenderer.onDrawFrame()

                        // 填加锚点
                        addAncorPoint(anchor)
                        Log.e(TAG,"bitmapRenderer.onDrawFrame():${hitResult.distance}")
                    }else{
                       detectFailed()
                    }

                    // 暂时在此处渲染
                    drawPoint()
                    drawLine(anchor,anchorList,viewMatrix,projectMatrix)
                }
                else{
                    detectFailed()
                }
            }else{
                detectFailed()
                return
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
            if (anchorList.size%2==0){
                anchorList.last().detach()
                anchorList.removeLast()
                anchorList.last().detach()
                anchorList.removeLast()
            }else{
                anchorList.last().detach()
                anchorList.removeLast()
            }

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

    private fun drawLine(currentAnchor: Anchor?,list:ArrayList<Anchor>,view:FloatArray,project:FloatArray){
            val size = list.size / 2
            for (index in 0 until size){
//                val point = floatArrayOf(
//                    list[index*2].pose.tx(),
//                    list[index*2].pose.ty(),
//                    list[index*2].pose.tz(),
//                    list[index*2+1].pose.tx(),
//                    list[index*2+1].pose.ty(),
//                    list[index*2+1].pose.tz(),
//                )
                // 两个点效果一样。所以注掉
                val pose1 = list[index*2].pose.translation
                val pose2 = list[index*2+1].pose.translation
                val point1 = floatArrayOf(
                    pose1[0],pose1[1],pose1[2],
                    pose2[0],pose2[1],pose2[2],
                )
                lineRenderer.vertexBuffer.put(point1).position(0)
                lineRenderer.upDateMatrix(view,project)
                lineRenderer.onDrawFrame()

                drawPicture(list[index*2].pose, list[index*2+1].pose)
            }

        // 当前锚点不为空时，进行渲染
        currentAnchor?.run {
            val isAnchor = list.isNotEmpty() && list.size % 2 != 0
            if (isAnchor){
                val anchor = list.last()
                val pose1 = anchor.pose
                val pose2 = this.pose
                var data = floatArrayOf(
                    pose1.tx(),pose1.ty(),pose1.tz(),
                    pose2.tx(),pose2.ty(),pose2.tz()
                )

                lineRenderer.vertexBuffer.put(data).position(0)
                lineRenderer.upDateMatrix(view,project)
                lineRenderer.onDrawFrame()

                drawPicture(pose1,pose2)
            }
        }
    }

    fun drawPoint(){
        for (anchor in anchorList){
            val pose = FloatArray(16)
            anchor.pose.toMatrix(pose ,0)
            pointRenderer.upDateMatrix(pose,viewMatrix,projectMatrix)
            pointRenderer.onDrawFrame()
        }
    }

    fun  addAncorPoint(anchor: Anchor){
        anchorQueue.poll()?.run {
            anchorList.takeIf {anchorList.size==limitsSize }?.apply {
                anchorList.first().detach()
                anchorList.removeFirst()
                anchorList.first().detach()
                anchorList.removeFirst()
            }
            anchorList.add(anchor)
        }
    }

    fun drawPicture(pose1:Pose,pose2:Pose){
        // 计算两个点之间的距离
        val length = pictureRenderer.length(pose1,pose2)
        val res = String.format("%.2f", length)
        // 获取将要绘制的bitmap
//        pictureRenderer.bitmap = pictureRenderer.drawBitmap(200,100,"${res}m")
        pictureRenderer.setLength2Bitmap("${res}m")
        // 获取模型矩阵
        var model = FloatArray(16)
        pose2.toMatrix(model,0)
        // 获取应该锚点的位置
        val position = floatArrayOf(
            pose2.tx()-pose1.tx(),
            pose2.ty()-pose1.ty(),
            pose2.tz()-pose1.tz(),
        )
        var newModel = FloatArray(16)
        Matrix.translateM(newModel,0,model,0, position[0],position[1],position[2])

        // 更新MVP矩阵，进行绘制
        pictureRenderer.upDateMatrix(newModel,viewMatrix,projectMatrix)
        pictureRenderer.onDrawFrame()
    }

}