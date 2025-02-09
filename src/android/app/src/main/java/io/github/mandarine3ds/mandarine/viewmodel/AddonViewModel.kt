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
import io.github.mandarine3ds.mandarine.utils.AddonsHelper
import io.github.mandarine3ds.mandarine.model.Game
import io.github.mandarine3ds.mandarine.model.Addon
import java.util.concurrent.atomic.AtomicBoolean

class AddonViewModel : ViewModel() {
    private val _addonList = MutableStateFlow(mutableListOf<Addon>())
    val addonList get() = _addonList.asStateFlow()
    private val _dialogState = MutableStateFlow<DialogEvent>(DialogEvent.None)
    val dialogState = _dialogState.asStateFlow()

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

    fun installMod(uri: Uri) { 
        val currentGame = game ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                AddonsHelper.installMod(uri, currentGame)
            }
        }
    }
        
    fun onCloseAddons() {
        if (_addonList.value.isEmpty()) {
            return
        }
        //TODO: implement logic
    }
}
