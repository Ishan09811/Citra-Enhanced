// Copyright 2023 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package io.github.mandarine3ds.mandarine.fragments

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.mandarine3ds.mandarine.R
import io.github.mandarine3ds.mandarine.viewmodel.MessageDialogViewModel

class MessageDialogFragment : DialogFragment() {
    private val messageDialogViewModel: MessageDialogViewModel by activityViewModels()
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val unknownTitle = requireArguments().get(TITLE)
        val title = when (unknownTitle) { 
            is String -> unknownTitle!!
            is Int -> if (unknownTitle == 0) "" else getString(unknownTitle)
            else -> ""
        }

        val unknownDescription = requireArguments().get(DESCRIPTION)
        val description = when (unknownDescription) {
            is String -> unknownDescription!!
            is Int -> if (unknownDescription == 0) "" else getString(unknownDescription)
            else -> ""
        }

        val unknownPositiveButtonTitle = requireArguments().get(POSITIVE_BUTTON_TITLE)
        val positiveButtonTitle = when (unknownPositiveButtonTitle) {
            is String -> unknownPositiveButtonTitle!!
            is Int -> if (unknownPositiveButtonTitle == 0) "" else getString(unknownPositiveButtonTitle)
            else -> ""
        }
        val positiveButton =  if (positiveButtonTitle.isNotEmpty()) {
            positiveButtonTitle
        } else if (messageDialogViewModel.positiveAction != null) {
            getString(android.R.string.ok)
        } else {
            getString(R.string.close)
        }

        val unknownNegativeButtonTitle = requireArguments().get(NEGATIVE_BUTTON_TITLE)
        val negativeButtonTitle = when (unknownNegativeButtonTitle) {
            is String -> unknownNegativeButtonTitle!!
            is Int -> if (unknownNegativeButtonTitle == 0) "" else getString(unknownNegativeButtonTitle)
            else -> ""
        }
        val negativeButton = negativeButtonTitle

        val unknownNeutralButtonTitle = requireArguments().get(NEUTRAL_BUTTON_TITLE)
        val neutralButtonTitle = when (unknownNeutralButtonTitle) {
            is String -> unknownNeutralButtonTitle!!
            is Int -> if (unknownNeutralButtonTitle == 0) "" else getString(unknownNeutralButtonTitle)
            else -> ""
        }
        val neutralButton = neutralButtonTitle
        
        val helpLinkId = requireArguments().getInt(HELP_LINK)

        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())

        dialogBuilder.setPositiveButton(positiveButton) { _, _ ->
            messageDialogViewModel.positiveAction?.invoke()
        }
        
        if (messageDialogViewModel.negativeAction != null || negativeButton.isNotEmpty()) {
            dialogBuilder.setNegativeButton(negativeButton) { _, _ ->
                messageDialogViewModel.negativeAction?.invoke()
            }
        }

        if (messageDialogViewModel.neutralAction != null || neutralButton.isNotEmpty()) {
            dialogBuilder.setNeutralButton(neutralButton) { _, _ ->
                messageDialogViewModel.neutralAction?.invoke()
            }
        }
        
        if (description.isNotEmpty())
            dialogBuilder.setMessage(description)

        if (title.isNotEmpty())
            dialogBuilder.setTitle(title)

        if (helpLinkId != 0) {
            dialogBuilder.setNeutralButton(R.string.learn_more) { _, _ ->
                openLink(getString(helpLinkId))
            }
        }

        return dialogBuilder.show()
    }

    private fun openLink(link: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        startActivity(intent)
    }

    companion object {
        const val TAG = "MessageDialogFragment"

        private const val TITLE = "Title"
        private const val DESCRIPTION = "Description"
        private const val POSITIVE_BUTTON_TITLE = "PositiveButtonTitle"
        private const val NEGATIVE_BUTTON_TITLE = "NegativeButtonTitle"
        private const val NEUTRAL_BUTTON_TITLE = "NeutralButtonTitle"
        private const val HELP_LINK = "Link"

        fun newInstance(
            activity: FragmentActivity? = null,
            title: Any = "",
            description: Any = "",
            helpLinkId: Int = 0,
            positiveButtonTitle: Any = "",
            positiveAction: (() -> Unit)? = null,
            negativeButtonTitle: Any = "",
            negativeAction: (() -> Unit)? = null,
            neutralButtonTitle: Any = "",
            neutralAction: (() -> Unit)? = null
        ): MessageDialogFragment {
            var clearActions = false
            if (activity != null) {
                ViewModelProvider(activity)[MessageDialogViewModel::class.java].apply {
                    clear()
                    this.positiveAction = positiveAction
                    this.negativeAction = negativeAction
                    this.neutralAction = neutralAction
                }
            } else {
                clearActions = true
            }
            val dialog = MessageDialogFragment()
            val bundle = Bundle()
            bundle.putInt(HELP_LINK, helpLinkId)
            when (title) {
                is String -> bundle.putString(TITLE, title)
                is Int -> bundle.putInt(TITLE, title)
                else -> bundle.putString(TITLE, "")
            }

            when (description) {
                is String -> bundle.putString(DESCRIPTION, description)
                is Int -> bundle.putInt(DESCRIPTION, description)
                else -> bundle.putString(DESCRIPTION, "")
            }

            when (positiveButtonTitle) {
                is String -> bundle.putString(POSITIVE_BUTTON_TITLE, positiveButtonTitle)
                is Int -> bundle.putInt(POSITIVE_BUTTON_TITLE, positiveButtonTitle)
                else -> bundle.putString(POSITIVE_BUTTON_TITLE, "")
            }

            when (negativeButtonTitle) {
                is String -> bundle.putString(NEGATIVE_BUTTON_TITLE, negativeButtonTitle)
                is Int -> bundle.putInt(NEGATIVE_BUTTON_TITLE, negativeButtonTitle)
                else -> bundle.putString(NEGATIVE_BUTTON_TITLE, "")
            }

            when (neutralButtonTitle) {
                is String -> bundle.putString(NEUTRAL_BUTTON_TITLE, neutralButtonTitle)
                is Int -> bundle.putInt(NEUTRAL_BUTTON_TITLE, neutralButtonTitle)
                else -> bundle.putString(NEUTRAL_BUTTON_TITLE, "")
            }
            dialog.arguments = bundle
            return dialog
        }
    }
}
