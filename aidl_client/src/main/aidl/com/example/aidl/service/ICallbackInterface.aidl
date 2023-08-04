// ICallbackInterface.aidl
package com.example.aidl.service;

interface ICallbackInterface {
    void server2client(in ParcelFileDescriptor pfd);
}