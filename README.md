# aidldemo
**使用AIDL+匿名共享内存实现跨进程双向通信和大文件传输。**

![demo.gif](https://github.com/kongpf8848/aidldemo/blob/master/images/demo.gif)

# AIDL简介
```AIDL```是```Android```中实现跨进程通信(```Inter-Process Communication```)的一种方式。```AIDL```的传输数据机制基于```Binder```，```Binder```对传输数据大小有限制，
传输超过1M的文件就会报```android.os.TransactionTooLargeException```异常，一种解决办法就是使用匿名共享内存进行大文件传输。

![](https://github.com/kongpf8848/aidldemo/blob/master/images/pipe.png)

# 共享内存简介
共享内存是进程间通信的一种方式，通过映射一块公共内存到各自的进程空间来达到共享内存的目的。

![](https://github.com/kongpf8848/aidldemo/blob/master/images/shmem.png)

对于进程间需要传递大量数据的场景下，这种通信方式是十分高效的，但是共享内存并未提供同步机制，也就是说，在第一个进程结束对共享内存的写操作之前，并无自动机制可以阻止第二个进程开始对它进行读取，所以我们通常需要用其他的机制来同步对共享内存的访问，例如信号量。

```Android```中的匿名共享内存(Ashmem)是基于```Linux```共享内存的，借助```Binder```+文件描述符(```FileDescriptor```)实现了共享内存的传递。它可以让多个进程操作同一块内存区域，并且除了物理内存限制，没有其他大小限制。相对于```Linux```的共享内存，Ashmem对内存的管理更加精细化，并且添加了互斥锁。```Java```层在使用时需要用到```MemoryFile```，它封装了```native```代码。```Android```平台上共享内存通常的做法如下：
* 进程A通过```MemoryFile```创建共享内存，得到fd(```FileDescriptor```)
* 进程A通过fd将数据写入共享内存
* 进程A将fd封装成实现```Parcelable```接口的```ParcelFileDescriptor```对象，通过```Binder```将```ParcelFileDescriptor```对象发送给进程B
* 进程B获从```ParcelFileDescriptor```对象中获取fd，从fd中读取数据

# 客户端和服务端双向通信+传输大文件实战

我们先实现客户端向服务端传输大文件，然后再实现服务端向客户端传输大文件。

## 定义AIDL接口
```java
//IMyAidlInterface.aidl
interface IMyAidlInterface {
    void client2server(in ParcelFileDescriptor pfd);
}
```
## 服务端
1. **实现```IMyAidlInterface```接口**
```kotlin
//AidlService.kt
class AidlService : Service() {

    private val mStub: IMyAidlInterface.Stub = object : IMyAidlInterface.Stub() {

        @Throws(RemoteException::class)
        override fun sendData(pfd: ParcelFileDescriptor) {
          
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return mStub
    }
}
``` 
2. **接收数据**
```kotlin
//AidlService.kt
@Throws(RemoteException::class)
override fun sendData(pfd: ParcelFileDescriptor) {

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
    
    ......
}

```

## 客户端
1. **绑定服务**
    * 在项目的```src```目录中加入```.aidl```文件
    * 声明一个```IMyAidlInterface```接口实例（基于```AIDL```生成）
    * 创建```ServiceConnection```实例，实现```android.content.ServiceConnection```接口
    * 调用```Context.bindService()```绑定服务，传入```ServiceConnection```实例
    * 在```onServiceConnected()```实现中，调用```IMyAidlInterface.Stub.asInterface(binder)```，将返回参数转换为```IMyAidlInterface```类型 
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
                Toast.makeText(this, "bind ok", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "bind fail", Toast.LENGTH_SHORT).show()
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
2. **发送数据**
     * 将发送文件转换成字节数组```ByteArray```
     * 创建```MemoryFile```对象
     * 向```MemoryFile```对象中写入字节数组
     * 获取```MemoryFile```对应的```FileDescriptor```
     * 根据```FileDescriptor```创建```ParcelFileDescriptor```
     * 调用```IPC```方法，发送```ParcelFileDescriptor```对象
```kotlin
//MainActivity.kt
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
	mStub?.client2server(pfd)

    } catch (e: IOException) {
	e.printStackTrace()
    } catch (e: RemoteException) {
	e.printStackTrace()
    }
}
```  
至此，我们已经实现了客户端向服务端传输大文件，下面就继续实现服务端向客户端传输大文件功能。
服务端主动给客户端发送数据，客户端只需要进行监听即可。
* 定义监听回调接口
```java
//ICallbackInterface.aidl
package io.github.kongpf8848.aidlserver;

interface ICallbackInterface {
    void server2client(in ParcelFileDescriptor pfd);
}
```
* 在```IMyAidlInterface.aidl```中添加注册回调和反注册回调方法，如下：
```java
//IMyAidlInterface.aidl
import io.github.kongpf8848.aidlserver.ICallbackInterface;

interface IMyAidlInterface {

    ......

    void registerCallback(ICallbackInterface callback);

    void unregisterCallback(ICallbackInterface callback);
}
```
* 服务端实现接口方法
```kotlin
//AidlService.kt
private val callbacks=RemoteCallbackList<ICallbackInterface>()

private val mStub: IMyAidlInterface.Stub = object : IMyAidlInterface.Stub() {

     ......

    override fun registerCallback(callback: ICallbackInterface) {
        callbacks.register(callback)
    }

    override fun unregisterCallback(callback: ICallbackInterface) {
        callbacks.unregister(callback)
    }
}
```
* 客户端绑定服务后注册回调
```kotlin
//MainActivity.kt
private val callback=object: ICallbackInterface.Stub() {
    override fun server2client(pfd: ParcelFileDescriptor) {
        val fileDescriptor = pfd.fileDescriptor
        val fis = FileInputStream(fileDescriptor)
        val bytes = fis.readBytes()
        if (bytes != null && bytes.isNotEmpty()) {
           ......
        }
    }

}

private val serviceConnection = object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        mStub = IMyAidlInterface.Stub.asInterface(binder)
        mStub?.registerCallback(callback)
    }

    override fun onServiceDisconnected(name: ComponentName) {
        mStub = null
    }
}
```
* 服务端发送文件，回调给客户端。此处仅贴出核心代码，如下：
```kotlin
//AidlService.kt
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
```
至此，我们实现了客户端和服务端双向通信和传输大文件😉😉😉
# 其他
* [AIDL官方文档](https://developer.android.google.cn/guide/components/aidl.html?hl=zh-cn)

* [AIDL使用文档](https://github.com/kongpf8848/aidldemo/blob/master/aidl.md)
