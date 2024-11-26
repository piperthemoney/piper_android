package com.piperbloom.proxyvpn.ui.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.piperbloom.proxyvpn.R

class CustomBlurredDialogFragment : DialogFragment() {

    private var playstoreUrl: String? = null
    private var externalUrl: String? = null

    override fun onAttach(context: Context) {
        val newBase = updateBaseContext(context)
        super.onAttach(newBase)
    }

    private fun updateBaseContext(newBase: Context): Context {
        val overrideConfiguration = Configuration(
            newBase.resources.configuration
        ).apply { fontScale = 1.0f } // Prevent font scaling

        return newBase.createConfigurationContext(overrideConfiguration)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isCancelable = false

        arguments?.let {
            playstoreUrl = it.getString("playstoreUrl")
            externalUrl = it.getString("externalUrl")
        }

    }

    @SuppressLint("UseGetLayoutInflater")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())

        val dialogView = LayoutInflater.from(context).inflate(R.layout.update_dialog, null)

        dialog.setContentView(dialogView)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.window?.setDimAmount(0.4f)

        dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialogView.findViewById<Button>(R.id.closeAppBtn).setOnClickListener {
            dialog.dismiss()
            requireActivity().finishAffinity()
        }

        dialogView.findViewById<Button>(R.id.downloadBtn).setOnClickListener {
            playstoreUrl?.let { it1 -> externalUrl?.let { it2 -> openLink(it1, it2) } }
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun openLink(playstoreUrl: String,externalUrl: String) {
        try {
//            val uri = Uri.parse(playstoreUrl)
//            val packageName = uri.getQueryParameter("id")
//
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)

            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(externalUrl))
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(webIntent)

        } catch (e: ActivityNotFoundException) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(externalUrl))
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(webIntent)
        }
    }

}