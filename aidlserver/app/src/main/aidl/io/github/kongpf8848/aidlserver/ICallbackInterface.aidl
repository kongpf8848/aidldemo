// ICallbackInterface.aidl
package io.github.kongpf8848.aidlserver;

interface ICallbackInterface {
    void server2client(in ParcelFileDescriptor pfd);
}