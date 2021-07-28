# aidldemo
使用AIDL(Android 接口定义语言)跨进程传输大文件示例。

# 什么是AIDL

```AIDL(Android Interface Definition Language)```，即Android接口定义语言，是Android中实现跨进程通信(Inter-Process Communication)的一种方式。在 Android 中，一个进程通常无法访问另一个进程的内存。因此，为进行通信，进程需将其对象分解成可供操作系统理解的原语，并将其编组为可供操作的对象。编写执行该编组操作的代码较为繁琐，因此 Android 会使用 AIDL 处理此问题。

# AIDL的使用

1. 创建.aidl文件

  必须使用 Java 编程语言构建 `.aidl` 文件。每个 `.aidl` 文件均须定义单个接口，并且只需要接口声明和方法签名。
  ```java
  // IMyAidlInterface.aidl
  package io.github.kongpf8848.aidlserver;

  // Declare any non-default types here with import statements

  interface IMyAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
  }
  ```

  默认AIDL支持下列数据类型：

  * Java中的基本数据类型，包括 byte，short，int，long，float，double，boolean，char

  * String类型
  
  * CharSequence类型
  
  * List类型 
    List中的所有元素必须是以上支持的类型之一，或者是一个其他AIDL生成的接口或者Parcelable类型。
	
  * Map类型
    Map中的所有元素必须是以上支持的类型之一，或者是一个其他AIDL生成的接口或者Parcelable类型

   如需自定义数据类型，需要实现android.os.Parcelable 接口，如 
   ```java
   //Book.java
   public class Book implements Parcelable {
    private int bookId;
    private String bookName;

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }


    public Book(int bookId, String bookName) {
        this.bookId = bookId;
        this.bookName = bookName;
    }

    protected Book(Parcel in) {
        bookId = in.readInt();
        bookName = in.readString();
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(bookId);
        dest.writeString(bookName);
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

   }
   ```
   为这个类单独建立一个aidl文件, 并使用parcelable关键字进行定义
   ```java
   //Book.aidl
   package io.github.kongpf8848.aidlserver;
   parcelable Book;
   ```
   
2. 服务端实现AIDL接口  
   应用程序编译时会生成以.aidl文件命名的 .java 接口文件。生成的接口包含一个名为 Stub 的子类（例如，YourInterface.Stub），该子类是其父接口的抽象实现，并且会声明 .aidl 文件中的所有方法

   实现 .aidl 生成的接口，如下：

   ```kotlin
   package io.github.kongpf8848.aidlserver

   import android.app.Service
   import android.content.Intent
   import android.os.RemoteException
   import java.util.*

   class AidlService : Service() {

    private val list: MutableList<Book> = ArrayList()

    private val mStub: IMyAidlInterface.Stub = object : IMyAidlInterface.Stub() {

        @Throws(RemoteException::class)
        override fun addBook(book: Book) {
            list.add(book)
        }

        @Throws(RemoteException::class)
        override fun getBookList(): List<Book> {
            return list
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return mStub
    }

  }
  ```
3. 客户端绑定服务，调用 IPC方法
  * 在项目的 src/目录中加入 .aidl 文件
  * 声明一个 IBinder 接口实例（基于 AIDL 生成）
  * 实现android.content.ServiceConnection接口
  * 调用Context.bindService()，传入ServiceConnection实现实例
  * 在onServiceConnected()实现中，调用 YourInterfaceName.Stub.asInterface((IBinder)service)，以将返回的参数转换为 YourInterface类型 
  * 调用在接口上定义的方法
  ```kotlin
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