package io.github.kongpf8848.aidlserver

import android.graphics.BitmapFactory
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),MyApplication.OnGetClientDataCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MyApplication.application.setOnGetClientDataCallback(this)
        btn_send_to_client.setOnClickListener {
            try {
                val inputStream = assets.open("server1.jpg")
                val byteArray=inputStream.readBytes()
                val memoryFile= MemoryFile("server_image", byteArray.size)
                memoryFile.writeBytes(byteArray, 0, 0, byteArray.size)
                val fd=MemoryFileUtils.getFileDescriptor(memoryFile)
                val pfd= ParcelFileDescriptor.dup(fd)
                val message= Message().apply {
                    what=2
                    obj=pfd
                }
                MyApplication.application.handler.sendMessage(message)
            }catch (e:RemoteException){
                e.printStackTrace()
            }
        }
    }

    override fun onGetClientData(bytes: ByteArray?) {
        if (bytes != null && bytes.isNotEmpty()) {
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bitmap != null) {
                iv_pic.setImageBitmap(bitmap)
            }
        }
    }
}