package com.sweetbytesdev.picpool.PicPool

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.sweetbytesdev.picpool.Interfaces.WorkFinish
import com.sweetbytesdev.picpool.R
import com.sweetbytesdev.picpool.Utility.HeaderItemDecoration
import com.sweetbytesdev.picpool.Utility.PermUtil
import com.sweetbytesdev.picpool.Utility.Utility
import com.sweetbytesdev.picpool.adapters.MainImageAdapter
import com.sweetbytesdev.picpool.adapters.InstantImageAdapter
import com.sweetbytesdev.picpool.models.Img
import com.wonderkiln.camerakit.CameraKit
import com.wonderkiln.camerakit.CameraKitEventCallback
import com.wonderkiln.camerakit.CameraKitImage
import com.wonderkiln.camerakit.CameraView
import java.util.ArrayList
import java.util.HashSet

class PicPool : AppCompatActivity(), View.OnTouchListener {

    companion object {

        private val SELECTION = "selection"
        val IMAGE_RESULTS = "image_results"
        var TOPBAR_HEIGHT: Float = 0.toFloat()

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utility.setupStatusBarHidden(this)
        Utility.hideStatusBar(this)
        setContentView(R.layout.activity_main_pic_pool)
        initialize()
    }

    private var mCamera: CameraView? = null
    private var colorPrimaryDark: Int = 0
    private var mHandleView: ImageView? = null
    private var clickme: ImageView? = null
    private var mCaptureRing: ImageView? = null
    private var selection_back: ImageView? = null
    private var selection_check: ImageView? = null
    private var mFlashContainer: FrameLayout? = null
    private var mCameraToggle: ImageView? = null
    private var mTopBar: FrameLayout? = null
    private var selection_count: TextView? = null
    private var selection_ok: TextView? = null
    private var sendButton: FrameLayout? = null
    private var img_count: TextView? = null
    private var mBubbleView: TextView? = null
    private var mScrollbar: FrameLayout? = null
    private var bottomButtons: FrameLayout? = null
    private var status_bar_bg: View? = null
    private var recyclerView: RecyclerView? = null
    private var instantRecyclerView:RecyclerView? = null
    private var initializeAdapter:InstantImageAdapter? = null
    private var mainFrameLayout:FrameLayout? = null
    private var bottomBarHeight = 0
    private var mainImageAdapter: MainImageAdapter? = null
    private var mLayoutManager: GridLayoutManager? = null
    private val selectionList = HashSet<Img>()
    private var mBottomSheetBehavior: BottomSheetBehavior? = null

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
        colorPrimaryDark = ResourcesCompat.getColor(resources, R.color.colorPrimaryPicPool, theme)
        mCamera = findViewById<CameraView>(R.id.camera)
        mCamera?.start()
        mCamera?.setFocus(CameraKit.Constants.FOCUS_TAP_WITH_MARKER)

        mCaptureRing = findViewById(R.id.img_ring)
        mFlashContainer = findViewById(R.id.con_flash)
        mCameraToggle = findViewById(R.id.img_camera_toggle)
        mTopBar = findViewById(R.id.con_topbar)
        selection_count = findViewById(R.id.tv_selection_count)
        selection_ok = findViewById(R.id.selection_ok)
        selection_back = findViewById(R.id.selection_back)
        selection_check = findViewById(R.id.selection_check)
        selection_check?.visibility = if (SelectionCount > 1) View.VISIBLE else View.GONE
        sendButton = findViewById(R.id.sendButton)
        img_count = findViewById(R.id.img_count)

        mBubbleView = findViewById(R.id.fastscroll_bubble)
        mBubbleView?.visibility = View.GONE
        mHandleView = findViewById(R.id.fastscroll_handle)
        mScrollbar = findViewById(R.id.fastscroll_scrollbar)
        mScrollbar?.visibility = View.GONE

        bottomButtons = findViewById(R.id.bottomButtons)
        TOPBAR_HEIGHT = Utility.convertDpToPixel(56f, this)
        status_bar_bg = findViewById(R.id.status_bar_bg)
        instantRecyclerView = findViewById(R.id.instantRecyclerView)
        initializeAdapter = InstantImageAdapter(this)
        initializeAdapter?.addOnSelectionListener(onSelectionListener)
        instantRecyclerView?.adapter = initializeAdapter
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView?.addOnScrollListener(mScrollListener)
        mainFrameLayout = findViewById(R.id.mainFrameLayout)
        bottomBarHeight = Utility.getSoftButtonsBarSizePort(this)

        val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT)
        lp.setMargins(0, 0, 0, bottomBarHeight)
        mainFrameLayout?.layoutParams = lp
        val layoutParams = sendButton?.layoutParams as FrameLayout.LayoutParams
        layoutParams.setMargins(0, 0, Utility.convertDpToPixel(16f, this).toInt(),
                Utility.convertDpToPixel(174f, this) as Int)
        sendButton?.layoutParams = layoutParams

        // Set up main recyclerview
        mainImageAdapter = MainImageAdapter(this)
        mLayoutManager = GridLayoutManager(this, MainImageAdapter.SPAN_COUNT)
        mLayoutManager?.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (mainImageAdapter?.getItemViewType(position) === MainImageAdapter.HEADER) {
                    MainImageAdapter.SPAN_COUNT
                } else 1
            }
        }
        recyclerView?.layoutManager = mLayoutManager
        mainImageAdapter?.addOnSelectionListener(onSelectionListener)
        recyclerView?.adapter = mainImageAdapter
        recyclerView?.addItemDecoration(HeaderItemDecoration(this, recyclerView!!, mainImageAdapter!!))

        mHandleView?.setOnTouchListener(this)

        // Taking the picture
        clickme?.setOnClickListener(View.OnClickListener {
            mCamera?.captureImage(CameraKitEventCallback { cameraKitImage ->
                if (cameraKitImage.jpeg != null) {
                    synchronized(cameraKitImage) {
                        val photo = Utility.writeImage(cameraKitImage.jpeg)
                        selectionList.clear()
                        selectionList.add(Img("", "", photo.getAbsolutePath(), false, "", -1))
                        returnObjects()
                    }
                } else {
                    Toast.makeText(this, "Unable to Get The Image", Toast.LENGTH_SHORT).show()
                }
            })
            // Toast.makeText(Pix.this, "fin", Toast.LENGTH_SHORT).show();
            //Log.e("Hello", "onclick");
        })

        // Ok'ing the chosen selection
        selection_ok?.setOnClickListener {
            returnObjects()
        }

        sendButton?.setOnClickListener {
            returnObjects()
        }

        selection_back.setOnClickListener {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
        }
    }

    fun returnObjects() {
        val list = ArrayList<String>()
        for (i in selectionList) {
            list.add(i.url)
            // Log.e("Pix images", "img " + i.getUrl());
        }
        val resultIntent = Intent()
        resultIntent.putStringArrayListExtra(IMAGE_RESULTS, list)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return true
    }
}