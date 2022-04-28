package com.example.flutter_skimage

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.opengl.*
import android.os.Build
import android.util.Log
import android.view.Surface
import androidx.annotation.RequiresApi
import gles.EglCore
import gles.OffscreenSurface
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.android.FlutterSurfaceView
import java.lang.Exception
import java.lang.reflect.Array
import java.lang.reflect.Method
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL11


fun genTexture(): Int {
    return genTexture(GLES20.GL_TEXTURE_2D)
}

val genBuf = IntArray(1)

fun genTexture(textureType: Int): Int {


    //GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    GLES20.glGenTextures(1, genBuf, 0)

    GLES20.glBindTexture(textureType, genBuf[0])

    // Set texture default draw parameters
    if (textureType == GLES11Ext.GL_TEXTURE_EXTERNAL_OES) {
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MIN_FILTER,
            GL10.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MAG_FILTER,
            GL10.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_S,
            GL10.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_T,
            GL10.GL_CLAMP_TO_EDGE
        )
    } else {
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_NEAREST
        )
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
    }
    Log.d("message", genBuf[0].toString())

    return genBuf[0]
}

fun loadTexture(context: Context, resourceId: Int, size: MutableList<Int>): Int {
    val texId: Int = genTexture()

    if (texId != 0) {
        val options = BitmapFactory.Options()
        options.inScaled = false // No pre-scaling
        options.inJustDecodeBounds = true
        // Just decode bounds
        BitmapFactory.decodeResource(context.getResources(), resourceId, options)
        // Set return size
        size.add(options.outWidth)
        size.add(options.outHeight)

        // Decode
        options.inJustDecodeBounds = false
        val bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options)
        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        //val surfaceTexture = SurfaceTexture(texId)
        //surfaceTexture.setDefaultBufferSize(options.outWidth, options.outHeight)
        //val surface = Surface(surfaceTexture)
//
        //try {
        //    val canvas = surface.lockCanvas(null)
        //    canvas.drawBitmap(bitmap, 0.0.toFloat(), 0.0.toFloat(), null)
        //    surface.unlockCanvasAndPost(canvas)
        //} catch (e: Exception) {
        //    Log.d("message", e.toString())
        //}
        //surfaceTexture.updateTexImage()
        //surfaceTexture.release()
        //surface.release()

        bitmap.recycle()

    }
    Log.d("message",texId.toString())
    return texId
}

private fun calculateMethodSignature(method: Method?): String {
    var signature = ""
    if (method != null) {
        signature += "("
        for (c in method.getParameterTypes()) {
            val Lsig: String = Array.newInstance(c, 1).javaClass.name
            signature += Lsig.substring(1)
        }
        signature += ")"
        val returnType: Class<*> = method.getReturnType()
        if (returnType == Void.TYPE) {
            signature += "V"
        } else {
            signature += Array.newInstance(returnType, 1).javaClass.name
        }
        signature = signature.replace('.', '/')
    }
    return signature
}

class MainActivity() : FlutterActivity() {

    val dimensions = mutableListOf<Int>()

    var textureId: Int

    var eglCore: EglCore? = null

    var surface: OffscreenSurface? = null

    var eglContext: EGLContext? = null

    var count: Int = 0

    fun getTextureID(): IntArray {

        if (count == 3) {
            GLES20.glDeleteTextures(1, genBuf, 0)
        }

        count++

        return listOf(textureId, dimensions[0], dimensions[1]).toIntArray()
    }

    init {
        System.loadLibrary("ffi_bridge")

        loadJNI()

        textureId = 0

    }

    private external fun loadJNI()

    override fun setEGLContext(eglContext: EGLContext) {
        this.eglContext = eglContext
        this.eglCore = EglCore(eglContext, 0)
        this.surface = OffscreenSurface(eglCore, 1, 1)
        surface?.makeCurrent()
        textureId = loadTexture(this, R.drawable.texture2, dimensions)
    }

    override fun onFlutterSurfaceViewCreated(flutterSurfaceView: FlutterSurfaceView) {
        super.onFlutterSurfaceViewCreated(flutterSurfaceView)
    }




}

