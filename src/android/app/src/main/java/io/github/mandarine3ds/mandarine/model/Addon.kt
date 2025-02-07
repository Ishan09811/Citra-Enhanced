// Copyright 2025 Mandarine Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package io.github.mandarine3ds.mandarine.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
open class Addon(
    val title: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString() ?: "")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Addon> {
        override fun createFromParcel(parcel: Parcel): Addon = Addon(parcel)
        override fun newArray(size: Int): Array<Addon?> = arrayOfNulls(size)
    }
}

@Parcelize
@Serializable
class Mod(
    val title: String = ""
    val path: String = "",
    val filename: String = "",
    val installedPath: String = "",
    val titleId: Int = 0,
    val enabled: Boolean = true
) : Addon()

@Parcelize
@Serializable
class CustomTexture(
    val title: String = ""
    val path: String = "",
    val filename: String = "",
    val installedPath: String = "",
    val titleId: Int = 0,
    val enabled: Boolean = true
) : Addon()
