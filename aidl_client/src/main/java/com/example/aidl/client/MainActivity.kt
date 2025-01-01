package com.example.aidl.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.os.*
import android.view.View
import android.widget.ImageView
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aidl.aidl.ICallbackInterface
import com.example.aidl.aidl.IMyAidlInterface
import java.io.FileInputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var btn_bind_service: View? = null
    private var btn_send_to_server: View? = null
    private var iv_pic: ImageView? = null
    private var mStub: IMyAidlInterface? = null
    private val callback=object: ICallbackInterface.Stub() {
        override fun server2client(pfd: ParcelFileDescriptor) {
            val fileDescriptor = pfd.fileDescriptor
            val fis = FileInputStream(fileDescriptor)
            val bytes = fis.readBytes()
            if (bytes != null && bytes.isNotEmpty()) {
                Log.d("JACK", "bytes size:${bytes.size},thread:${Thread.currentThread().name}")
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap != null) {
                    runOnUiThread{
                        iv_pic?.setImageBitmap(bitmap)
                    }

                }
            }
        }

    }
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            Log.d("JACK", "onServiceConnected() called with: name = $name, binder = $binder")
            mStub = IMyAidlInterface.Stub.asInterface(binder)
            mStub?.registerCallback(callback)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d("JACK", "onServiceDisconnected")
            mStub = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_bind_service = findViewById(R.id.btn_bind_service)
        btn_send_to_server = findViewById(R.id.btn_send_to_server)
        iv_pic = findViewById(R.id.iv_pic)
        btn_bind_service?.setOnClickListener {
            bindService()
        }
        btn_send_to_server?.setOnClickListener {
            sendLargeData()
        }
    }


    private fun bindService() {
        if (mStub != null) {
            return
        }
        val intent = Intent("com.example.aidl.server.AidlService")
        intent.setClassName("com.example.aidl.server","com.example.aidl.server.AidlService")

        try {
            val bindSucc = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            if (bindSucc) {
                Toast.makeText(this, "bind ok", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "bind fail", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendSmallData() {
        if (mStub == null) {
            return
        }
        try {
            val byteArray = AssetUtils.openAssets(this, "small.jpg")
            if (byteArray != null) {
               mStub?.sendImage(byteArray)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("JACK","error:${e.message}")
        } catch (e: RemoteException) {
            e.printStackTrace()
            Log.e("JACK","error:${e.message}")
        }
    }

    private fun sendLargeData() {
        if (mStub == null) {
            return
        }
        try {
            /**
             * 读取assets目录下文件
             */
            val inputStream = assets.open("large.jpg")

            /**
             * 将inputStream转换成字节数组
             */
            val byteArray=inputStream.readBytes()

            /**
             * 创建MemoryFile
             */
            val memoryFile=MemoryFile("client_image", byteArray.size)

            /**
             * 向MemoryFile中写入字节数组
             */
            memoryFile.writeBytes(byteArray, 0, 0, byteArray.size)

            /**
             * 获取MemoryFile对应的FileDescriptor
             */
            val fd= MemoryFileUtils.getFileDescriptor(memoryFile)

            /**
             * 根据FileDescriptor创建ParcelFileDescriptor
             */
            val pfd= ParcelFileDescriptor.dup(fd)

            /**
             * 发送数据
             */
            mStub?.client2server(pfd)

        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        if(mStub!=null) {
            mStub?.unregisterCallback(callback)
            unbindService(serviceConnection)
        }
        super.onDestroy()
    }
}