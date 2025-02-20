// Copyright 2023 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package io.github.mandarine3ds.mandarine.utils

import android.content.Context
import android.net.Uri
import android.content.Intent
import androidx.documentfile.provider.DocumentFile

object FileBrowserHelper {
    fun getSelectedFiles(
        result: Any,
        context: Context,
        extensions: List<String?>
    ): Array<String>? {
        val files: MutableList<DocumentFile?> = ArrayList()

        when (result) {
            is Intent -> {
                val clipData = result.clipData
                if (clipData == null) {
                    result.data?.let { files.add(DocumentFile.fromSingleUri(context, it)) }
                } else {
                    for (i in 0 until clipData.itemCount) {
                        val item = clipData.getItemAt(i)
                        files.add(DocumentFile.fromSingleUri(context, item.uri))
                    }
                }
            }
            is List<*> -> {
                result.filterIsInstance<Uri>().forEach { uri ->
                    files.add(DocumentFile.fromSingleUri(context, uri))
                }
            }
            else -> return null
        }

        if (files.isNotEmpty()) {
            val filePaths = files.mapNotNull { file ->
                file?.name?.let { filename ->
                    val extensionStart = filename.lastIndexOf('.')
                    if (extensionStart > 0) {
                        val fileExtension = filename.substring(extensionStart + 1)
                        if (extensions.contains(fileExtension)) {
                            file.uri.toString()
                        } else null
                    } else null
                }
            }

            return if (filePaths.isEmpty()) null else filePaths.toTypedArray()
        }
        return null
    }
}
