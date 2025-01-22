package fr.openium.kodex.ext

import android.content.ContentResolver.SCHEME_CONTENT
import android.content.ContentResolver.SCHEME_FILE
import android.content.Context
import android.net.Uri
import fr.openium.kodex.SCHEME_ASSETS
import java.io.File

internal fun Context.asFile(data: Any?): File? =
    when (data) {
        is File -> data

        is Uri -> {
            when (data.scheme) {
                SCHEME_CONTENT -> {
                    // We can't access "content://" file scheme directly, we need to copy content to a temporary file
                    val tmpFile = File.createTempFile("tmp_content", null)
                    try {
                        contentResolver.openInputStream(data)?.use { inputStream ->
                            tmpFile.outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                    } catch (e: Exception) {
                        tmpFile.delete()
                        throw e
                    }
                    tmpFile
                }

                SCHEME_FILE -> {
                    when (data.pathSegments.firstOrNull()) {
                        SCHEME_ASSETS -> {
                            // Remove the "android_asset" part
                            val path = data.pathSegments.drop(1).joinToString("/")

                            // We can't access "file:///android_assets" file scheme directly, we need to copy content to a temporary file
                            val tmpFile = File.createTempFile("tmp_asset", null)
                            try {
                                assets.open(path).use { inputStream ->
                                    tmpFile.outputStream().use { outputStream ->
                                        inputStream.copyTo(outputStream)
                                    }
                                }
                            } catch (e: Exception) {
                                tmpFile.delete()
                                throw e
                            }
                            tmpFile
                        }

                        else -> {
                            data.path?.let(::File)
                        }
                    }
                }

                else -> {
                    File(data.toString())
                }
            }
        }

        is String -> {
            Uri.parse(data)?.let { uri ->
                asFile(uri)
            }
        }

        else -> null
    }