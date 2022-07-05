package com.android.ar_ruler_kt

import android.graphics.Point
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.ar_ruler_kt.helper.SessionHelper
import com.android.ar_ruler_kt.opengl.BackgroundSurface
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
        val mPoint = Point()
        this.getWindowManager().getDefaultDisplay().getSize(mPoint)
        val motionEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, mPoint.x / 2f, mPoint.y / 2f, 0);
        session = SessionHelper.session
        backgroundSurface.session = session
        backgroundSurface.backgroundRenderer
        backgroundSurface.motionEvent = motionEvent
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