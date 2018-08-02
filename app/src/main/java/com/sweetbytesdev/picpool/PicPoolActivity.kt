package com.sweetbytesdev.picpool

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.sweetbytesdev.picpool.PicPool.PicPool

class PicPoolActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pic_pool)

        var button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            PicPool.start(this, 2)
        }
    }
}
