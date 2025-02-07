package io.github.mandarine3ds.mandarine.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.mandarine3ds.mandarine.databinding.ListItemAddonsBinding
import io.github.mandarine3ds.mandarine.model.Mod

class AddonsAdapter(
    private val addonList: List<Any>
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

    class AddonViewHolder(private val binding: ListItemAddonsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(addon: Any) {
            binding.addon = addon
            binding.addonSwitch.isChecked = true // Default state;
            binding.addonSwitch.setOnCheckedChangeListener { _, isChecked ->
                //TODO: handle
            }
        }
    }
}
