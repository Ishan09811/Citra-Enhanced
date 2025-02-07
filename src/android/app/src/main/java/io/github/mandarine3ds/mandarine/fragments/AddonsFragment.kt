// SPDX-FileCopyrightText: 2025 Mandarine Project
// SPDX-License-Identifier: GPL-2.0-or-later

package io.github.mandarine3ds.mandarine.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.coroutines.launch
import io.github.mandarine3ds.mandarine.R
import io.github.mandarine3ds.mandarine.adapters.AddonsAdapter
import io.github.mandarine3ds.mandarine.databinding.FragmentAddonsBinding
import io.github.mandarine3ds.mandarine.viewmodel.AddonViewModel
import io.github.mandarine3ds.mandarine.viewmodel.HomeViewModel
import io.github.mandarine3ds.mandarine.utils.AddonsHelper
import io.github.mandarine3ds.mandarine.utils.ViewUtils.updateMargins
import io.github.mandarine3ds.mandarine.utils.collect
import java.io.File

class AddonsFragment : Fragment() {
    private var _binding: FragmentAddonsBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by activityViewModels()
    private val addonViewModel: AddonViewModel by activityViewModels()

    private val args by navArgs<AddonsFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addonViewModel.onOpenAddons(args.game)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddonsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeViewModel.setNavigationVisibility(visible = false, animated = false)
        homeViewModel.setStatusBarShadeVisibility(false)

        binding.toolbarAddons.setNavigationOnClickListener {
            binding.root.findNavController().popBackStack()
        }

        binding.toolbarAddons.title = "Addons: " + args.game.title

        binding.listAddons.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = AddonsAdapter()
        }

        addonViewModel.addonList.collect(viewLifecycleOwner) {
            (binding.listAddons.adapter as AddonsAdapter).updateList(it)
        }
        
        binding.buttonInstall.setOnClickListener {
            // TODO: implement logic
        }

        setInsets()
    }

    override fun onResume() {
        super.onResume()
        addonViewModel.refreshAddons()
    }

    override fun onDestroy() {
        super.onDestroy()
        addonViewModel.onCloseAddons()
    }

    val installAddon =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { result ->
            if (result == null) {
                return@registerForActivityResult
            }

            //TODO: implement logic
        }

    private fun setInsets() =
        ViewCompat.setOnApplyWindowInsetsListener(
            binding.root
        ) { _: View, windowInsets: WindowInsetsCompat ->
            val barInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutoutInsets = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())

            val leftInsets = barInsets.left + cutoutInsets.left
            val rightInsets = barInsets.right + cutoutInsets.right

            binding.toolbarAddons.updateMargins(left = leftInsets, right = rightInsets)
            binding.listAddons.updateMargins(left = leftInsets, right = rightInsets)
            binding.listAddons.updatePadding(
                bottom = barInsets.bottom +
                    resources.getDimensionPixelSize(R.dimen.spacing_bottom_list_fab)
            )

            val fabSpacing = resources.getDimensionPixelSize(R.dimen.spacing_fab)
            binding.buttonInstall.updateMargins(
                left = leftInsets + fabSpacing,
                right = rightInsets + fabSpacing,
                bottom = barInsets.bottom + fabSpacing
            )

            windowInsets
        }
}
