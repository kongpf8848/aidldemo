package io.github.kongpf8848.aidlserver

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Message
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import android.util.Log
import java.io.FileInputStream
import java.util.*

class AidlService : Service() {

    private val list: MutableList<Book> = ArrayList()

    private val mStub: IMyAidlInterface.Stub = object : IMyAidlInterface.Stub() {

        @Throws(RemoteException::class)
        override fun addBook(book: Book) {
            Log.d("JACK", "thread:" + Thread.currentThread().name + " add Book")
            list.add(book)
        }

        @Throws(RemoteException::class)
        override fun getBookList(): List<Book> {
            Log.d("JACK", "thread:" + Thread.currentThread().name + " getBookList")
            return list
        }

        @Throws(RemoteException::class)
        override fun sendData(pfd: ParcelFileDescriptor) {
            Log.d("JACK", "thread:" + Thread.currentThread().name + " sendData")
            /**
             * 从ParcelFileDescriptor中获取FileDescriptor
             */
            val fileDescriptor = pfd.fileDescriptor

            /**
             * 根据FileDescriptor构建InputStream对象
             */
            val fis = FileInputStream(fileDescriptor)

            /**
             * 从InputStream中读取字节数组
             */
            val data = fis.readBytes()
            Log.d("JACK", "bytes size:${data.size}")
            val message = Message().apply {
                what = 1
                obj = data
            }
            MyApplication.application.handler.sendMessage(message)
        }

        @Throws(RemoteException::class)
        override fun sendImage(data:ByteArray) {
            Log.d("JACK", "bytes size:${data.size}")
            val message = Message().apply {
                what = 1
                obj = data
            }
            MyApplication.application.handler.sendMessage(message)
        }



    }

    override fun onBind(intent: Intent): IBinder {
        Log.d("JACK", "onBind")
        return mStub
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("JACK", "onCreate")
    }

    override fun onStart(intent: Intent, startId: Int) {
        super.onStart(intent, startId)
        Log.d("JACK", "onStart")
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d("JACK", "onUnbind")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("JACK", "onDestroy")
    }
}