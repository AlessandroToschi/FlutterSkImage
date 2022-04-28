package com.example.flutter_skimage

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.media.ImageReader
import android.opengl.*
import android.os.Bundle
import android.util.Log
import android.view.Surface
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.REPEAT_MODE_ALL
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import gles.EglCore
import gles.OffscreenSurface
import io.flutter.embedding.android.FlutterActivity
import java.lang.reflect.Array
import java.lang.reflect.Method
import javax.microedition.khronos.opengles.GL10


fun genTexture(): Int {
    return genTexture(GLES20.GL_TEXTURE_2D)
}

val genBuf = IntArray(2)

fun genTexture(textureType: Int): Int {

    GLES20.glGenTextures(2, genBuf, 0)

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

    var eglContext: EGLContext? = null

    var count: Int = 0

    lateinit var surfaceTexture: SurfaceTexture

    lateinit var player: ExoPlayer

    fun getTextureID(): IntArray {

        val currentTextureID = genBuf[count % 2]
        //count++
        //val nextTextureID = genBuf[count % 2]

        runOnUiThread {
            surfaceTexture.updateTexImage()
            //surfaceTexture.detachFromGLContext()
            //surfaceTexture.attachToGLContext(nextTextureID)
        }

        //GLES20.glDeleteTextures(1, genBuf, 0)

        return listOf(currentTextureID, 1920, 1080).toIntArray()
    }

    init {

        System.loadLibrary("ffi_bridge")

        loadJNI()

    }

    private external fun loadJNI()

    override fun setEGLContext(eglContext: EGLContext) {
        this.eglContext = eglContext
        val eglCore = EglCore(eglContext, 0)
        val surface = OffscreenSurface(eglCore, 1, 1)
        surface.makeCurrent()
        GLES20.glGenTextures(2, genBuf, 0)
        //textureId = loadTexture(this, R.drawable.texture2, dimensions)

        runOnUiThread {
            loadPlayer()
        }

    }

    fun loadPlayer() {
        val uri = "android.resource://" + packageName + "/" + R.raw.video_h264_60fps
        val dataSourceFactory = DefaultDataSource.Factory(context)
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri))

        player = ExoPlayer.Builder(this).build()
        player.addMediaSource(mediaSource)
        player.repeatMode = REPEAT_MODE_ALL
        player.prepare()

        val eglCore = EglCore(eglContext, 0)
        val offscreenSurface = OffscreenSurface(eglCore, 1, 1)
        offscreenSurface.makeCurrent()

        surfaceTexture = SurfaceTexture(genBuf[0])
        val surface = Surface(surfaceTexture);

        player.setVideoSurface(surface);
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_READY) {
                    player.play()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()

        surfaceTexture?.release()
    }

}

