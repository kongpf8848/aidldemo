package io.github.kongpf8848.aidlserver

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.os.Message

class MyApplication : Application() {

    private var callback: IImageCallback? = null
    val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what == 1) {
                val bytes = msg.obj as ByteArray
                callback?.onData(bytes)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        application = this
    }

    fun setImageCallback(callback: IImageCallback?) {
        this.callback = callback
    }

    interface IImageCallback {
        fun onData(bytes: ByteArray?)
    }

    companion object {
        lateinit var application: MyApplication
    }
}