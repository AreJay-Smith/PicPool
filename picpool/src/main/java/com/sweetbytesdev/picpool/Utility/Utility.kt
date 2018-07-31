package com.sweetbytesdev.picpool.Utility

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object Utility {

    var HEIGHT: Int = 0
    var WIDTH:Int = 0

    fun setupStatusBarHidden(appCompatActivity: AppCompatActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val w = appCompatActivity.window
            w.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            w.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
    }

    fun showStatusBar(appCompatActivity: AppCompatActivity) {
        synchronized(appCompatActivity) {
            val w = appCompatActivity.window
            val decorView = w.decorView
            // Show Status Bar.
            val uiOptions = View.SYSTEM_UI_FLAG_VISIBLE
            decorView.systemUiVisibility = uiOptions

        }
    }

    fun hideStatusBar(appCompatActivity: AppCompatActivity) {
        synchronized(appCompatActivity) {
            val w = appCompatActivity.window
            val decorView = w.decorView
            // Hide Status Bar.
            val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
            decorView.systemUiVisibility = uiOptions
        }
    }

    fun getScreenSize(activity: Activity) {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        HEIGHT = displayMetrics.heightPixels
        WIDTH = displayMetrics.widthPixels
    }

    fun convertDpToPixel(dp: Float, context: Context): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun convertPixelsToDp(px: Float, context: Context): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun getSoftButtonsBarSizePort(activity: Activity): Int {
        // getRealMetrics is only available with API 17 and +
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val metrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(metrics)
            val usableHeight = metrics.heightPixels
            activity.windowManager.defaultDisplay.getRealMetrics(metrics)
            val realHeight = metrics.heightPixels
            return if (realHeight > usableHeight)
                realHeight - usableHeight
            else
                0
        }
        return 0
    }

    fun writeImage(jpeg: ByteArray): File {
        val dir = File(Environment.getExternalStorageDirectory(), "/DCIM/Camera")
        if (!dir.exists())
            dir.mkdir()
        val photo = File(dir, "IMG_" + SimpleDateFormat("yyyyMMdd_HHmmSS", Locale.ENGLISH).format(Date()) + ".jpg")
        if (photo.exists()) {
            photo.delete()
        }

        try {
            val fos = FileOutputStream(photo.path)
            fos.write(jpeg)
            fos.close()
        } catch (e: Exception) {
            Log.e("PictureDemo", "Exception in photoCallback", e)
        }

        return photo
    }
}