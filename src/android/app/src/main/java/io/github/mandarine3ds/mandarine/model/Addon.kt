// Copyright 2025 Mandarine Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package io.github.mandarine3ds.mandarine.model

import kotlinx.serialization.Serializable

interface Addon {
    var title: String
}

@Serializable
data class Mod(
    override var title: String = "",
    val path: String = "",
    val filename: String = "",
    val installedPath: String = "",
    val titleId: Int = 0,
    val enabled: Boolean = true
) : Addon

@Serializable
data class CustomTexture(
    override var title: String = "",
    val path: String = "",
    val filename: String = "",
    val installedPath: String = "",
    val titleId: Int = 0,
    val enabled: Boolean = true
) : Addon
