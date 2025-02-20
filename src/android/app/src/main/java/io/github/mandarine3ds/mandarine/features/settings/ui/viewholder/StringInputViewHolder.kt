// Copyright 2023 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package io.github.mandarine3ds.mandarine.features.settings.ui.viewholder

import android.view.View
import io.github.mandarine3ds.mandarine.databinding.ListItemSettingBinding
import io.github.mandarine3ds.mandarine.features.settings.model.view.SettingsItem
import io.github.mandarine3ds.mandarine.features.settings.model.view.StringInputSetting
import io.github.mandarine3ds.mandarine.features.settings.ui.SettingsAdapter
import io.github.mandarine3ds.mandarine.utils.ViewUtils.setVisible

class StringInputViewHolder(val binding: ListItemSettingBinding, adapter: SettingsAdapter) :
    SettingViewHolder(binding.root, adapter) {
    private lateinit var setting: SettingsItem

    override fun bind(item: SettingsItem) {
        setting = item
        binding.textSettingName.setText(item.nameId)
        if (item.descriptionId != 0) {
            binding.textSettingDescription.setVisible(true)
            binding.textSettingDescription.setText(item.descriptionId)
        } else {
            binding.textSettingDescription.setVisible(false)
        }
        binding.textSettingValue.setVisible(true)
        binding.textSettingValue.text = setting.setting?.valueAsString

        binding.buttonClear.isEnabled = setting.isEditable
        binding.buttonClear.setVisible(adapter.isClearable(setting))
        binding.buttonClear.setOnClickListener {
            adapter.onClearClick(setting)
        }
    }

    override fun onClick(clicked: View) {
        if (!setting.isEditable) {
            adapter.onClickDisabledSetting()
            return
        }
        adapter.onStringInputClick((setting as StringInputSetting), bindingAdapterPosition)
    }

    override fun onLongClick(clicked: View): Boolean {
        if (setting.isEditable) {
            return adapter.onLongClick(setting.setting!!, bindingAdapterPosition)
        } else {
            adapter.onClickDisabledSetting()
        }
        return false
    }
}
