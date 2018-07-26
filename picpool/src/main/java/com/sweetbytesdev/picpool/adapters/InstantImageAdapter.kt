package com.sweetbytesdev.picpool.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.request.RequestOptions
import com.sweetbytesdev.picpool.Interfaces.OnSelectionListener
import com.sweetbytesdev.picpool.R
import com.sweetbytesdev.picpool.Utility.Utility
import com.sweetbytesdev.picpool.models.Img
import java.util.ArrayList

class InstantImageAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private val context: Context
    private val list: ArrayList<Img>
    private var onSelectionListener: OnSelectionListener? = null
    private val glide: RequestManager
    private val options: RequestOptions

    constructor(context: Context) {
        this.context = context
        this.list = arrayListOf()

        glide = Glide.with(context)
        options = RequestOptions().override(256).transform(CenterCrop()).transform(FitCenter())
    }

    fun addOnSelectionListener(onSelectionListener: OnSelectionListener) {
        this.onSelectionListener = onSelectionListener
    }

    fun getItemList(): ArrayList<Img> {
        return list
    }

    fun addImageList(images: ArrayList<Img>) {
        list.addAll(images)
        notifyDataSetChanged()
    }

    fun clearList() {
        list.clear()
    }

    fun select(selection: Boolean, pos: Int) {
        if (pos < 100) {
            list[pos].isSelected = selection
            notifyItemChanged(pos)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == MainImageAdapter.HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.initial_image, parent, false)
            return HolderNone(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.initial_image, parent, false)
            return Holder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val image = list[position]
        return if (image.contentUrl.isEmpty()) MainImageAdapter.HEADER else MainImageAdapter.ITEM
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val image = list[position]
        if (holder is Holder) {
            val imageHolder = holder as Holder
            val margin = 2
            val size = Utility.convertDpToPixel(72f, context) - 2
            val layoutParams = FrameLayout.LayoutParams(size.toInt(), size.toInt())
            layoutParams.setMargins(margin, margin, margin, margin)
            imageHolder.itemView.setLayoutParams(layoutParams)
            val padding = (size / 3.5).toInt()
            imageHolder.selection.setPadding(padding, padding, padding, padding)
            imageHolder.preview.setLayoutParams(layoutParams)

            glide.load(image.contentUrl).apply(options).into(imageHolder.preview)

            imageHolder.selection.visibility = if (image.isSelected) View.VISIBLE else View.GONE
        } else {
            val noneHolder = holder as HolderNone
            val layoutParams = FrameLayout.LayoutParams(0, 0)
            noneHolder.itemView.layoutParams = layoutParams
            noneHolder.itemView.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class Holder : RecyclerView.ViewHolder, View.OnClickListener, View.OnLongClickListener {

        var preview: ImageView
        var selection: ImageView

        constructor(itemView: View):super(itemView) {
            preview = itemView.findViewById(R.id.preview)
            selection = itemView.findViewById(R.id.selection)
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(view: View) {
            var id = layoutPosition
            onSelectionListener?.OnClick(list[id], view, id)
        }

        override fun onLongClick(view: View?): Boolean {
            var id = layoutPosition
            onSelectionListener?.OnLongClick(list[id], view!!, id)
            return true
        }
    }

    inner class HolderNone : RecyclerView.ViewHolder {
        constructor(itemView: View): super(itemView)
    }
}