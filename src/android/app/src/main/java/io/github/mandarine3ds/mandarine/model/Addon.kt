// Copyright 2025 Mandarine Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package io.github.mandarine3ds.mandarine.model

import android.os.Parcelable
import android.content.Intent
import android.net.Uri
import java.util.HashSet
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
class Mod(
    val title: String = "",
    val path: String = "",
    val filename: String
) : Parcelable

@Parcelize
@Serializable
class CustomTexture(
    val title: String = "",
    val path: String = "",
    val filename: String
) : Parcelable
