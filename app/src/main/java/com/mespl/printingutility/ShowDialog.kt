package com.mespl.printingutility

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class ShowDialog {
    private val context: Context? = null
    private val prefs: SharedPreferences? = null

    companion object {
        private val internetFailureDialog: AlertDialog? = null
        fun showToast(ctx: Context?, Message: String?) {
            Toast.makeText(ctx, Message, Toast.LENGTH_SHORT).show()
        }

        fun showToastSmall(ctx: Context?, Message: String?) {
            Toast.makeText(ctx, Message, Toast.LENGTH_SHORT).show()
        }

        fun showAlertDialog(ctx: Context, msg: String) {
            val dialog = Dialog(ctx)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.custom_dialog)
            val body = dialog.findViewById(R.id.msg) as TextView
            body.text = msg

            val okBtn = dialog.findViewById(R.id.OK) as Button
            okBtn.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }


        fun showAlertDialogFinish(ctx: Context, msg: String?) {
            val dialog = Dialog(ctx)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.custom_dialog)
            val body = dialog.findViewById(R.id.msg) as TextView
            body.text = msg
            val okBtn = dialog.findViewById(R.id.OK) as Button
            okBtn.setOnClickListener {
                dialog.dismiss()
                ctx as Activity
                ctx.finish()
            }
            dialog.show()
        }

        fun showProgress(ctx: Context?, msg: String): ProgressDialog {
            val mProgressDialog = ProgressDialog(ctx)
            mProgressDialog.setIndeterminate(true)
            mProgressDialog.setCancelable(false)
            mProgressDialog.setTitle(msg)
            mProgressDialog.setMessage(ctx?.getString(R.string.please_wait))
            return mProgressDialog
        }

        fun dismissDialog(pDialog: ProgressDialog?) {
            if (pDialog != null && pDialog?.isShowing == true)
                pDialog.dismiss()
        }


    }
}

