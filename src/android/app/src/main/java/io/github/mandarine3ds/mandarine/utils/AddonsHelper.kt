// Copyright 2025 Mandarine / Mandarine Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package io.github.mandarine3ds.mandarine.utils

import android.content.SharedPreferences
import android.net.Uri
import androidx.preference.PreferenceManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import io.github.mandarine3ds.mandarine.MandarineApplication
import io.github.mandarine3ds.mandarine.model.Mod
import io.github.mandarine3ds.mandarine.model.Addon
import io.github.mandarine3ds.mandarine.model.Game
import java.io.IOException

object AddonsHelper {
    const val KEY_MODS = "Mods"

    private lateinit var preferences: SharedPreferences

    private fun getMods(): List<Mod> {
        var mods = mutableListOf<Mod>()
        val context = MandarineApplication.appContext
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val serializedMods = preferences.getStringSet(KEY_MODS, emptySet()) ?: emptySet()
        mods = serializedMods.map { Json.decodeFromString<Mod>(it) }.toMutableList()
        mods.forEach { mod ->
          if (!FileUtil.exists(mod.installedPath)) mods.remove(mod)
        }
        return mods.toList()
    } 

    fun getMod(uri: Uri, installedPath: Uri, game: Game, title: String? = null): Mod {
        val filePath = uri.toString()

        val newMod = Mod(
            title = (title ?: FileUtil.getFilename(uri)).replace("[\\t\\n\\r]+".toRegex(), " "),
            path = filePath,
            filename = if (FileUtil.isNativePath(filePath)) {
                MandarineApplication.documentsTree.getFilename(filePath)
            } else {
                FileUtil.getFilename(Uri.parse(filePath))
            },
            installedPath = installedPath.toString(),
            titleId = game.titleId
        )

        return newMod
    }

    fun addMod(uri: Uri, installedPath: Uri, game: Game, title: String? = null) {
        preferences = PreferenceManager.getDefaultSharedPreferences(MandarineApplication.appContext)
        val serializedMods = preferences.getStringSet(KEY_MODS, emptySet()) ?: emptySet()
        val mods = serializedMods.map { Json.decodeFromString<Mod>(it) }.toMutableList()
        mods.add(getMod(uri, installedPath, game, title))
        val newSerializedMods = mutableSetOf<String>()
        mods.forEach { newSerializedMods.add(Json.encodeToString(it)) }

        preferences.edit()
            .remove(KEY_MODS)
            .putStringSet(KEY_MODS, newSerializedMods)
            .apply()
    }

    fun installMod(uri: Uri, game: Game) {
        if (FileUtil.getExtension(uri) != "zip") return
        val destDirUri = FileUtil.getModsDir().uri!!
        val extractedUri = FileUtil.extractZip(uri, destDirUri) ?: return
        val extractedFolderName = FileUtil.getFilename(extractedUri)
        if (extractedFolderName != null && FileUtil.isDirectory(extractedUri.toString())) {
            if (extractedFolderName == String.format("%016X", game.titleId)) 
                addMod(uri, extractedUri, game)
            else FileUtil.deleteDocument(extractedUri.toString())
        } else {
            FileUtil.deleteDocument(extractedUri.toString())
        }
    }

    fun getAddons(game: Game): List<Addon> {
        val mods = getMods()
        return mods.filter { mod ->  
            mod.titleId.toInt() == game.titleId.toInt()
        }
    }
}
