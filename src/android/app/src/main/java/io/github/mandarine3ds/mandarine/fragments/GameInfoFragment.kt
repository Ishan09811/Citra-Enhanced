// SPDX-FileCopyrightText: 2023 yuzu Emulator Project
// SPDX-License-Identifier: GPL-2.0-or-later
// Copyright 2025 Mandarine Project

package io.github.mandarine3ds.mandarine.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import io.github.mandarine3ds.mandarine.R
import io.github.mandarine3ds.mandarine.databinding.FragmentGameInfoBinding
import io.github.mandarine3ds.mandarine.viewmodel.HomeViewModel
import io.github.mandarine3ds.mandarine.utils.ViewUtils.setVisible
import io.github.mandarine3ds.mandarine.utils.ViewUtils.updateMargins

class GameInfoFragment : Fragment() {
    private var _binding: FragmentGameInfoBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by activityViewModels()

    private val args by navArgs<GameInfoFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameInfoBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeViewModel.setNavigationVisibility(visible = false, animated = false)
        homeViewModel.setStatusBarShadeVisibility(false)

        binding.apply {
            toolbarInfo.setNavigationOnClickListener {
                view.findNavController().popBackStack()
            }

            name.setHint(R.string.name)
            nameField.setText(args.game.title)
            nameField.setOnLongClickListener { 
              copyToClipboard(getString(R.string.name), args.game.title) 
              true
            }

            val pathString = Uri.parse(args.game.path).path ?: ""
            path.setHint(R.string.path)
            pathField.setText(pathString)
            pathField.setOnClickListener { copyToClipboard(getString(R.string.path), pathString) }

            titleId.setHint(R.string.title_id)
            titleIdField.setText(String.format("%016X", args.game.titleId))
            titleIdField.setOnClickListener {
                copyToClipboard(getString(R.string.title_id), String.format("%016X", args.game.titleId))
            }

            if (args.game.company.isNotEmpty()) {
                author.setHint(R.string.author)
                authorField.setText(args.game.company)
                authorField.setOnClickListener {
                    copyToClipboard(getString(R.string.author), args.game.company)
                }
            } else {
                author.setVisible(false)
            }

            version.setHint(R.string.version)
            versionField.setText("1.0")
            versionField.setOnClickListener {
                copyToClipboard(getString(R.string.version), "1.0")
            }
        }
        setInsets()
    }

    private fun copyToClipboard(label: String, body: String) {
        val clipBoard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, body)
        clipBoard.setPrimaryClip(clip)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(
                requireContext(),
                R.string.copied_to_clipboard,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setInsets() =
        ViewCompat.setOnApplyWindowInsetsListener(
            binding.root
        ) { _: View, windowInsets: WindowInsetsCompat ->
            val barInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutoutInsets = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())

            val leftInsets = barInsets.left + cutoutInsets.left
            val rightInsets = barInsets.right + cutoutInsets.right

            binding.toolbarInfo.updateMargins(left = leftInsets, right = rightInsets)
            binding.scrollInfo.updateMargins(left = leftInsets, right = rightInsets)

            binding.contentInfo.updatePadding(bottom = barInsets.bottom)

            windowInsets
        }
}
