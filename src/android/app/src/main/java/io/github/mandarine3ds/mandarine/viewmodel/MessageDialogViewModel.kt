// Copyright 2025 Mandarine Project
// SPDX-License-Identifier: GPL-2.0-or-later

package io.github.mandarine3ds.mandarine.viewmodel

import androidx.lifecycle.ViewModel

class MessageDialogViewModel : ViewModel() {
    var positiveAction: (() -> Unit)? = null
    var negativeAction: (() -> Unit)? = null
    var neutralAction: (() -> Unit)? = null

    fun clear() {
        positiveAction = null
        negativeAction = null
        neutralAction = null
    }
}
