// Copyright 2023 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package io.github.mandarine3ds.mandarine.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.widget.ImageView
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.FragmentActivity
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.key.Keyer
import coil.memory.MemoryCache
import coil.request.ImageRequest
import coil.request.Options
import coil.transform.RoundedCornersTransformation
import io.github.mandarine3ds.mandarine.R
import io.github.mandarine3ds.mandarine.model.Game
import java.nio.IntBuffer

class GameIconFetcher(
    private val game: Game,
    private val options: Options
) : Fetcher {
    override suspend fun fetch(): FetchResult {
        val bitmapDrawable = getGameIcon(game.icon).toDrawable(options.context.resources)
        bitmapDrawable.setFilterBitmap(false)
        return DrawableResult(
            drawable = bitmapDrawable,
            isSampled = false,
            dataSource = DataSource.DISK
        )
    }

    private fun getGameIcon(vector: IntArray?): Bitmap {
        val bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.RGB_565)
        bitmap.copyPixelsFromBuffer(IntBuffer.wrap(vector))
        return bitmap
    }

    class Factory : Fetcher.Factory<Game> {
        override fun create(data: Game, options: Options, imageLoader: ImageLoader): Fetcher =
            GameIconFetcher(data, options)
    }
}

class GameIconKeyer : Keyer<Game> {
    override fun key(data: Game, options: Options): String = data.path
}

object GameIconUtils {
    fun loadGameIcon(activity: FragmentActivity, game: Game, imageView: ImageView, isRound: Boolean = true) {
        val imageLoader = ImageLoader.Builder(activity)
            .components {
                add(GameIconKeyer())
                add(GameIconFetcher.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(activity)
                    .maxSizePercent(0.25)
                    .build()
            }
            .build()

        val transformations = mutableListOf<coil.transform.Transformation>()

        if (isRound) {
            transformations.add(
                RoundedCornersTransformation(
                    activity.resources.getDimensionPixelSize(R.dimen.spacing_med).toFloat()
                )
            )
        }

        val request = ImageRequest.Builder(activity)
            .data(game)
            .target(imageView)
            .error(R.drawable.no_icon)
            .apply {
                if (transformations.isNotEmpty()) {
                    transformations(transformations)
                }
            }
            .build()
        imageLoader.enqueue(request)
    }

    fun getGameIcon(game: Game, width: Int = 48, height: Int = 48): Bitmap? {
        if (game.icon == null) {
            return null
        }
        return Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565).apply {
            copyPixelsFromBuffer(IntBuffer.wrap(game.icon))
        }
    }
}
