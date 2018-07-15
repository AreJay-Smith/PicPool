package com.sweetbytesdev.picpool.PicPool

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import com.sweetbytesdev.picpool.Interfaces.WorkFinish
import com.sweetbytesdev.picpool.R
import com.sweetbytesdev.picpool.Utility.PermUtil
import com.sweetbytesdev.picpool.Utility.Utility

class PicPool : AppCompatActivity(), View.OnTouchListener {

    companion object {

        private val SELECTION = "selection"

        fun start(context: Fragment, requestCode: Int, selectionCount: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PermUtil.checkForCamara_WritePermissions(context, object : WorkFinish {
                    override fun onWorkFinish(check: Boolean) {
                        val i = Intent(context.activity, PicPool::class.java)
                        i.putExtra(SELECTION, selectionCount)
                        context.startActivityForResult(i, requestCode)
                    }
                })
            } else {
                val i = Intent(context.activity, PicPool::class.java)
                i.putExtra(SELECTION, selectionCount)
                context.startActivityForResult(i, requestCode)
            }
        }

        fun start(context: Fragment, requestCode: Int) {
            start(context, requestCode, 1)
        }

        fun start(context: FragmentActivity, requestCode: Int, selectionCount: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PermUtil.checkForCamara_WritePermissions(context, object : WorkFinish {
                    override fun onWorkFinish(check: Boolean) {
                        val i = Intent(context, PicPool::class.java)
                        i.putExtra(SELECTION, selectionCount)
                        context.startActivityForResult(i, requestCode)
                    }
                })
            } else {
                val i = Intent(context, PicPool::class.java)
                i.putExtra(SELECTION, selectionCount)
                context.startActivityForResult(i, requestCode)
            }
        }

        fun start(context: FragmentActivity, requestCode: Int) {
            start(context, requestCode, 1)
        }
    }

    private var SelectionCount = 1
//    private var mCamera: CameraView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utility.setupStatusBarHidden(this)
        Utility.hideStatusBar(this)
        setContentView(R.layout.activity_main_pic_pool)
        initialize()
    }

    private fun initialize() {
        Utility.getScreenSize(this)
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
        try {
            SelectionCount = intent.getIntExtra(SELECTION, 1)
        } catch (e: Exception) {
            e.printStackTrace()
        }

//        mCamera = findViewById<View>(R.id.camera)
//        mCamera.start()
//        mCamera.setFocus(CameraKit.Constants.FOCUS_TAP_WITH_MARKER)

    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return true
    }
}