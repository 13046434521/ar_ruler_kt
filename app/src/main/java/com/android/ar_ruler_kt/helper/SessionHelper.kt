package com.android.ar_ruler_kt.helper

import android.content.Context
import com.google.ar.core.Session

/**
 * @author：TianLong
 * @date：2022/6/27 20:59
 * @detail：
 */
object SessionHelper {

    lateinit var session: Session
    fun initialize(context:Context){


        session = Session(context)
    }

    fun config(){
//        session.configure()
    }


    fun release(){
        session.close()
    }
}