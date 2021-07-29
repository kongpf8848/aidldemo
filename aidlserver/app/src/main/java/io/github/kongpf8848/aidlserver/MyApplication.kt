package io.github.kongpf8848.aidlserver

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.ParcelFileDescriptor

class MyApplication : Application() {

    /**
     * 从客户端接收到数据回调
     */
    private var getDataCallback: OnGetClientDataCallback? = null
    /**
     * 从服务端向客户端发送数据回调
     */
    private var sendDataCallback: OnSendClientDataCallback? = null
    val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when(msg.what){

                1->{
                    val bytes = msg.obj as ByteArray
                    getDataCallback?.onGetClientData(bytes)
                }
                2->{
                    val pfd=msg.obj as ParcelFileDescriptor
                    sendDataCallback?.onSendlientData(pfd)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        application = this
    }

    fun setOnGetClientDataCallback(callback: OnGetClientDataCallback?) {
        this.getDataCallback = callback
    }

    fun setOnSendClientDataCallback(callback: OnSendClientDataCallback?) {
        this.sendDataCallback = callback
    }

    interface OnGetClientDataCallback {
        fun onGetClientData(bytes: ByteArray?)
    }

    interface OnSendClientDataCallback {
        fun onSendlientData(pfd: ParcelFileDescriptor)
    }

    companion object {
        lateinit var application: MyApplication
    }
}