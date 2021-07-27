package io.github.kongpf8848.aidlclient

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.github.kongpf8848.aidlserver.Book
import io.github.kongpf8848.aidlserver.IMyAidlInterface
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var mStub: IMyAidlInterface? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            Log.d("JACK", "onServiceConnected() called with: name = $name, binder = $binder")
            mStub = IMyAidlInterface.Stub.asInterface(binder)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d("JACK", "onServiceDisconnected")
            mStub = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button1.setOnClickListener {
            bindService()
        }
        button2.setOnClickListener {
            try {
                mStub?.addBook(Book(1, "Network"))
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
        button3.setOnClickListener {
            try {
                mStub?.addBook(Book(2, "Computer"))
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
        button4.setOnClickListener {
            try {
                mStub?.bookList?.forEach {
                    Log.d("JACK", "book:${it}")
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
        button5.setOnClickListener {
            sendSmallData()
        }
        button6.setOnClickListener {
            sendLargeData()
        }
    }


    private fun bindService() {
        if (mStub != null) {
            return
        }
        val intent = Intent("io.github.kongpf8848.aidlserver.AidlService")
        intent.setClassName("io.github.kongpf8848.aidlserver","io.github.kongpf8848.aidlserver.AidlService")

        try {
            val bindSucc = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            if (bindSucc) {
                Toast.makeText(this, "bind Service OK", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "bind Service FAIL", Toast.LENGTH_SHORT).show()
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
            val byteArray = AssetUtils.openAssets(this, "large.jpg")
            if (byteArray != null) {
                val memoryFile= MemoryFileUtils.createMemoryFile("image", byteArray.size)
                if(memoryFile!=null) {
                    memoryFile.writeBytes(byteArray, 0, 0, byteArray.size)
                    val fd=MemoryFileUtils.getFileDescriptor(memoryFile)
                    val pfd= ParcelFileDescriptor.dup(fd)
                    mStub?.sendData(pfd)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        if(mStub!=null) {
            unbindService(serviceConnection)
        }
        super.onDestroy()
    }
}