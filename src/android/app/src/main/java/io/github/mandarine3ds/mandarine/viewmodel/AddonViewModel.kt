// SPDX-FileCopyrightText: 2025 Mandarine Project
// SPDX-License-Identifier: GPL-2.0-or-later

package io.github.mandarine3ds.mandarine.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import io.github.mandarine3ds.mandarine.utils.AddonsHelper
import io.github.mandarine3ds.mandarine.utils.AddonsHelper.AddonInstallResult
import io.github.mandarine3ds.mandarine.ui.main.MainActivity
import io.github.mandarine3ds.mandarine.utils.FileBrowserHelper
import io.github.mandarine3ds.mandarine.utils.FileUtil
import io.github.mandarine3ds.mandarine.model.Game
import io.github.mandarine3ds.mandarine.model.Addon
import io.github.mandarine3ds.mandarine.MandarineApplication
import java.util.concurrent.atomic.AtomicBoolean

class AddonViewModel : ViewModel() {
    private val _addonList = MutableStateFlow(mutableListOf<Addon>())
    val addonList get() = _addonList.asStateFlow()
    private val _dialogState = MutableStateFlow<DialogEvent>(DialogEvent.None)
    val dialogState = _dialogState.asStateFlow()
    private val _shouldShowProgressDialog = MutableStateFlow(false)
    val shouldShowProgressDialog get() = _shouldShowProgressDialog.asStateFlow()

    var game: Game? = null

    private val isRefreshing = AtomicBoolean(false)

    fun onOpenAddons(game: Game) {
        this.game = game
        refreshAddons()
    }

    fun showErrorDialog(message: String) {
        _dialogState.value = DialogEvent.ShowErrorDialog(message)
    }

    fun dismissErrorDialog() {
        _dialogState.value = DialogEvent.None
    }

    fun refreshAddons() {
        if (isRefreshing.get()) {
            return
        }
        
        val currentGame = game ?: return
        
        isRefreshing.set(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val addons = AddonsHelper.getAddons(currentGame).toMutableList()
                addons.sortBy { it.title }
                _addonList.value = addons
                isRefreshing.set(false)
            }
        }
    }

    fun installAddon(uris: List<Uri>) { 
        val currentGame = game ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val ciaUris = uris.filter { FileUtil.getExtension(it) == "cia" }
                if (ciaUris.isNotEmpty()) {
                    val selectedFiles = FileBrowserHelper.getSelectedFiles(uris, MandarineApplication.appContext, listOf("cia"))
                    MainActivity.InstallCIAFiles(selectedFiles)          
                }
                val zipUris = uris.filter { FileUtil.getExtension(it) == "zip" }
                if (zipUris.isEmpty()) return@withContext
                for (uri in zipUris) {
                    _dialogState.value = DialogEvent.ShowProgressDialog()
                    val result = AddonsHelper.installMod(uri, currentGame)
                    _dialogState.value = DialogEvent.None
                    when (result) {
                        AddonInstallResult.Success -> refreshAddons()
                        AddonInstallResult.UnknownError -> {
                            showErrorDialog("An unknown error occurred while installing ${FileUtil.getFilename(uri)} addon")
                            waitForDialogToClose()
                        }
                        AddonInstallResult.InvalidArchive -> {
                            showErrorDialog("${FileUtil.getFilename(uri)} addon file isn't supported")
                            waitForDialogToClose()
                        }
                        AddonInstallResult.AlreadyInstalled -> {
                            showErrorDialog("${FileUtil.getFilename(uri)} addon file is already installed")
                            waitForDialogToClose()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    suspend fun waitForDialogToClose() {
        while (_dialogState.value != DialogEvent.None) {
            delay(100)
        }
    }
        
    fun onCloseAddons() {
        //do nothing
    }
}
