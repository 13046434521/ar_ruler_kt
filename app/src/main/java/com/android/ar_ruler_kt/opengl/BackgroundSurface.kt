package com.android.ar_ruler_kt.opengl

import android.content.Context
import android.util.AttributeSet

/**
 * @author：TianLong
 * @date：2022/6/27 23:32
 * @detail：背景渲染类
 */
class BackgroundSurface: GLSurface {
    constructor(context: Context?) : super(context,null)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        initialize()
    }

    private fun initialize(){
        iBaseRenderer = BackgroundRenderer()
    }
}