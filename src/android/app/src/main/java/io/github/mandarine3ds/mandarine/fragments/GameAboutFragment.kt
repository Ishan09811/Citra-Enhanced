// SPDX-FileCopyrightText: 2023 yuzu Emulator Project
// SPDX-License-Identifier: GPL-2.0-or-later

package io.github.mandarine3ds.mandarine.fragments

import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.content.res.ColorStateList
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.MenuItem
import android.widget.Toast
import android.graphics.Color
import android.util.Log
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.palette.graphics.Palette
import io.github.mandarine3ds.mandarine.HomeNavigationDirections
import io.github.mandarine3ds.mandarine.R
import io.github.mandarine3ds.mandarine.MandarineApplication
import io.github.mandarine3ds.mandarine.adapters.GameAboutAdapter
import io.github.mandarine3ds.mandarine.databinding.FragmentGameAboutBinding
import io.github.mandarine3ds.mandarine.databinding.DialogShortcutBinding
import io.github.mandarine3ds.mandarine.features.settings.model.Settings
import io.github.mandarine3ds.mandarine.model.Game
import io.github.mandarine3ds.mandarine.model.GameAbout
import io.github.mandarine3ds.mandarine.viewmodel.GamesViewModel
import io.github.mandarine3ds.mandarine.viewmodel.HomeViewModel
import io.github.mandarine3ds.mandarine.model.SubmenuGameAbout
import io.github.mandarine3ds.mandarine.utils.DirectoryInitialization
import io.github.mandarine3ds.mandarine.utils.FileUtil
import io.github.mandarine3ds.mandarine.utils.FileUtil.inputStream
import io.github.mandarine3ds.mandarine.utils.GameIconUtils
import io.github.mandarine3ds.mandarine.utils.GpuDriverHelper
import io.github.mandarine3ds.mandarine.utils.ViewUtils.marquee
import io.github.mandarine3ds.mandarine.utils.ViewUtils.updateMargins
import io.github.mandarine3ds.mandarine.utils.collect
import io.github.mandarine3ds.mandarine.utils.ThemeUtil
import java.io.BufferedOutputStream
import java.io.File
import kotlin.math.abs
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope

class GameAboutFragment : Fragment() {
    private var _binding: FragmentGameAboutBinding? = null
    private val binding get() = _binding!!

    private val dialogShortcutBinding: DialogShortcutBinding by lazy { DialogShortcutBinding.inflate(requireActivity().layoutInflater) }

    private val homeViewModel: HomeViewModel by activityViewModels()
    private val gamesViewModel: GamesViewModel by activityViewModels()

    private val args by navArgs<GameAboutFragmentArgs>()

    private var shouldLightStatusBar: Boolean = false

    private val shortcutManager by lazy { requireActivity().getSystemService(ShortcutManager::class.java) }
    private val openShortcutIconLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        handleShortcutIconResult(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        setHasOptionsMenu(true)
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
        homeViewModel.setStatusBarShadeVisibility(false)

        (requireActivity() as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.title = args.game.title

	shouldLightStatusBar = !ThemeUtil.isNightMode(requireActivity() as AppCompatActivity)

        binding.appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            val collapseThreshold = Math.abs(verticalOffset) == totalScrollRange

            if (collapseThreshold) {
                binding.toolbar.animate().alpha(1f).setDuration(300).start()
                (requireActivity() as? AppCompatActivity)?.getSupportActionBar()?.setDisplayShowTitleEnabled(true)
                homeViewModel.setStatusBarShadeVisibility(false)
		setStatusBarLightTheme(shouldLightStatusBar)
            } else if (verticalOffset == 0) {
                binding.toolbar.animate().alpha(0f).setDuration(300).start()
                (requireActivity() as? AppCompatActivity)?.getSupportActionBar()?.setDisplayShowTitleEnabled(false)
                homeViewModel.setStatusBarShadeVisibility(true)
		setStatusBarLightTheme(!ThemeUtil.isNightMode(requireActivity() as AppCompatActivity))
            } else {
                if (binding.toolbar.alpha < 1f) {
                    binding.toolbar.animate().alpha(1f).setDuration(300).start()
                }
                (requireActivity() as? AppCompatActivity)?.getSupportActionBar()?.setDisplayShowTitleEnabled(true)
                homeViewModel.setStatusBarShadeVisibility(false)
		setStatusBarLightTheme(shouldLightStatusBar)
            }
        })

        val bitmap = GameIconUtils.getGameIcon(args.game)
        if (bitmap != null) {
	    try {
                Palette.from(bitmap).generate { palette ->
		    if (palette == null) Log.e("GameAboutFragment", "palette generation failed!")
                    palette?.let {
                        val defaultColor = MaterialColors.getColor(requireView(), com.google.android.material.R.attr.colorSurface)
                        val dominantColor = palette.getDominantColor(defaultColor)
                        .takeIf { it != defaultColor } ?: palette.getVibrantColor(defaultColor)
                        .takeIf { it != defaultColor } ?: palette.getMutedColor(defaultColor)
 
                        binding.appBarLayout.setBackgroundColor(dominantColor)
                        binding.collapsingToolbarLayout.setContentScrimColor(dominantColor)
			//binding.collapsingToolbarLayout.setExpandedTitleColor(if (isLightColor(dominantColor)) Color.BLACK else Color.WHITE)
                        binding.collapsingToolbarLayout.setCollapsedTitleTextColor(if (isLightColor(dominantColor)) Color.BLACK else Color.WHITE)
			binding.toolbar.setBackgroundColor(dominantColor)
			binding.toolbar.setNavigationIconTint(if (isLightColor(dominantColor)) Color.BLACK else Color.WHITE)
			binding.buttonShortcut.setIconTint(ColorStateList.valueOf(if (isLightColor(dominantColor)) Color.BLACK else Color.WHITE))
			shouldLightStatusBar = if (isLightColor(dominantColor)) true else false
                    }
                }
	    } catch (e: Exception) {
		Log.e("GameAboutFragment", "palette generation failed: ${e.message}")
	    }
        }

        binding.buttonShortcut.isEnabled = shortcutManager.isRequestPinShortcutSupported
        binding.buttonShortcut.setOnClickListener {
            showShortcutDialog(args.game)
        }

        GameIconUtils.loadGameIcon(requireActivity(), args.game, binding.imageGameScreen, false)

        binding.buttonStart.setOnClickListener {
            val action = HomeNavigationDirections.actionGlobalEmulationActivity(args.game)
            view.findNavController().navigate(action)
        }

        reloadList()
        setInsets()
    }

    private fun setStatusBarLightTheme(value: Boolean) {
	WindowCompat.getInsetsController((requireActivity() as AppCompatActivity).window, (requireActivity() as AppCompatActivity).window.decorView).isAppearanceLightStatusBars = value
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                binding.root.findNavController().popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gamesViewModel.reloadGames(true)
    }

    override fun onDestroyView() {
	setStatusBarLightTheme(!ThemeUtil.isNightMode(requireActivity() as AppCompatActivity))
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

    private fun showShortcutDialog(game: Game) {
	(dialogShortcutBinding.root.parent as? ViewGroup)?.removeView(dialogShortcutBinding.root)
        dialogShortcutBinding.shortcutNameInput.setText(game.title)
        GameIconUtils.loadGameIcon(requireActivity(), game, dialogShortcutBinding.shortcutIcon)

        dialogShortcutBinding.shortcutIcon.setOnClickListener {
           openShortcutIconLauncher?.launch("image/*")
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.create_shortcut)
            .setView(dialogShortcutBinding.root)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val shortcutName = dialogShortcutBinding.shortcutNameInput.text.toString()
                if (shortcutName.isEmpty()) {
                    Toast.makeText(requireContext(), R.string.shortcut_name_empty, Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                val iconBitmap = (dialogShortcutBinding.shortcutIcon.drawable as BitmapDrawable).bitmap

                CoroutineScope(Dispatchers.IO).launch {
                    val icon = Icon.createWithBitmap(iconBitmap)
                    val shortcut = ShortcutInfo.Builder(requireContext(), shortcutName)
                    .setShortLabel(shortcutName)
                    .setIcon(icon)
                    .setIntent(game.launchIntent.apply {
                        putExtra("launchedFromShortcut", true)
                    })
                    .build()

                    shortcutManager?.requestPinShortcut(shortcut, null)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun isLightColor(color: Int): Boolean {
        val r = Color.red(color) / 255.0
        val g = Color.green(color) / 255.0
        val b = Color.blue(color) / 255.0

        val rLinear = if (r <= 0.03928) r / 12.92 else Math.pow((r + 0.055) / 1.055, 2.4)
        val gLinear = if (g <= 0.03928) g / 12.92 else Math.pow((g + 0.055) / 1.055, 2.4)
        val bLinear = if (b <= 0.03928) b / 12.92 else Math.pow((b + 0.055) / 1.055, 2.4)

        val luminance = 0.2126 * rLinear + 0.7152 * gLinear + 0.0722 * bLinear
        return luminance > 0.5
    }

    private fun handleShortcutIconResult(uri: Uri?) {
        if (uri != null && uri != Uri.EMPTY) {
            val scaledBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(uri!!.inputStream()), 108, 108, true)
            dialogShortcutBinding.shortcutIcon.setImageBitmap(scaledBitmap)
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

            /*binding.layoutAll.updatePadding(
                top = barInsets.top,
                bottom = barInsets.bottom +
                    resources.getDimensionPixelSize(R.dimen.spacing_bottom_list_fab)
            )*/

            windowInsets
        }
}
