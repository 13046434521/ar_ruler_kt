package com.android.ar_ruler_kt.opengl

import android.graphics.*

/**
 * @author：TianLong
 * @date：2022/7/9 0:33
 * @detail：
 */
interface IBitmapInterview {
    val paint: Paint
        get() {
            val tempPaint = Paint()
            tempPaint.color = Color.WHITE
            tempPaint.textSize = 250f
            tempPaint.style = Paint.Style.FILL
            return tempPaint
        }

    val paintText: Paint
        get() {
            val tempPaint = Paint()
            tempPaint.color = Color.BLACK
            tempPaint.textSize = 50f
            tempPaint.style = Paint.Style.FILL
            return tempPaint
        }
    val paintCircle: Paint
        get() {
            val tempPaint = Paint()
            tempPaint.color = Color.BLUE
            tempPaint.textSize = 50f
            tempPaint.style = Paint.Style.FILL
            return tempPaint
        }
    val canvas: Canvas
        get() = Canvas()

    fun drawBitmap(data: Bitmap,content:String): Bitmap {
        // 保证bitmap是可编辑的
        val bitmap = data.copy(Bitmap.Config.ARGB_8888, true)
        val width = bitmap.width
        val height = bitmap.height
        val rectF = RectF(0f, 0f, width.toFloat(), height.toFloat())

        // 创建Canvas
        val canvas = Canvas(bitmap)
        canvas.drawRoundRect(rectF, 100f, 100f, paint)

        // 获取文字的宽高
        val rect = Rect()
        paintText.getTextBounds(content,0, content.length,rect)
        val textWidth = rect.width()
        val textHeight = rect.height()
        // 绘制文字，view向下为y轴正方向，向左为x轴正方向，（0，0）位置在屏幕左上角
        // 位置设置为0，0时，会将文字的左下角绘制到（0.0）的位置。
        // 所以要将文字中心绘制到想要的位置上，宽需要往左（x軸负方向）便宜textWidth/2，高需要往下（y軸正方向）。
        // 原因：想想文字左下角绘制在屏幕左上角（0，0）时的效果
        canvas.drawText( content,(width.toFloat()-textWidth)/2, (height.toFloat()+textHeight)/2,paintText)

//      canvas.drawCircle( width.toFloat()/2, height.toFloat()/2,10.0f,paintCircle)
        return bitmap
    }

    fun drawBitmap(width:Int,height:Int,content:String):Bitmap{
        val bitmap =Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.argb(255,255,255,255))
        return drawBitmap(bitmap,content)
    }
}