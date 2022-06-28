package com.android.ar_ruler_kt.opengl

import android.content.Context
import android.util.Log
import com.google.ar.core.Session

/**
 * @author：TianLong
 * @date：2022/6/27 23:31
 * @detail：背景 渲染 Renderer
 */
class BackgroundRenderer(context : Context) : BaseRenderer(context),SessionImpl {
    override var fragmentPath: String = "shader/background_show_camera.frag"
    override var vertexPath: String = "shader/background_show_camera.vert"

    override var session :Session? = null

    override fun onSurfaceCreated() {
        Log.w(TAG,"onSurfaceCreated")
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        Log.w(TAG,"onSurfaceChanged:width$width   height:$height")
    }

    override fun onDrawFrame() {
        session?.run {
            this.setCameraTextureName(textureIds[0])
            val frame = this.update()
//            val image= frame.acquireCameraImage()
//            Log.w(TAG,"update:${image.format}")
        }
    }
}