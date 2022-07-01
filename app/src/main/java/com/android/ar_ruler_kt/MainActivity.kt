package com.android.ar_ruler_kt

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.android.ar_ruler_kt.helper.SessionHelper
import com.android.ar_ruler_kt.opengl.BackgroundRenderer
import com.android.ar_ruler_kt.opengl.BackgroundSurface
import com.android.ar_ruler_kt.opengl.BaseRenderer
import com.google.ar.core.Session

class MainActivity : AppCompatActivity() {
    var session:Session? = null
    val backgroundSurface :BackgroundSurface by lazy{findViewById(R.id.gl_main_background)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!SessionHelper.initialize(this)){
            Toast.makeText(this,"ARCore初始化失败",Toast.LENGTH_SHORT).show()
            return
        }

        session = SessionHelper.session
        backgroundSurface.session = session
        backgroundSurface.backgroundRenderer
    }

    override fun onResume() {
        super.onResume()
        backgroundSurface.onResume()
        session?.resume()
    }

    override fun onPause() {
        super.onPause()
        backgroundSurface.onPause()
        session?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        session?.close()
    }
}