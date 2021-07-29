// IMyAidlInterface.aidl
package io.github.kongpf8848.aidlserver;
import io.github.kongpf8848.aidlserver.Book;
import io.github.kongpf8848.aidlserver.ICallbackInterface;

interface IMyAidlInterface {

    void addBook(in Book book);

    List<Book> getBookList();

    void sendImage(in byte[]data);

    void client2server(in ParcelFileDescriptor pfd);

    void registerCallback(ICallbackInterface callback);

    void unregisterCallback(ICallbackInterface callback);
}
