package io.github.kongpf8848.aidlclient

import android.content.Context
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

object AssetUtils {

    fun openAssets(context: Context, fileName: String): ByteArray? {
        try {
            val inputStream = context.assets.open(fileName)
            return openInputStream(context, inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun openInputStream(context: Context?, inputStream: InputStream): ByteArray? {
        try {
            val baos = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var len = -1
            while (inputStream.read(buffer).also { len = it } >= 0) {
                baos.write(buffer, 0, len)
            }
            baos.flush()
            baos.close()
            inputStream.close()
            return baos.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}