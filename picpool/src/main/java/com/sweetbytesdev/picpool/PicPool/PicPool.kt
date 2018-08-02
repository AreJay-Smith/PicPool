package com.sweetbytesdev.picpool.PicPool

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MotionEvent
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.sweetbytesdev.picpool.Interfaces.FastScrollStateChangeListener
import com.sweetbytesdev.picpool.Interfaces.WorkFinish
import com.sweetbytesdev.picpool.R
import com.sweetbytesdev.picpool.Utility.*
import com.sweetbytesdev.picpool.adapters.MainImageAdapter
import com.sweetbytesdev.picpool.adapters.InstantImageAdapter
import com.sweetbytesdev.picpool.models.Img
import com.wonderkiln.camerakit.CameraKit
import com.wonderkiln.camerakit.CameraKitEventCallback
import com.wonderkiln.camerakit.CameraKitImage
import com.wonderkiln.camerakit.CameraView
import java.util.*

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
    private val sBubbleAnimDuration = 1000
    private val sScrollbarHideDelay = 1000
    private val mFastScrollStateChangeListener: FastScrollStateChangeListener? = null
    private val sTrackSnapRange = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utility.setupStatusBarHidden(this)
        Utility.hideStatusBar(this)
        setContentView(R.layout.activity_main_pic_pool)
        initialize()
    }

    override fun onResume() {
        super.onResume()
        mCamera?.start()
    }

    override fun onPause() {
        mCamera?.stop()
        super.onPause()
    }

    private var mCamera: CameraView? = null
    private var colorPrimaryDark: Int = 0
    private var mHandleView: ImageView? = null
    private var clickme: ImageView? = null
    private var selection_back: ImageView? = null
    private var selection_check: ImageView? = null
    private var selection_count: TextView? = null
    private var selection_ok: TextView? = null
    private var sendButton: FrameLayout? = null
    private var img_count: TextView? = null
    private var mBubbleView: TextView? = null
    private var mScrollbar: View? = null
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
    private var mBottomSheetBehavior: BottomSheetBehavior<*>? = null
    private var topbar: View? = null
    private var LongSelection = false

    private var flash: FrameLayout? = null
    private var front: ImageView? = null

    private var mViewHeight: Float = 0.toFloat()
    private val handler = Handler()

    private var mScrollbarAnimator: ViewPropertyAnimator? = null
    private val mScrollbarHider = Runnable { hideScrollbar() }
    private val mHideScrollbar = true

    private var mBubbleAnimator: ViewPropertyAnimator? = null

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

        clickme = findViewById(R.id.img_ring)
        flash = findViewById(R.id.con_flash)
        front = findViewById(R.id.img_camera_toggle)
        topbar = findViewById(R.id.con_topbar)
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

        selection_back?.setOnClickListener {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
        }
        selection_check?.setOnClickListener(View.OnClickListener {
            topbar?.setBackgroundColor(colorPrimaryDark)
            selection_count?.text = resources.getString(R.string.tap_to_select)
            img_count?.text = selectionList.size.toString()
            DrawableCompat.setTint(selection_back?.drawable!!, Color.parseColor("#ffffff"))
            LongSelection = true
            selection_check?.visibility = View.GONE
        })

        val iv = flash?.getChildAt(0) as ImageView
        mCamera?.flash = CameraKit.Constants.FLASH_AUTO
        flash?.setOnClickListener(View.OnClickListener {
            val height = flash?.height
            iv.animate().translationY(height.toFloat()).setDuration(100).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    iv.translationY = (-(height / 2)).toFloat()
                    when (mCamera?.flash) {
                        CameraKit.Constants.FLASH_ON -> {
                            iv.setImageResource(R.drawable.ic_flash_auto)
                            mCamera?.flash = CameraKit.Constants.FLASH_AUTO
                        }
                        CameraKit.Constants.FLASH_AUTO -> {
                            iv.setImageResource(R.drawable.ic_flash_off)
                            mCamera?.flash = CameraKit.Constants.FLASH_OFF
                        }
                        else -> {
                            iv.setImageResource(R.drawable.ic_flash_on)
                            mCamera?.flash = CameraKit.Constants.FLASH_ON
                        }
                    }

                    iv.animate().translationY(0f).setDuration(50).setListener(null).start()
                }
            }).start()
        })

        front?.setOnClickListener(View.OnClickListener {
            val oa1 = ObjectAnimator.ofFloat(front, "scaleX", 1f, 0f).setDuration(150)
            val oa2 = ObjectAnimator.ofFloat(front, "scaleX", 0f, 1f).setDuration(150)
            oa1.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    front?.setImageResource(R.drawable.ic_photo_camera)
                    oa2.start()
                }
            })
            oa1.start()
            mCamera?.facing = if (mCamera?.getFacing() == CameraKit.Constants.FACING_FRONT) CameraKit.Constants.FACING_BACK else CameraKit.Constants.FACING_FRONT
        })
        DrawableCompat.setTint(selection_back?.drawable!!, colorPrimaryDark)

        updateImages()
    }

    private fun updateImages() {
        mainImageAdapter?.clearList()
        val cursor = Utility.getCursor(this)
        val INSTANTLIST = ArrayList<Img>()
        var header = ""
        var limit = 100
        if (cursor?.count!! < 100) {
            limit = cursor?.count
        }
        val date = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
        val data = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
        val contentUrl = cursor.getColumnIndex(MediaStore.Images.Media._ID)
        var calendar: Calendar

        try {
            for (i in 0 until limit) {
                cursor.moveToNext()
                val path = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + cursor.getInt(contentUrl))
                calendar = Calendar.getInstance()
                calendar.timeInMillis = cursor.getLong(date)
                val dateDifference = Utility.getDateDifference(this, calendar)
                if (!header.equals("" + dateDifference, ignoreCase = true)) {
                    header = "" + dateDifference
                    INSTANTLIST.add(Img("" + dateDifference, "", "", false, "", -1))
                }
                INSTANTLIST.add(Img("" + header, "" + path, cursor.getString(data), false, "", -1))
            }
        } finally {
            cursor.close()
        }


        // TODO: Replace with RxJava
        object : ImageFetcher(this) {
            override fun onPostExecute(imgs: ArrayList<Img>) {
                super.onPostExecute(imgs)
                mainImageAdapter?.addImageList(imgs)
            }
        }.execute(Utility.getCursor(this))
        initializeAdapter?.addImageList(INSTANTLIST)
        mainImageAdapter?.addImageList(INSTANTLIST)
        setBottomSheetBehavior()
    }

    private fun setBottomSheetBehavior() {
        val bottomSheet = findViewById<View>(R.id.bottom_sheet)
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        mBottomSheetBehavior?.peekHeight = Utility.convertDpToPixel(194f, this).toInt()
        mBottomSheetBehavior?.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                Utility.manipulateVisibility(this@PicPool, slideOffset,
                        instantRecyclerView!!, recyclerView!!, status_bar_bg!!,
                        topbar!!, bottomButtons!!, sendButton!!, LongSelection)
                if (slideOffset == 1f) {
                    Utility.showScrollbar(mScrollbar!!, this@PicPool)
                    mainImageAdapter?.notifyDataSetChanged()
                    mViewHeight = mScrollbar?.measuredHeight!!.toFloat()
                    handler.post(Runnable { setViewPositions(getScrollProportion(recyclerView!!)) })
                    sendButton?.visibility = View.GONE
                    mCamera?.stop()
                } else if (slideOffset == 0f) {

                    initializeAdapter?.notifyDataSetChanged()
                    hideScrollbar()
                    img_count?.text = selectionList.size.toString()
                    mCamera?.start()
                }
            }
        })
    }

    private fun hideScrollbar() {
        val transX = resources.getDimensionPixelSize(R.dimen.fastscroll_scrollbar_padding_end).toFloat()
        mScrollbarAnimator = mScrollbar?.animate()?.translationX(transX)?.alpha(0f)
                ?.setDuration(Constants.sScrollbarAnimDuration.toLong())
                ?.setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        mScrollbar?.visibility = View.GONE
                        mScrollbarAnimator = null
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        super.onAnimationCancel(animation)
                        mScrollbar?.visibility = View.GONE
                        mScrollbarAnimator = null
                    }
                })
    }

    private fun setViewPositions(y: Float) {
        val handleY = Utility.getValueInRange(0, (mViewHeight - mHandleView!!.height).toInt(), (y - mHandleView!!.height / 2).toInt())
        mBubbleView?.y = handleY + Utility.convertDpToPixel(56f, this@PicPool)
        mHandleView?.y = handleY.toFloat()
    }

    private fun getScrollProportion(recyclerView: RecyclerView): Float {
        val verticalScrollOffset = recyclerView.computeVerticalScrollOffset()
        val verticalScrollRange = recyclerView.computeVerticalScrollRange()
        val rangeDiff = verticalScrollRange - mViewHeight
        val proportion = verticalScrollOffset.toFloat() / if (rangeDiff > 0) rangeDiff else 1f
        return mViewHeight * proportion
    }

    private fun returnObjects() {
        val list = ArrayList<String>()
        for (i in selectionList) {
            list.add(i.url!!)
            // Log.e("Pix images", "img " + i.getUrl());
        }
        val resultIntent = Intent()
        resultIntent.putStringArrayListExtra(IMAGE_RESULTS, list)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun setRecyclerViewPosition(y: Float) {
        if (recyclerView != null && recyclerView?.adapter != null) {
            val itemCount = recyclerView?.adapter!!.itemCount
            val proportion: Float

            if (mHandleView?.y == 0f) {
                proportion = 0f
            } else if (mHandleView!!.y + mHandleView!!.height >= mViewHeight - sTrackSnapRange) {
                proportion = 1f
            } else {
                proportion = y / mViewHeight
            }

            val scrolledItemCount = Math.round(proportion * itemCount)
            val targetPos = Utility.getValueInRange(0, itemCount - 1, scrolledItemCount)
            recyclerView?.layoutManager?.scrollToPosition(targetPos)

            if (mainImageAdapter != null) {
                mBubbleView?.text = mainImageAdapter?.getSectionMonthYearText(targetPos)
            }
        }
    }

    private fun showBubble() {
        if (!Utility.isViewVisible(mBubbleView)) {
            mBubbleView?.visibility = View.VISIBLE
            mBubbleView?.alpha = 0f
            mBubbleAnimator = mBubbleView?.animate()?.alpha(1f)
                    ?.setDuration(sBubbleAnimDuration.toLong())
                    ?.setListener(object : AnimatorListenerAdapter() {

                        // adapter required for new alpha value to stick
                    })
            mBubbleAnimator?.start()
        }
    }

    private fun hideBubble() {
        if (Utility.isViewVisible(mBubbleView)) {
            mBubbleAnimator = mBubbleView?.animate()?.alpha(0f)
                    ?.setDuration(sBubbleAnimDuration.toLong())
                    ?.setListener(object : AnimatorListenerAdapter() {

                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            mBubbleView?.visibility = View.GONE
                            mBubbleAnimator = null
                        }

                        override fun onAnimationCancel(animation: Animator) {
                            super.onAnimationCancel(animation)
                            mBubbleView?.visibility = View.GONE
                            mBubbleAnimator = null
                        }
                    })
            mBubbleAnimator?.start()
        }
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (event.x < mHandleView!!.x - ViewCompat.getPaddingStart(mHandleView)) {
                    return false
                }
                mHandleView?.isSelected = true
                handler.removeCallbacks(mScrollbarHider)
                Utility.cancelAnimation(mScrollbarAnimator)
                Utility.cancelAnimation(mBubbleAnimator)

                if (!Utility.isViewVisible(mScrollbar) && recyclerView!!.computeVerticalScrollRange() - mViewHeight > 0) {
                    mScrollbarAnimator = Utility.showScrollbar(mScrollbar!!, this@PicPool)
                }

                if (mainImageAdapter != null) {
                    showBubble()
                }

                if (mFastScrollStateChangeListener != null) {
                    mFastScrollStateChangeListener.onFastScrollStart(this)
                }
                val y = event.rawY
                mBubbleView.setText(mainImageAdapter?.getSectionText(recyclerView!!.verticalScrollbarPosition))
                setViewPositions(y - TOPBAR_HEIGHT)
                setRecyclerViewPosition(y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val y = event.rawY
                mBubbleView?.text = mainImageAdapter?.getSectionText(recyclerView!!.verticalScrollbarPosition)
                setViewPositions(y - TOPBAR_HEIGHT)
                setRecyclerViewPosition(y)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mHandleView?.isSelected = false
                if (mHideScrollbar) {
                    handler.postDelayed(mScrollbarHider, sScrollbarHideDelay.toLong())
                }
                hideBubble()
                if (mFastScrollStateChangeListener != null) {
                    mFastScrollStateChangeListener.onFastScrollStop(this)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onBackPressed() {
        if (selectionList.size > 0) {
            for (img in selectionList) {
                mainImageAdapter!!.getItemList()[img.position!!].isSelected = false
                mainImageAdapter?.notifyItemChanged(img.position!!)
                initializeAdapter?.getItemList()?.get(img.position!!)?.isSelected = false
                initializeAdapter?.notifyItemChanged(img.position!!)
            }
            LongSelection = false
            if (SelectionCount > 1) {
                selection_check?.visibility = View.VISIBLE
            }
            DrawableCompat.setTint(selection_back!!.drawable, colorPrimaryDark)
            topbar?.setBackgroundColor(Color.parseColor("#ffffff"))
            val anim = ScaleAnimation(
                    1f, 0f, // Start and end values for the X axis scaling
                    1f, 0f, // Start and end values for the Y axis scaling
                    Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                    Animation.RELATIVE_TO_SELF, 0.5f) // Pivot point of Y scaling
            anim.fillAfter = true // Needed to keep the result of the animation
            anim.duration = 300
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    sendButton?.visibility = View.GONE
                    sendButton?.clearAnimation()
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
            sendButton?.startAnimation(anim)
            selectionList.clear()
        } else if (mBottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior?.setState(BottomSheetBehavior.STATE_COLLAPSED)
        } else {
            super.onBackPressed()
        }
    }
}