package com.android.ar_ruler_kt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.ar_ruler_kt.helper.SessionHelper
import com.android.ar_ruler_kt.opengl.BackgroundRenderer
import com.android.ar_ruler_kt.opengl.BackgroundSurface
import com.android.ar_ruler_kt.opengl.BaseRenderer
import com.google.ar.core.Session

class MainActivity : AppCompatActivity() {
    val session:Session by lazy {
        SessionHelper.session
    }
    val backgroundSurface :BackgroundSurface by lazy{findViewById(R.id.gl_main_background)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SessionHelper.initialize(this)


        session.resume()
        val  backgroundRenderer =backgroundSurface.iBaseRenderer as BackgroundRenderer
        backgroundRenderer.session = session

        backgroundSurface.requestRender()
//        val frame =session.update()
//        frame.acquireCameraImage().format
    }

    override fun onResume() {
        super.onResume()
    }
}