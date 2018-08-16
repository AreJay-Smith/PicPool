package com.sweetbytesdev.picpool.Utility

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.provider.MediaStore
import com.sweetbytesdev.picpool.models.Img
import java.text.SimpleDateFormat
import java.util.*

open class ImageFetcher : AsyncTask<Cursor, Void, ArrayList<Img>> {

    private var LIST = arrayListOf<Img>()
    private var context: Context

    constructor(context: Context) {
        this.context = context
    }

    override fun doInBackground(vararg params: Cursor?): ArrayList<Img> {
        val cursor = params[0]
        if (cursor != null) {
            val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
            val date = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
            val data = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
            val contentUrl = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            var header = ""
            var limit = 100
            if (cursor.count < 100) {
                limit = cursor.count
            }
            cursor.move(limit - 1)
            for (i in limit until cursor.count) {
                cursor.moveToNext()
                val curl = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + cursor.getInt(contentUrl))
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = cursor.getLong(date)
                val dateDifference = Utility.getDateDifference(context, calendar)

                if (!header.equals(dateDifference, ignoreCase = true)) {
                    header = dateDifference
                    LIST.add(Img(dateDifference, "", "", false, dateFormat.format(calendar.time), 0))
                }
                LIST.add(Img(header, curl.toString(), cursor.getString(data), false, dateFormat.format(calendar.time), 0))
            }
            cursor.close()
        }
        return LIST
    }
}