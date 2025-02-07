// SPDX-FileCopyrightText: 2025 Mandarine Project
// SPDX-License-Identifier: GPL-2.0-or-later

package io.github.mandarine3ds.mandarine.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.mandarine3ds.mandarine.utils.AddonHelper
import java.util.concurrent.atomic.AtomicBoolean

class AddonViewModel : ViewModel() {
    private val _addonList = MutableStateFlow(mutableListOf<Addon>())
    val addonList get() = _addonList.asStateFlow()

    var game: Game? = null

    private val isRefreshing = AtomicBoolean(false)

    fun onOpenAddons(game: Game) {
        this.game = game
        refreshAddons()
    }

    fun refreshAddons() {
        if (isRefreshing.get() || game == null) {
            return
        }
        isRefreshing.set(true)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                /*TODO: val addonList = (AddonHelper.getMods()).toMutableList()
                patchList.sortBy { it.name }
                _patchList.value = patchList*/
                isRefreshing.set(false)
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
