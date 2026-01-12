package com.mespl.printingutility

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

@SuppressLint("StaticFieldLeak")
object ToastController {

    private var currentToast: Toast? = null
    private var toastView: View? = null
    private var loader: ProgressBar? = null
    private var icon: ImageView? = null
    private var text: TextView? = null

    /**
     * Call this every time you want to show/update the toast.
     * @param context Context
     * @param message Toast message
     * @param completed true -> show success icon, false -> show loader
     */
    fun showToast(context: Context, message: String, completed: Boolean) {
        Handler(Looper.getMainLooper()).post {
            // initialize toast view if first time
            if (currentToast == null) {
                val inflater = LayoutInflater.from(context)
                toastView = inflater.inflate(R.layout.custom_toast, null)
                loader = toastView?.findViewById(R.id.toast_loader)
                icon = toastView?.findViewById(R.id.toast_icon)
                text = toastView?.findViewById(R.id.toast_text)

                currentToast = Toast(context).apply {
                    duration = Toast.LENGTH_LONG
                    view = toastView
                    setGravity(android.view.Gravity.CENTER, 0, 0)
                }
            }

            // update message
            text?.text = message

            // update loader / icon based on completed flag
            if (completed) {
                loader?.visibility = View.GONE
                icon?.visibility = View.VISIBLE
            } else {
                loader?.visibility = View.VISIBLE
                icon?.visibility = View.GONE
            }

            currentToast?.show()
        }
    }

    /** Optional: cancel the toast */
    fun cancel() {
        currentToast?.cancel()
        currentToast = null
        toastView = null
        loader = null
        icon = null
        text = null
    }
}
