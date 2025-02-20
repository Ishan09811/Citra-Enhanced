package io.github.mandarine3ds.mandarine.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import io.github.mandarine3ds.mandarine.databinding.ListItemAddonsBinding
import io.github.mandarine3ds.mandarine.model.*
import io.github.mandarine3ds.mandarine.utils.AddonsHelper.enable
import io.github.mandarine3ds.mandarine.utils.AddonsHelper.delete
import io.github.mandarine3ds.mandarine.viewmodel.AddonViewModel
import io.github.mandarine3ds.mandarine.MandarineApplication
import io.github.mandarine3ds.mandarine.viewholder.AbstractViewHolder

class AddonsAdapter(
    private var addonViewModel: AddonViewModel
) : AbstractDiffAdapter<Addon, AddonsAdapter.AddonViewHolder>(exact = false) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddonViewHolder {
        val binding = ListItemAddonsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AddonViewHolder(binding, addonViewModel)
    }

    class AddonViewHolder(
        private val binding: ListItemAddonsBinding,
        private val addonViewModel: AddonViewModel
    ) : AbstractViewHolder<Addon>(binding) {

        override fun bind(addon: Addon) {
            binding.addon = addon
            if (addon is Mod || addon is DLC || addon is Update) {
                binding.addonSwitch.isChecked = addon.enabled
                binding.addonSwitch.setOnCheckedChangeListener { _, isChecked ->
                    addon.enabled = isChecked
                    addon.enable(isChecked) // Saves the value
                }

                binding.buttonDelete.apply {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        if (addon.delete()) {
                            Toast.makeText(
                                MandarineApplication.appContext,
                                "Successfully deleted ${addon.title}",
                                Toast.LENGTH_SHORT
                            ).show()
                            addonViewModel.refreshAddons()
                        }
                    }
                }
            }
        }
    }
}
