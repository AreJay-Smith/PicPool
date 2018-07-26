package com.sweetbytesdev.picpool.models

import java.io.Serializable

data class Img(var headerDate: String,
               var contentUrl: String,
               var url: String,
               var isSelected: Boolean,
               var scrollerDate: String,
               var position: Int) : Serializable {

}