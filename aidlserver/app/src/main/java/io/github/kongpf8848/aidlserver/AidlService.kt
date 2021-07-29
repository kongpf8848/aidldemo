package io.github.kongpf8848.aidlserver

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import java.io.FileInputStream
import java.util.*

class AidlService : Service() {

    private val list: MutableList<Book> = ArrayList()
    private val callbacks=RemoteCallbackList<ICallbackInterface>()

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
        override fun sendImage(data:ByteArray) {
            Log.d("JACK", "bytes size:${data.size}")
            val message = Message().apply {
                what = 1
                obj = data
            }
            MyApplication.application.handler.sendMessage(message)
        }

        @Throws(RemoteException::class)
        override fun client2server(pfd: ParcelFileDescriptor) {
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


        override fun registerCallback(callback: ICallbackInterface) {
            callbacks.register(callback)
        }

        override fun unregisterCallback(callback: ICallbackInterface) {
            callbacks.unregister(callback)
        }
    }

    //服务端发送数据到客户端
    private fun server2client(pfd:ParcelFileDescriptor){
        val n=callbacks.beginBroadcast()
        for(i in 0 until n){
            val callback=callbacks.getBroadcastItem(i);
            if (callback!=null){
                try {
                    callback.server2client(pfd)
                } catch (e:RemoteException) {
                    e.printStackTrace()
                }
            }
        }
        callbacks.finishBroadcast()
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d("JACK", "onBind")
        return mStub
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("JACK", "onCreate")
        MyApplication.application.setOnSendClientDataCallback(object:MyApplication.OnSendClientDataCallback{
            override fun onSendlientData(pfd: ParcelFileDescriptor) {
                server2client(pfd)
            }
        })
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