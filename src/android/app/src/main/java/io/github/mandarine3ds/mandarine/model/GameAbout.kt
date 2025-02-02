package io.github.mandarine3ds.mandarine.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.coroutines.flow.StateFlow

interface GameAbout {
    @get:StringRes
    val titleId: Int

    @get:StringRes
    val descriptionId: Int

    @get:DrawableRes
    val iconId: Int
}

data class SubmenuGameAbout(
    override val titleId: Int,
    override val descriptionId: Int,
    override val iconId: Int,
    val details: (() -> String)? = null,
    val detailsFlow: StateFlow<String>? = null,
    val action: () -> Unit
) : GameAbout
