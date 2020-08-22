package com.xgimi.filemanager.views

import android.content.Context
import android.graphics.*
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Allocation.MipmapControl
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.AttributeSet
import android.view.Surface
import android.view.View
import com.blankj.utilcode.util.ScreenUtils
import com.xgimi.filemanager.R
import java.io.ByteArrayOutputStream

/**
 *    author : joker.peng
 *    e-mail : joker.peng@xgimi.com
 *    date   : 2020/8/13 11:46
 *    desc   :
 */
class BlurBgView : View {
    private var radius = 0
    private var scale = 0f
    var wh: IntArray = intArrayOf(ScreenUtils.getScreenWidth(), ScreenUtils.getAppScreenHeight())
    private var bgBitmap: Bitmap? = null
    private var color = 0

    constructor (context: Context) : this(context, null as AttributeSet?)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        radius = 20
        scale = 0.3f
        color = -1307437788
        wh = IntArray(2)
        val a = context.obtainStyledAttributes(attrs, R.styleable.BlurBgView)
        setRadius(a.getInt(R.styleable.BlurBgView_radius, 20))
        setScale(a.getFloat(R.styleable.BlurBgView_scale, 0.3f))
        setCoverColor(a.getColor(R.styleable.BlurBgView_coverColor, 1728053247))
        a.recycle()
    }

    fun getRadius(): Int {
        return radius
    }

    fun setRadius(radius: Int) {
        this.radius = radius
    }

    fun getScale(): Float {
        return scale
    }

    fun setScale(scale: Float) {
        this.scale = scale
    }

    fun getCoverColor(): Int {
        return color
    }

    fun setCoverColor(color: Int) {
        this.color = color
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (bgBitmap == null) {
            bgBitmap = getBlurBgBitmap()
        }
        if (bgBitmap != null) {
            val matrix = Matrix()
            matrix.postScale(
                this.width.toFloat() / bgBitmap!!.width.toFloat(),
                this.height.toFloat() / bgBitmap!!.height.toFloat()
            )
            canvas.drawBitmap(bgBitmap!!, matrix, null as Paint?)
            canvas.drawColor(color)
        }
    }

    fun getBlurBgBitmap(): Bitmap? {
        val bitmap = getScreenshot(wh[0], wh[1])
        val location = IntArray(2)
        getLocationInWindow(location)
        getLocationOnScreen(location)
        val temp = Bitmap.createBitmap(
            bitmap!!,
            location[0],
            location[1],
            this.width,
            this.height
        )
        return doBlur(this.context, temp, radius, scale)
    }

    fun getScreenshot(w: Int, h: Int): Bitmap? {
        return try {
            val e = Class.forName("android.view.SurfaceControl")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val myMethod = e.getDeclaredMethod(
                    "screenshot",
                    *arrayOf(
                        Rect::class.java,
                        Integer.TYPE,
                        Integer.TYPE,
                        Integer.TYPE
                    )
                )
                myMethod.invoke(
                    e,
                    Rect(), Integer.valueOf(w), Integer.valueOf(h), Surface.ROTATION_0
                ) as Bitmap
            } else {
                val myMethod = e.getDeclaredMethod(
                    "screenshot",
                    *arrayOf<Class<*>>(Integer.TYPE, Integer.TYPE)
                )
                myMethod.invoke(
                    e,
                    Integer.valueOf(w), Integer.valueOf(h)
                ) as Bitmap
            }
        } catch (var5: Exception) {
            var5.printStackTrace()
            null
        }
    }

    fun doBlur(
        context: Context?,
        fromBitmap: Bitmap?,
        radius: Int,
        scale: Float
    ): Bitmap? {
        return if (fromBitmap == null) {
            null
        } else {
            val width = fromBitmap.width
            val height = fromBitmap.height
            val matrix = Matrix()
            matrix.postScale(scale, scale)
            var newbm = Bitmap.createBitmap(fromBitmap, 0, 0, width, height, matrix, true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (newbm.config == Bitmap.Config.HARDWARE) {
                    val bitmapOptions = BitmapFactory.Options()
                    bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565
                    val mBytes = bitmapToBytes(newbm)
                    newbm = BitmapFactory.decodeByteArray(mBytes, 0, mBytes.size, bitmapOptions)
                }
            }
            val rs = RenderScript.create(context)
            val input =
                Allocation.createFromBitmap(rs, newbm, MipmapControl.MIPMAP_NONE, 1)
            val output = Allocation.createTyped(rs, input.type)
            val script =
                ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
            script.setRadius(radius.toFloat())
            script.setInput(input)
            script.forEach(output)
            output.copyTo(newbm)
            newbm
        }
    }

    private fun bitmapToBytes(newbm: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        newbm.compress(Bitmap.CompressFormat.PNG, 100, baos)
        return baos.toByteArray()
    }
}