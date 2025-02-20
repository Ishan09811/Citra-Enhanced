// Copyright 2025 Mandarine / Mandarine Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package io.github.mandarine3ds.mandarine.utils

import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.preference.PreferenceManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import io.github.mandarine3ds.mandarine.MandarineApplication
import io.github.mandarine3ds.mandarine.model.Mod
import io.github.mandarine3ds.mandarine.model.Addon
import io.github.mandarine3ds.mandarine.model.Update
import io.github.mandarine3ds.mandarine.model.DLC
import io.github.mandarine3ds.mandarine.model.Game
import java.io.IOException

object AddonsHelper {
    const val KEY_MODS = "Mods"

    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(MandarineApplication.appContext)
    }
    
    val _requiredModFiles: Set<String> = HashSet(
        listOf("code.ips", "code.bps", "code.bin", "exheader.bin", "logo.bcma.lz", "banner.bnr", "icon.bin")
    )

    val requiredModFiles: Set<String> get() = _requiredModFiles

    private fun getMods(): List<Mod> {
        var mods = mutableListOf<Mod>()
        val serializedMods = preferences.getStringSet(KEY_MODS, emptySet()) ?: emptySet()
        mods = serializedMods.map { Json.decodeFromString<Mod>(it) }.toMutableList()
        mods.removeAll { !FileUtil.exists(it.installedPath) }
        val removed = mods.removeAll { !FileUtil.exists(it.installedPath) }
        val newSerializedMods = mods.map { Json.encodeToString(it) }.toMutableSet()
        preferences.edit().putStringSet(KEY_MODS, newSerializedMods).apply()
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

    fun installMod(uri: Uri, game: Game): AddonInstallResult {
        if (FileUtil.getExtension(uri) != "zip") return AddonInstallResult.InvalidArchive
        val destDirUri = FileUtil.getModsDir(String.format("%016X", game.titleId)).uri ?: return AddonInstallResult.UnknownError
        val extractingResult = FileUtil.extractMod(uri, destDirUri, String.format("%016X", game.titleId))
        val extractedUri = extractingResult.first as? Uri ?: return extractingResult.second
        addMod(uri, extractedUri, game)         
        return extractingResult.second
    }

    fun getAddons(game: Game = Game(filename = ""), titleId: Long = 0L): List<Addon> {
        val addons = getMods().toMutableList() as MutableList<Addon>
        val titleID = if (game.titleId != 0L) game.titleId else titleId
        
        if (FileUtil.isUpdateExists(String.format("%016X", game.titleId)))
            addons.add(Update(titleId = game.titleId, enabled = preferences.getBoolean("${game.titleId}_Update_Enabled", true) ?: true))            
        if (FileUtil.isDLCExists(String.format("%016X", game.titleId)))
            addons.add(DLC(titleId = game.titleId, enabled = preferences.getBoolean("${game.titleId}_DLC_Enabled", true) ?: true)) 
            
        return addons.filter { addon ->  
            addon.titleId.toInt() == titleID.toInt()
        }.toList()
    }

    fun Addon.enable(value: Boolean) {
        if (this.title == "DLC") {
            preferences.edit().putBoolean("${this.titleId}_DLC_Enabled", value).apply()
            return 
        }

        if (this.title == "Update") {
            preferences.edit().putBoolean("${this.titleId}_Update_Enabled", value).apply()
            return 
        }
            
        if (this is Mod) {
            val serializedMods = preferences.getStringSet(KEY_MODS, emptySet()) ?: emptySet()
            val mods = serializedMods.map { Json.decodeFromString<Mod>(it) }.toMutableList()
            var modUpdated = false
            mods.forEach { mod ->
                if (mod.installedPath == this.installedPath) {
                    mod.enabled = value
                    modUpdated = true
                }
            }

            if (modUpdated) {
                val newSerializedMods = mods.map { Json.encodeToString(it) }.toMutableSet()

                preferences.edit()
                    .remove(KEY_MODS)
                    .putStringSet(KEY_MODS, newSerializedMods)
                    .apply()
            }
        }
    }

    fun Addon.delete(): Boolean {
        if (this is Mod)
            return FileUtil.deleteDir(this.installedPath)
        if (this is Update)
            return FileUtil.deleteUpdate(String.format("%016X", this.titleId))
        if (this is DLC)
            return FileUtil.deleteDLC(String.format("%016X", this.titleId))

        return false
    }

    enum class AddonInstallResult {
        Success,
        UnknownError,
        InvalidArchive,
        MissingMetadata, //TODO(Ishan09811): implement metadata system later
        InvalidMetadata,
        AlreadyInstalled,
    }
}
