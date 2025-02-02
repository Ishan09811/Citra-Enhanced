// SPDX-FileCopyrightText: 2023 yuzu Emulator Project
// SPDX-License-Identifier: GPL-2.0-or-later

package io.github.mandarine3ds.mandarine.fragments

import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.mandarine3ds.mandarine.HomeNavigationDirections
import io.github.mandarine3ds.mandarine.R
import io.github.mandarine3ds.mandarine.MandarineApplication
import io.github.mandarine3ds.mandarine.adapters.GameAboutAdapter
import io.github.mandarine3ds.mandarine.databinding.FragmentGameAboutBinding
import io.github.mandarine3ds.mandarine.features.settings.model.Settings
import io.github.mandarine3ds.mandarine.model.GameAbout
import io.github.mandarine3ds.mandarine.viewmodel.GamesViewModel
import io.github.mandarine3ds.mandarine.viewmodel.HomeViewModel
import io.github.mandarine3ds.mandarine.model.SubmenuGameAbout
import io.github.mandarine3ds.mandarine.utils.DirectoryInitialization
import io.github.mandarine3ds.mandarine.utils.FileUtil
import io.github.mandarine3ds.mandarine.utils.GameIconUtils
import io.github.mandarine3ds.mandarine.utils.GpuDriverHelper
import io.github.mandarine3ds.mandarine.utils.ViewUtils.marquee
import io.github.mandarine3ds.mandarine.utils.ViewUtils.updateMargins
import io.github.mandarine3ds.mandarine.utils.collect
import java.io.BufferedOutputStream
import java.io.File

class GameAboutFragment : Fragment() {
    private var _binding: FragmentGameAboutBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by activityViewModels()
    private val gamesViewModel: GamesViewModel by activityViewModels()

    private val args by navArgs<GameAboutFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameAboutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeViewModel.setNavigationVisibility(visible = false, animated = true)
        homeViewModel.setStatusBarShadeVisibility(true)

        (requireActivity() as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.title = args.game.title

        binding.buttonBack.setOnClickListener {
            view.findNavController().popBackStack()
        }

        val shortcutManager = requireActivity().getSystemService(ShortcutManager::class.java)
        binding.buttonShortcut.isEnabled = shortcutManager.isRequestPinShortcutSupported
        binding.buttonShortcut.setOnClickListener {
            //TODO: create shortcut
        }

        GameIconUtils.loadGameIcon(requireActivity(), args.game, binding.imageGameScreen)

        binding.buttonStart.setOnClickListener {
            //TODO: launch game
        }

        reloadList()
        setInsets()
    }

    override fun onDestroy() {
        super.onDestroy()
        gamesViewModel.reloadGames(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as? AppCompatActivity)?.setSupportActionBar(null)
    }

    private fun reloadList() {
        _binding ?: return

        val properties = mutableListOf<GameAbout>().apply {
            add(
                SubmenuGameAbout(
                    R.string.info,
                    R.string.info_description,
                    R.drawable.ic_info_outline
                ) {
                    //TODO
                }
            )
            add(
                SubmenuGameAbout(
                    R.string.add_ons,
                    R.string.add_ons_description,
                    R.drawable.ic_edit
                ) {
                    //TODO: launch cheats activity
                }
            )
        }
        binding.listProperties.apply {
            layoutManager =
                GridLayoutManager(requireContext(), resources.getInteger(R.integer.game_grid_columns))
            adapter = GameAboutAdapter(viewLifecycleOwner, properties)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun setInsets() =
        ViewCompat.setOnApplyWindowInsetsListener(
            binding.root
        ) { _: View, windowInsets: WindowInsetsCompat ->
            val barInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutoutInsets = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())

            val leftInsets = barInsets.left + cutoutInsets.left
            val rightInsets = barInsets.right + cutoutInsets.right

            val smallLayout = resources.getBoolean(R.bool.small_layout)
            if (smallLayout) {
                binding.listAll.updateMargins(left = leftInsets, right = rightInsets)
            } else {
                if (ViewCompat.getLayoutDirection(binding.root) ==
                    ViewCompat.LAYOUT_DIRECTION_LTR
                ) {
                    binding.listAll.updateMargins(right = rightInsets)
                    //binding.iconLayout!!.updateMargins(top = barInsets.top, left = leftInsets)
                } else {
                    binding.listAll.updateMargins(left = leftInsets)
                    //binding.iconLayout!!.updateMargins(top = barInsets.top, right = rightInsets)
                }
            }

            val fabSpacing = resources.getDimensionPixelSize(R.dimen.spacing_fab)
            binding.buttonStart.updateMargins(
                left = leftInsets + fabSpacing,
                right = rightInsets + fabSpacing,
                bottom = barInsets.bottom + fabSpacing
            )

            binding.layoutAll.updatePadding(
                top = barInsets.top,
                bottom = barInsets.bottom +
                    resources.getDimensionPixelSize(R.dimen.spacing_bottom_list_fab)
            )

            windowInsets
        }
}
