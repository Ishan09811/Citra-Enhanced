// SPDX-FileCopyrightText: 2023 yuzu Emulator Project
// SPDX-License-Identifier: GPL-2.0-or-later
// Copyright 2025 Mandarine Project

package io.github.mandarine3ds.mandarine.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import io.github.mandarine3ds.mandarine.databinding.CardOutlinedBinding
import io.github.mandarine3ds.mandarine.model.GameAbout
import io.github.mandarine3ds.mandarine.model.SubmenuGameAbout
import io.github.mandarine3ds.mandarine.utils.ViewUtils.marquee
import io.github.mandarine3ds.mandarine.utils.ViewUtils.setVisible
import io.github.mandarine3ds.mandarine.utils.collect
import io.github.mandarine3ds.mandarine.viewholder.AbstractViewHolder

class GameAboutAdapter(
    private val viewLifecycle: LifecycleOwner,
    private var properties: List<GameAbout>
) : AbstractListAdapter<GameAbout, AbstractViewHolder<GameAbout>>(properties) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AbstractViewHolder<GameAbout> {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            PropertyType.Submenu.ordinal -> {
                SubmenuGameAboutViewHolder(
                    CardOutlinedBinding.inflate(
                        inflater,
                        parent,
                        false
                    )
                )
            }
            else -> throw IllegalArgumentException("Unsupported view type: $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return PropertyType.Submenu.ordinal 
    }

    inner class SubmenuGameAboutViewHolder(val binding: CardOutlinedBinding) :
        AbstractViewHolder<GameAbout>(binding) {
        override fun bind(model: GameAbout) {
            val submenuInfo = model as SubmenuGameAbout

            binding.root.setOnClickListener {
                submenuInfo.action.invoke()
            }

            binding.title.setText(submenuInfo.titleId)
            binding.description.setText(submenuInfo.descriptionId)
            binding.icon.setImageDrawable(
                ResourcesCompat.getDrawable(
                    binding.icon.context.resources,
                    submenuInfo.iconId,
                    binding.icon.context.theme
                )
            )

            binding.details.marquee()
            if (submenuInfo.details != null) {
                binding.details.setVisible(true)
                binding.details.text = submenuInfo.details.invoke()
            } else if (submenuInfo.detailsFlow != null) {
                binding.details.setVisible(true)
                submenuInfo.detailsFlow.collect(viewLifecycle) { binding.details.text = it }
            } else {
                binding.details.setVisible(false)
            }
        }
    }

    enum class PropertyType {
        Submenu
    }
}
