package com.sweetbytesdev.picpool.Interfaces

import android.view.View
import com.sweetbytesdev.picpool.models.Img

interface OnSelectionListener {

    abstract fun OnClick(Img: Img, view: View, position: Int)

    abstract fun OnLongClick(img: Img, view: View, position: Int)
}