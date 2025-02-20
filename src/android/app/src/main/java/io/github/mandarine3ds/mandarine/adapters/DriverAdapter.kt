// Copyright 2023 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package io.github.mandarine3ds.mandarine.adapters

import android.net.Uri
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import io.github.mandarine3ds.mandarine.R
import io.github.mandarine3ds.mandarine.databinding.CardDriverOptionBinding
import io.github.mandarine3ds.mandarine.utils.GpuDriverMetadata
import io.github.mandarine3ds.mandarine.viewmodel.DriverViewModel
import io.github.mandarine3ds.mandarine.viewholder.AbstractViewHolder
import io.github.mandarine3ds.mandarine.utils.GpuDriverHelper
import io.github.mandarine3ds.mandarine.utils.ViewUtils.marquee
import io.github.mandarine3ds.mandarine.utils.ViewUtils.setVisible
import io.github.mandarine3ds.mandarine.MandarineApplication

class DriverAdapter(private val driverViewModel: DriverViewModel) :
    AbstractDiffAdapter<Pair<Uri, GpuDriverMetadata>, DriverAdapter.DriverViewHolder>(exact = false) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DriverViewHolder {
        val binding = CardDriverOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DriverViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DriverViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun onSelectDriver(position: Int) {
        driverViewModel.setSelectedDriverIndex(position)
        notifyItemChanged(driverViewModel.previouslySelectedDriver)
        notifyItemChanged(driverViewModel.selectedDriver)
    }

    private fun onDeleteDriver(driverData: Pair<Uri, GpuDriverMetadata>, position: Int) {
        if (driverViewModel.selectedDriver > position) {
            driverViewModel.setSelectedDriverIndex(driverViewModel.selectedDriver - 1)
        }
        driverViewModel.driversToDelete.add(driverData.first)
        driverViewModel.removeDriver(driverData)
        notifyItemRemoved(position)
        if (getItemCount() == 0) {
            driverViewModel.setSelectedDriverIndex(0)
        }
        notifyItemChanged(driverViewModel.selectedDriver)
    }

    inner class DriverViewHolder(val binding: CardDriverOptionBinding) :
        AbstractViewHolder<Pair<Uri, GpuDriverMetadata>>(binding) {

        override fun bind(driverData: Pair<Uri, GpuDriverMetadata>) {
            val driver = driverData.second

            binding.apply {
                radioButton.isChecked = driverViewModel.selectedDriver == bindingAdapterPosition
                root.setOnClickListener {
                    onSelectDriver(bindingAdapterPosition)
                }          
                buttonDelete.setOnClickListener {
                    onDeleteDriver(driverData, bindingAdapterPosition)
                }

                title.marquee()
                title.text = driver.name
                version.text = driver.version
                description.text = driver.description
                version.setVisible(true)
                description.setVisible(true)
                buttonDelete.setVisible(driver.name != MandarineApplication.appContext.getString(R.string.system_gpu_driver))
            }
        }
    }
}
