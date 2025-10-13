// ICallbackInterface.aidl
package com.example.aidl.aidl;

interface ICallbackInterface {
    void server2client(in ParcelFileDescriptor pfd);
}