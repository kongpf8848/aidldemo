// IMyAidlInterface.aidl
package io.github.kongpf8848.aidlserver;
import io.github.kongpf8848.aidlserver.Book;

interface IMyAidlInterface {

    //添加Book
    void addBook(in Book book);

    //获取Book列表
    List<Book> getBookList();

    //传输字节数组
    void sendImage(in byte[]data);

    //传输数据
    void sendData(in ParcelFileDescriptor pfd);


}
