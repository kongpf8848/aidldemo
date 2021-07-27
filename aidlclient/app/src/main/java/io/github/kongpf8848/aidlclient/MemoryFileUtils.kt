package io.github.kongpf8848.aidlclient

import android.os.MemoryFile
import java.io.FileDescriptor
import java.io.IOException

object MemoryFileUtils {

    fun createMemoryFile(name: String?, length: Int): MemoryFile? {
        try {
            return MemoryFile(name, length)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun getFileDescriptor(memoryFile: MemoryFile): FileDescriptor {
        return ReflectUtils.invoke(
            "android.os.MemoryFile",
            memoryFile,
            "getFileDescriptor"
        ) as FileDescriptor
    }
}