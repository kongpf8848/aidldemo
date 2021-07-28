# aidldemo
使用AIDL+共享内存实现跨进程大文件传输。

# AIDL简介
AIDL是Android中实现跨进程通信(Inter-Process Communication)的一种方式。AIDL的传输数据机制基于Binder，Binder对传输数据大小有限制，
传输超过1M的文件就会报```android.os.TransactionTooLargeException```异常，一种解决办法就是使用共享内存进行大文件传输。

#AIDL大文件传输实战

## 定义AIDL接口
```java
//IMyAidlInterface.aidl
interface IMyAidlInterface {
    //传输数据
    void sendData(in ParcelFileDescriptor pfd);
}
```
## 服务端
* 实现接口
```kotlin
//AidlService.kt
class AidlService : Service() {

    private val mStub: IMyAidlInterface.Stub = object : IMyAidlInterface.Stub() {

        @Throws(RemoteException::class)
        override fun sendData(pfd: ParcelFileDescriptor) {
            val fileDescriptor = pfd.fileDescriptor
            val fis = FileInputStream(fileDescriptor)
            val data = fis.readBytes()
			......
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return mStub
    }
}
``` 

## 客户端
* 绑定服务
* 在项目的 src/目录中加入.aidl文件
* 声明一个IMyAidlInterface接口实例（基于AIDL生成）
* 实现android.content.ServiceConnection接口
* 调用Context.bindService()绑定服务，传入ServiceConnection实现实例
* 在onServiceConnected()实现中，调用IMyAidlInterface.Stub.asInterface(binder)，将返回的参数转换为IMyAidlInterface类型 
```kotlin
//MainActivity.kt
class MainActivity : AppCompatActivity() {

    private var mStub: IMyAidlInterface? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            mStub = IMyAidlInterface.Stub.asInterface(binder)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mStub = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button1.setOnClickListener {
            bindService()
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

    override fun onDestroy() {
        if(mStub!=null) {
            unbindService(serviceConnection)
        }
        super.onDestroy()
    }
}
```  
* 发送数据
 * 将发送文件转换成字节数组
 * 创建MemoryFile
 * 向MemoryFile中写入字节数组
 * 获取MemoryFile对应的FileDescriptor
 * 根据FileDescriptor创建ParcelFileDescriptor
 * 发送ParcelFileDescriptor对象
```kotlin
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
		val memoryFile=MemoryFile("image", byteArray.size)

		/**
		 * 向MemoryFile中写入字节数组
		 */
		memoryFile.writeBytes(byteArray, 0, 0, byteArray.size)

		/**
		 * 获取MemoryFile对应的FileDescriptor
		 */
		val fd=MemoryFileUtils.getFileDescriptor(memoryFile)

		/**
		 * 根据FileDescriptor创建ParcelFileDescriptor
		 */
		val pfd= ParcelFileDescriptor.dup(fd)

		/**
		 * 发送数据
		 */
		mStub?.sendData(pfd)

	} catch (e: IOException) {
		e.printStackTrace()
	} catch (e: RemoteException) {
		e.printStackTrace()
	}
}
```  
