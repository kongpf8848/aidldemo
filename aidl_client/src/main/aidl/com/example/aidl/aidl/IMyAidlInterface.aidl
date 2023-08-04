// IMyAidlInterface.aidl
package com.example.aidl.aidl;
import com.example.aidl.aidl.Book;
import com.example.aidl.aidl.ICallbackInterface;

interface IMyAidlInterface {

    void addBook(in Book book);

    List<Book> getBookList();

    void sendImage(in byte[]data);

    void client2server(in ParcelFileDescriptor pfd);

    void registerCallback(ICallbackInterface callback);

    void unregisterCallback(ICallbackInterface callback);
}
