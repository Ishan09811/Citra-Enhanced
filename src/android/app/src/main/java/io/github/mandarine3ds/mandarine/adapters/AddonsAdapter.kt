package io.github.mandarine3ds.mandarine.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.mandarine3ds.mandarine.databinding.ListItemAddonsBinding
import io.github.mandarine3ds.mandarine.model.Mod
import io.github.mandarine3ds.mandarine.model.Addon
import io.github.mandarine3ds.mandarine.utils.AddonsHelper.enable

class AddonsAdapter(
    private var addonList: List<Addon> = listOf()
) : RecyclerView.Adapter<AddonsAdapter.AddonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddonViewHolder {
        val binding = ListItemAddonsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AddonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddonViewHolder, position: Int) {
        val addon = addonList[position]
        holder.bind(addon)
    }

    override fun getItemCount() = addonList.size

    fun updateList(newList: List<Addon>) {
        addonList = newList
        notifyDataSetChanged()
    }

    class AddonViewHolder(private val binding: ListItemAddonsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(addon: Addon) {
            binding.addon = addon
            if (addon is Mod) {
                binding.addonSwitch.isChecked = addon.enabled
                binding.addonSwitch.setOnCheckedChangeListener { _, isChecked ->   
                   addon.enabled = isChecked
                   addon.enable(isChecked) // saves the value
                }
            }
        }
    }
}
