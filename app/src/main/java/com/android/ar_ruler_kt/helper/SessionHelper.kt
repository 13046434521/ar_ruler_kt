package com.android.ar_ruler_kt.helper

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.ar.core.*

/**
 * @author：TianLong
 * @date：2022/6/27 20:59
 * @detail：ARCore Session Helper类
 */
object SessionHelper : Helper(){
    val featureSet: Set<Session.Feature> = setOf()
    lateinit var session: Session

    private val sessionConfig: Config by lazy {
        return@lazy Config(session).setFocusMode(Config.FocusMode.AUTO)
            .setUpdateMode(Config.UpdateMode.BLOCKING)
            .setPlaneFindingMode(Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL)
    }
    fun initialize(context:Context):Boolean{
        return if (prepare(context)){
            session = Session(context,featureSet)
            val cameraConfigFilter = CameraConfigFilter(session)
            val list = session.getSupportedCameraConfigs(cameraConfigFilter)
            for (con in list){
                Log.w(TAG,"id:${con.cameraId}   width:${con.imageSize.width}  height:${con.imageSize.height} upper:${con.fpsRange.upper }   lower${con.fpsRange.lower}")
            }
            session.cameraConfig=list.last()

            session.configure(sessionConfig)
            true
        }else{
            false
        }
    }

    fun release(){
        session.close()
    }

    private fun prepare (context: Context):Boolean{
        when {
            (!CameraPermissionHelper.hasCameraPermission(context as Activity))-> {
                CameraPermissionHelper.requestCameraPermission(context)
                Log.w(TAG,"ARCore must have camera permission")
                return false
            }

            ArCoreApk.getInstance().requestInstall(context,true) == ArCoreApk.InstallStatus.INSTALL_REQUESTED ->{
                Log.w(TAG,"ARCore must install ArCoreApk")
                return false
            }
        }
        Log.w(TAG,"ARCore has prepared")
        return true
    }

    fun update():Frame{
        val frame = session.update()
        return frame
    }
}