package com.android.ar_ruler_kt.opengl

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES30
import com.android.ar_ruler_kt.helper.ShaderHelper

/**
 * @author：TianLong
 * @date：2022/6/27 22:39
 * @detail：基础 Renderer 类
 */
 abstract class BaseRenderer(override var context: Context) : IBaseRenderer,ShaderImpl{
    var textureIds = IntArray(1)
    var program : Int = -1
    val textureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES
    override lateinit var vertexSource: String
    override lateinit var fragmentSource: String
    fun initOpenGL(){
        // 1. 创建program
        program = GLES30.glCreateProgram()
        ShaderHelper.checkGLError("111")
        // 创建Shader
        val fragShader = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER)
        val vertexShader = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER)
        ShaderHelper.checkGLError("111")
        // 获取Shader内容
        vertexSource =  ShaderHelper.readRawTextFileFromAssets(context,vertexPath)
        fragmentSource = ShaderHelper.readRawTextFileFromAssets(context,fragmentPath)
        ShaderHelper.checkGLError("111")
        // 传入ShaderSource
        GLES30.glShaderSource(fragShader, fragmentSource)
        GLES30.glShaderSource(vertexShader,vertexSource)
        ShaderHelper.checkGLError("111")
        // 编译Shader
        GLES30.glCompileShader(fragShader)
        GLES30.glCompileShader(vertexShader)
        ShaderHelper.checkGLError("111")
        // program和shader进行attach
        GLES30.glAttachShader(program,fragShader)
        GLES30.glAttachShader(program,vertexShader)
        // 链接program
        GLES30.glLinkProgram(program)
        // 2. 使用program
        GLES30.glUseProgram(program)
        // 3. 生成纹理
        GLES30.glGenTextures(1,textureIds,0)
        // 4. 绑定纹理


        GLES30.glBindTexture(textureTarget,textureIds[0])
        // 5. 设置纹理相关参数
        GLES30.glTexParameteri(textureTarget, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(textureTarget, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(textureTarget, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST)
        GLES30.glTexParameteri(textureTarget, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)

        ShaderHelper.checkGLError("111")
        // 解绑纹理
        GLES30.glBindTexture(textureTarget,0)
    }

    override fun onSurfaceCreated() {
        initOpenGL()
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        GLES30.glViewport(0,0,width,height)
    }

    override fun onDrawFrame() {
        GLES30.glBindTexture(textureTarget,textureIds[0])





        GLES30.glBindTexture(textureTarget,0)
    }
}