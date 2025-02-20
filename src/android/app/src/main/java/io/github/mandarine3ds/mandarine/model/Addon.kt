// Copyright 2025 Mandarine Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package io.github.mandarine3ds.mandarine.model

import kotlinx.serialization.Serializable

interface Addon {
    var title: String
    var titleId: Long
    var enabled: Boolean
}

@Serializable
data class Mod(
    override var title: String = "",
    val path: String = "",
    val filename: String = "",
    val installedPath: String = "",
    override var titleId: Long = 0L,
    override var enabled: Boolean = true
) : Addon

@Serializable
data class Update(
    override var title: String = "Update",
    var version: String = "",
    override var titleId: Long = 0L,
    override var enabled: Boolean = true
) : Addon

@Serializable
data class DLC(
    override var title: String = "DLC",
    override var titleId: Long = 0L,
    override var enabled: Boolean = true
) : Addon

// TODO: Implement CustomTexture system
/*@Serializable
data class CustomTextures(
    override var title: String = "Custom Textures",
    var customTextureList: List<CustomTexture>
    override var enabled: Boolean = true
) : Addon

@Serializable
data class CustomTexture(
    override var title: String = "",
    override var enabled: Boolean = true
) : Addon*/
