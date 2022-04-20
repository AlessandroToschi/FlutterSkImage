package com.example.flutter_skimage

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLUtils
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import gles.EglCore
import gles.OffscreenSurface
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.android.FlutterSurfaceView
import java.lang.reflect.Array
import java.lang.reflect.Method
import javax.microedition.khronos.opengles.GL10


fun genTexture(): Int {
    return genTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES)
}

fun genTexture(textureType: Int): Int {
    val genBuf = IntArray(1)
    GLES20.glBindTexture(textureType, 0)
    GLES20.glGenTextures(1, genBuf, 0)
    GLES20.glBindTexture(textureType, genBuf[0])

    // Set texture default draw parameters
    if (textureType == GLES11Ext.GL_TEXTURE_EXTERNAL_OES) {
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MIN_FILTER,
            GL10.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MAG_FILTER,
            GL10.GL_LINEAR.toFloat()
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
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GL10.GL_TEXTURE_MIN_FILTER,
            GL10.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GL10.GL_TEXTURE_MAG_FILTER,
            GL10.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT)
    }
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
        // Recycle the bitmap, since its data has been loaded into OpenGL.
        bitmap.recycle()
    }
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

    fun getTextureID(): IntArray {
        return listOf(textureId, dimensions[0], dimensions[1]).toIntArray()
    }

    init {
        Log.d("message: ", calculateMethodSignature(MainActivity::class.java.getMethod("getTextureID")));
        System.loadLibrary("ffi_bridge")
        loadJNI()
        textureId = 0
        Log.d("message", dimensions.toString())
    }

    external fun loadJNI()

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onFlutterSurfaceViewCreated(flutterSurfaceView: FlutterSurfaceView) {
        super.onFlutterSurfaceViewCreated(flutterSurfaceView)
        val eglCore = EglCore(null, EglCore.FLAG_TRY_GLES3)
        val surface = OffscreenSurface(eglCore, 1, 1)
        surface.makeCurrent()
        textureId = loadTexture(this, R.drawable.texture2, dimensions)
        Log.d("message", textureId.toString())
    }
}

