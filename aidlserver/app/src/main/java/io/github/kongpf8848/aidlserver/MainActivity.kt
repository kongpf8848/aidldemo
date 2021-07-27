package io.github.kongpf8848.aidlserver

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),MyApplication.IImageCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MyApplication.application.setImageCallback(this)
    }

    override fun onData(bytes: ByteArray?) {
        if (bytes != null && bytes.isNotEmpty()) {
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bitmap != null) {
                iv_pic.setImageBitmap(bitmap)
            }
        }
    }
}