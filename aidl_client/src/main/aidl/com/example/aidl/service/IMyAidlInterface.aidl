// IMyAidlInterface.aidl
package com.example.aidl.service;
import com.example.aidl.service.Book;
import com.example.aidl.service.ICallbackInterface;

interface IMyAidlInterface {

    void addBook(in Book book);

    List<Book> getBookList();

    void sendImage(in byte[]data);

    void client2server(in ParcelFileDescriptor pfd);

    void registerCallback(ICallbackInterface callback);

    void unregisterCallback(ICallbackInterface callback);
}
