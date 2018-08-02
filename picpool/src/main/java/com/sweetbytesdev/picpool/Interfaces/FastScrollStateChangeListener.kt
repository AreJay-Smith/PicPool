package com.sweetbytesdev.picpool.Interfaces

import com.sweetbytesdev.picpool.PicPool.PicPool

interface FastScrollStateChangeListener {
    /**
     * Called when fast scrolling begins
     */
    abstract fun onFastScrollStart(fastScroller: PicPool)

    /**
     * Called when fast scrolling ends
     */
    abstract fun onFastScrollStop(fastScroller: PicPool)
}