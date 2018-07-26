package com.sweetbytesdev.picpool.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.request.RequestOptions
import com.sweetbytesdev.picpool.Interfaces.OnSelectionListener
import com.sweetbytesdev.picpool.Utility.Utility
import com.sweetbytesdev.picpool.models.Img
import java.util.ArrayList

class MainImageAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>, HeaderItemDecoration {

    companion object {
        val HEADER = 1
        val ITEM = 2
        val SPAN_COUNT = 3
        private val MARGIN = 2
    }

    private val context: Context
    private val list: ArrayList<Img>
    private var onSelectionListener: OnSelectionListener? = null
    private val layoutParams: FrameLayout.LayoutParams
    private val glide: RequestManager
    private val options: RequestOptions

    constructor(context: Context) {
        this.context = context
        this.list = arrayListOf()

        var size = Utility.WIDTH / SPAN_COUNT
        layoutParams = FrameLayout.LayoutParams(size, size)
        layoutParams.setMargins(MARGIN, MARGIN - 1, MARGIN, MARGIN - 1)
        options = RequestOptions().override(360).transform(CenterCrop()).transform(FitCenter())
        glide = Glide.with(context)
    }
}