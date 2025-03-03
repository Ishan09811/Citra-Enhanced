// Copyright 2025 Citra Project / Mandarine Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package io.github.mandarine3ds.mandarine.adapters

import android.graphics.Bitmap
import android.net.Uri
import android.os.SystemClock
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.mandarine3ds.mandarine.HomeNavigationDirections
import io.github.mandarine3ds.mandarine.MandarineApplication
import io.github.mandarine3ds.mandarine.R
import io.github.mandarine3ds.mandarine.databinding.CardGameBinding
import io.github.mandarine3ds.mandarine.model.Game
import io.github.mandarine3ds.mandarine.utils.GameIconUtils
import io.github.mandarine3ds.mandarine.utils.ViewUtils.marquee
import io.github.mandarine3ds.mandarine.utils.ViewUtils.setVisible
import androidx.viewbinding.ViewBinding
import io.github.mandarine3ds.mandarine.databinding.CardGameBigBinding
import io.github.mandarine3ds.mandarine.viewmodel.GamesViewModel
import io.github.mandarine3ds.mandarine.model.GameListItem
import io.github.mandarine3ds.mandarine.viewholder.AbstractViewHolder

class GameAdapter(
    private val activity: AppCompatActivity,
    private val filerGamesCallBack: ((Int, Int) -> Unit)? = null
) : ListAdapter<GameListItem, RecyclerView.ViewHolder>(AsyncDifferConfig.Builder(DiffCallback()).build()),
    View.OnClickListener, View.OnLongClickListener {

    private var lastClickTime = 0L

    companion object {
        const val VIEW_TYPE_LIST = 0
        const val VIEW_TYPE_GRID = 1
        const val SEPARATOR = 2
    }

    private var viewType = VIEW_TYPE_LIST

    fun setViewType(type: Int) {
        viewType = type
        notifyDataSetChanged()
    }

    fun getViewType(): Int = viewType

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is GameListItem.GameItem -> viewType
        is GameListItem.Separator -> SEPARATOR
        else -> VIEW_TYPE_LIST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            SEPARATOR -> SeparatorViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.list_item_separator, parent, false)
            )
            else -> GameViewHolder(
                when (viewType) {
                    VIEW_TYPE_LIST -> CardGameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                    VIEW_TYPE_GRID -> CardGameBigBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                    else -> CardGameBigBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                },
                viewType
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is GameListItem.GameItem -> (holder as GameViewHolder).bind(item.game)
            is GameListItem.Separator -> { }
            else -> (holder as GameViewHolder).bind(item.game)
        }
    }

    override fun onClick(view: View) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) return
        lastClickTime = SystemClock.elapsedRealtime()

        val holder = view.tag as GameViewHolder
        if (!gameExists(holder)) return

        val preferences = PreferenceManager.getDefaultSharedPreferences(MandarineApplication.appContext)
        preferences.edit().putLong(holder.game.keyLastPlayedTime, System.currentTimeMillis()).apply()

        val action = HomeNavigationDirections.actionGlobalEmulationActivity(game = holder.game, shouldApplyCustomSettings = false)
        view.findNavController().navigate(action)
    }

    override fun onLongClick(view: View): Boolean {
        val holder = view.tag as GameViewHolder
        if (!gameExists(holder)) return true

        val context = view.context
        if (holder.game.titleId == 0L) {
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.properties)
                .setMessage(R.string.properties_not_loaded)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        } else {
            val action = HomeNavigationDirections.actionGlobalGameAboutFragment(holder.game)
            view.findNavController().navigate(action)
        }
        return true
    }

    private fun gameExists(holder: GameViewHolder): Boolean {
        if (holder.game.isInstalled) return true

        val gameExists = DocumentFile.fromSingleUri(MandarineApplication.appContext, Uri.parse(holder.game.path))
            ?.exists() == true

        if (!gameExists) {
            Toast.makeText(MandarineApplication.appContext, R.string.loader_error_file_not_found, Toast.LENGTH_LONG).show()
            ViewModelProvider(activity)[GamesViewModel::class.java].reloadGames(true)
        }

        return gameExists
    }

    inner class GameViewHolder(
        private val binding: ViewBinding,
        private val viewType: Int
    ) : RecyclerView.ViewHolder(binding.root) {
        lateinit var game: Game

        init {
            binding.root.tag = this
            binding.root.setOnClickListener(this@GameAdapter)
            binding.root.setOnLongClickListener(this@GameAdapter)
        }

        override fun bind(game: Game) {
            this.game = game
            when (viewType) {
                VIEW_TYPE_LIST -> bindListView(binding as CardGameBinding, game)
                VIEW_TYPE_GRID -> bindGridView(binding as CardGameBigBinding, game)
                else -> bindListView(binding as CardGameBinding, game)
            }
        }

        private fun bindListView(binding: CardGameBinding, game: Game) {
            binding.gameTitle.text = game.title

            binding.imageGameScreen.scaleType = ImageView.ScaleType.CENTER_CROP
            GameIconUtils.loadGameIcon(activity, game, binding.imageGameScreen)

            binding.gameTitle.setVisible(game.title.isNotEmpty())
            binding.gameRegion.setVisible(game.company.isNotEmpty())

            val preferences = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
            val isFavorite = game.keyIsFavorite.let {
                preferences.getBoolean(it, false)
            }

            binding.favoriteIcon.setImageResource(R.drawable.ic_star)
            binding.gameTitle.text = game.title
            binding.favoriteIcon.setVisible(isFavorite)
            binding.gameRegion.text = game.regions
            binding.filename.text = game.filename

            val backgroundColorId =
                if (
                    isValidGame(game.filename.substring(game.filename.lastIndexOf(".") + 1).lowercase())
                ) {
                    R.attr.colorSurface
                } else {
                    R.attr.colorErrorContainer
                }
            binding.cardContents.setBackgroundColor(
                MaterialColors.getColor(
                    binding.cardContents,
                    backgroundColorId
                )
            )

            binding.gameTitle.marquee()
            binding.gameRegion.marquee()
            binding.filename.marquee()
        }
    }
    
    inner class SeparatorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private fun bindGridView(binding: CardGameBigBinding, game: Game) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(binding.root.context)
        val isFavorite = game.keyIsFavorite.let {
            preferences.getBoolean(it, false)
        }

        binding.favoriteIcon.setImageResource(R.drawable.ic_star)

        binding.textGameTitle.text = game.title
        binding.textGameTitle.setVisible(game.title.isNotEmpty())
        binding.favoriteIcon.setVisible(isFavorite)
        GameIconUtils.loadGameIcon(activity, game, binding.imageGameScreen)
        binding.textGameTitle.marquee()
    }

    private fun isValidGame(extension: String): Boolean {
        return Game.badExtensions.stream()
            .noneMatch { extension == it.lowercase() }
    }

    private class DiffCallback : DiffUtil.ItemCallback<GameListItem>() {
        override fun areItemsTheSame(oldItem: GameListItem, newItem: GameListItem): Boolean {
            return when {
                oldItem is GameListItem.GameItem && newItem is GameListItem.GameItem ->
                    oldItem.game.titleId == newItem.game.titleId
                oldItem is GameListItem.Separator && newItem is GameListItem.Separator -> true
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: GameListItem, newItem: GameListItem): Boolean {
            return when {
                oldItem is GameListItem.GameItem && newItem is GameListItem.GameItem ->
                    oldItem.game == newItem.game
                oldItem is GameListItem.Separator && newItem is GameListItem.Separator -> true
                else -> false
            }
        }
    }
}
