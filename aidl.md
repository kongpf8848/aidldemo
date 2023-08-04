# AIDL的使用
1. 创建.aidl文件

  必须使用 Java 编程语言构建.aidl文件。每个.aidl文件均须定义单个接口，并且只需要接口声明和方法签名。
  ```java
  // IMyAidlInterface.aidl
  package com.example.aidl.aidl;

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

   如需自定义数据类型，需要实现```android.os.Parcelable 接口```，如：
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
   然后为这个类单独建立一个aidl文件, 并使用parcelable关键字进行定义
   
   ```java
   //Book.aidl
   package com.example.aidl.aidl;
   parcelable Book;
   ```
   在IMyAidlInterface.aidl中使用Book类型
   ```java
   // IMyAidlInterface.aidl
  package com.example.aidl.aidl;
  import com.example.aidl.aidl.Book;

  interface IMyAidlInterface {

       void addBook(in Book book);

       List<Book> getBookList();
  }
   ```
2. 服务端实现AIDL接口

  应用程序编译时会生成以.aidl文件命名的 .java 接口文件。生成的接口包含一个名为 Stub 的子类（例如，IMyAidlInterface.Stub），该子类是其父接口的抽象实现，并且会声明.aidl文件中的所有方法。
  
  实现.aidl生成的接口，如下:

   ```kotlin
   //AidlService.kt
   package com.example.aidl.server

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
3. 客户端绑定服务，调用IPC方法
  * 在项目的src目录中加入.aidl文件
  * 声明一个IMyAidlInterface接口实例（基于AIDL生成）
  * 创建ServiceConnection实例，实现android.content.ServiceConnection接口
  * 调用Context.bindService()绑定服务，传入ServiceConnection实例
  * 在onServiceConnected()实现中，调用IMyAidlInterface.Stub.asInterface(binder)，将返回参数转换为IMyAidlInterface类型
  * 调用在接口上定义的方法
  
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

	     override fun onDestroy() {
		 if(mStub!=null) {
	           unbindService(serviceConnection)
		 }
		 super.onDestroy()
	     }
   }
  ```
