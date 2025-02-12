
// SPDX-License-Identifier: GPL-2.0 or later
// Copyright 2025 Mandarine Project

package io.github.mandarine3ds.mandarine.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.FragmentActivity
import io.github.mandarine3ds.mandarine.R
import io.github.mandarine3ds.mandarine.MandarineApplication
import io.github.mandarine3ds.mandarine.getPublicFilesDir
import io.github.mandarine3ds.mandarine.utils.FileUtil.asDocumentFile
import io.github.mandarine3ds.mandarine.utils.FileUtil.outputStream
import io.github.mandarine3ds.mandarine.utils.FileUtil.inputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import java.io.FilenameFilter
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

interface SaveManagementUtils {

    companion object {
        private val savesFolderRoot = "/sdmc/Nintendo 3DS/00000000000000000000000000000000/00000000000000000000000000000000/title"
        private var exportZipName: String = "export"
        private var exportZipTitleId: String = ""
        
        fun registerDocumentPicker(context : Context, titleId: String? = null) : ActivityResultLauncher<Array<String>> {
            return (context as ComponentActivity).registerForActivityResult(ActivityResultContracts.OpenDocument()) {
                it?.let { uri -> importSave(context, uri) }
            }
        }

        fun registerDocumentPicker(fragmentAct : FragmentActivity, titleId: String? = null) : ActivityResultLauncher<Array<String>> {
            val activity = fragmentAct as AppCompatActivity
            val activityResultRegistry = fragmentAct.activityResultRegistry

            return activityResultRegistry.register("documentPickerKey", ActivityResultContracts.OpenDocument()) {
                it?.let { uri -> importSave(activity, uri, titleId) }
            }
        }

        fun registerStartForResultExportSave(context : Context, titleId: String?) : ActivityResultLauncher<String> {
            context.getPublicFilesDir()?.let { it.deleteRecursively() }
            return (context as ComponentActivity).registerForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
                uri?.let {
                    exportSave(it)
                }
            }
        }

        fun registerStartForResultExportSave(fragmentAct : FragmentActivity) : ActivityResultLauncher<String> {
            val activity = fragmentAct as AppCompatActivity
            val activityResultRegistry = fragmentAct.activityResultRegistry
            activity.getPublicFilesDir()?.let { it.deleteRecursively() }
            return activityResultRegistry.register("saveExportFolderPickerKey", ActivityResultContracts.CreateDocument("application/zip")) { uri ->
                uri?.let {
                    activity.contentResolver.takePersistableUriPermission(
                        it, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    exportSave(it)
                }
            }
        }

        /**
         * Checks if the saves folder exists.
         */
        fun savesFolderRootExists() : Boolean {
            return MandarineApplication.documentsTree.exists(savesFolderRoot)
        }

        /**
         * Checks if the save folder for the given game exists.
         */
        fun saveFolderGameExists(titleId : String?) : Boolean {
            if (titleId == null) return false
            return MandarineApplication.documentsTree.exists(savesFolderRoot + "/${titleId.lowercase().substring(0, 8)}/${titleId.lowercase().substring(8)}/data/00000001/main")
        }

        /**
         * @return The folder containing the save file for the given game.
         */
        fun getSaveFolderGame(titleId : String) : String {
            return savesFolderRoot + "/${titleId.lowercase().substring(0, 8)}/${titleId.lowercase().substring(8)}/data/00000001"
        }

        /**
         * Zips the save file located in the given folder path and creates a new zip file with the given name, and the current date and time.
         * @param saveFolderPath The path to the folder containing the save file to zip.
         * @param outputZipName The initial part of the name of the zip file to create.
         * @return the zip file created.
         */
        private fun zipSave(mainFileUri: Uri, titleId: String, outputZipName: String): File? {
            try {
                val tempFolder = File(MandarineApplication.appContext.getExternalFilesDir(null), "temp")
                tempFolder.mkdirs()
                val titleFolder = File(tempFolder, titleId)
                titleFolder.mkdirs()
                val mainFile = File(titleFolder, "main")
                mainFileUri.inputStream()?.use { inputStream ->
                    FileOutputStream(mainFile).use { outputStream ->
                       inputStream.copyTo(outputStream)
                    }
                }
                val outputZipFile = File(
                    tempFolder,
                    "$outputZipName - ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}.zip"
                )
                outputZipFile.createNewFile()

                ZipOutputStream(BufferedOutputStream(FileOutputStream(outputZipFile))).use { zos ->
                    titleFolder.walkTopDown().forEach { file ->
                        val zipFileName = file.relativeTo(titleFolder.parentFile).path
                        val entry = ZipEntry("$zipFileName${if (file.isDirectory) "/" else ""}")
                        zos.putNextEntry(entry)
                        if (file.isFile) file.inputStream().use { it.copyTo(zos) }
                        zos.closeEntry()
                    }
                }

                return outputZipFile
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        /**
         * Exports the save file located in the given folder path by creating a zip file and sharing it via intent.
         * @param titleId The title ID of the game to export the save file of. If empty, export all save files.
         * @param outputZipName The initial part of the name of the zip file to create.
         */
        fun exportSave(uri: Uri) {
            if (exportZipTitleId == null) return
            CoroutineScope(Dispatchers.IO).launch {
                val saveFolderPath = MandarineApplication.documentsTree.getUri("${savesFolderRoot}/${exportZipTitleId.lowercase().substring(0, 8)}/${exportZipTitleId.lowercase().substring(8)}/data/00000001/main")
                val zipCreated = zipSave(saveFolderPath, exportZipTitleId!!, exportZipName)
                if (zipCreated == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(MandarineApplication.appContext, R.string.error, Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }
                
                try {
                    MandarineApplication.appContext.contentResolver.openOutputStream(uri)?.use { output ->
                        FileInputStream(zipCreated).use { it.copyTo(output) }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(MandarineApplication.appContext, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(MandarineApplication.appContext, R.string.save_exported_successfully, Toast.LENGTH_LONG).show()
                }
            }
        }

        fun exportSave(startForResultExportSave : ActivityResultLauncher<String>, titleId: String?, outputZipName : String) {
            exportZipTitleId = titleId ?: ""
            exportZipName = outputZipName
            startForResultExportSave.launch("$exportZipName - ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
        }

        /**
         * Launches the document picker to import a save file.
         */
        fun importSave(documentPicker : ActivityResultLauncher<Array<String>>) {         
            documentPicker.launch(arrayOf("application/zip"))
        }

        /**
         * Imports the save files contained in the zip file, and replaces any existing ones with the new save file.
         * @param zipUri The Uri of the zip file containing the save file(s) to import.
         */
        private fun importSave(context : Context, zipUri : Uri, titleId: String? = null) {
            val inputZip = zipUri.inputStream()
            // A zip needs to have at least one subfolder named after a TitleId in order to be considered valid.
            var validZip = false
            val savesFolder = savesFolderRoot
            val cacheSaveDir = File("${MandarineApplication.appContext.cacheDir.path}/saves/")
            cacheSaveDir.mkdir()

            if (inputZip == null) {
                Toast.makeText(context, R.string.error, Toast.LENGTH_LONG).show()
                return
            }

            val filterTitleId = FilenameFilter { _, dirName -> dirName.matches(Regex("^[0-9A-Fa-f]{16}$")) }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    FileUtil.unzipToInternalStorage(BufferedInputStream(inputZip), cacheSaveDir)
                    cacheSaveDir.list(filterTitleId)?.forEach { savePath ->
                        if (savePath == titleId ?: savePath) {
                            MandarineApplication.documentsTree.deleteDocument("${savesFolderRoot}/${savePath.lowercase().substring(0, 8)}/${savePath.lowercase().substring(8)}/data/00000001/main")
                            copy(File(cacheSaveDir, "${savePath}/main"), "${savesFolder}/${savePath.lowercase().substring(0, 8)}/${savePath.lowercase().substring(8)}/data/00000001")
                            validZip = true
                        }
                    }

                    withContext(Dispatchers.Main) {
                        if (!validZip) {
                            Toast.makeText(context, R.string.save_file_invalid_zip_structure, Toast.LENGTH_LONG).show()
                            return@withContext
                        }
                        Toast.makeText(context, R.string.save_file_imported_ok, Toast.LENGTH_LONG).show()
                    }
                } catch (e : IOException) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, R.string.error, Toast.LENGTH_LONG).show()
                    }
                } finally {
                    cacheSaveDir.deleteRecursively()
                }
            }
        }

        /**
         * Deletes the save file for a given game.
         */
        /*fun deleteSaveFile(titleId : String?) : Boolean {
            if (titleId == null) return false
            File("$savesFolderRoot/$titleId").deleteRecursively()
            return true
        }*/

        private fun copy(inputFile: File, outputFile: String) {
            var outputUriFile = DocumentFile.fromTreeUri(MandarineApplication.appContext, MandarineApplication.documentsTree.getUri(outputFile)!!)                
            val destinationFile = outputUriFile!!.createFile("application/octet-stream", "main")
            destinationFile!!.outputStream()?.use { os ->
                FileInputStream(inputFile).use { it.copyTo(os) }
            }
        }
    }
}
