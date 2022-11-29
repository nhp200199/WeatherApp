package com.example.weatherapp

import android.app.AlertDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity: AppCompatActivity() {
    var dialog: Dialog? = null
    protected fun showDialog(title: String,
                             message: String,
                             onPositiveButton: (() -> Unit)?,
                             onNegativeButton: (() -> Unit)?
    ) {
        hideDialog()

        dialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OKE") { dialog, _ ->
                dialog.dismiss()
                onPositiveButton?.invoke()
            }
            .setNegativeButton("NO") { dialog, _ ->
                dialog.dismiss()
                onNegativeButton?.invoke()
            }
            .setCancelable(false)
            .create()
        dialog!!.show()
    }

    protected fun hideDialog() {
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
            dialog = null
        }
    }
}