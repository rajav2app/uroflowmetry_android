package com.example.uroflowmetryapp.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.vtitan.uroflowmetry.R


class MsgDialogFragment(private val titleStr: String, private val stmtStr: String) :
    DialogFragment() {
    private var negativeStr = "CANCEL"
    private var positiveStr = "OK"
    private var pl: PositiveActionListener? = null
    private var nl: NegativeActionListener? = null
    private var isActionReq = false
    private var isHTML = false
    private var dialogView: View? = null

    interface PositiveActionListener {
        fun onPositiveAction(dialog: DialogInterface?, id: Int)
    }

    interface NegativeActionListener {
        fun onNegativeAction(dialog: DialogInterface?, id: Int)
    }

    fun setFromHTML(b: Boolean) {
        isHTML = b
    }

    fun setNegativeStr(str: String) {
        negativeStr = str
    }

    fun setPositiveStr(str: String) {
        positiveStr = str
    }

    fun setConfirmActions(
        @Nullable pl: PositiveActionListener?,
        @Nullable nl: NegativeActionListener?
    ) {
        isActionReq = true
        this.pl = pl
        this.nl = nl
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(getActivity())
        // Get the layout inflater
        val inflater: LayoutInflater = requireActivity().getLayoutInflater()
        dialogView = inflater.inflate(R.layout.message_dialog, null)
        val title: TextView = dialogView!!.findViewById(R.id.tv_dlg_title)
        val stmt: TextView = dialogView!!.findViewById(R.id.tv_param1)
        title.text = titleStr
        if (isHTML) {
            stmt.text = Html.fromHtml(stmtStr, Html.FROM_HTML_MODE_COMPACT)
        } else {
            stmt.text = stmtStr
        }
        if (isActionReq) {
            builder.setView(dialogView)
            if (pl != null) {
                builder.setPositiveButton(positiveStr,
                    DialogInterface.OnClickListener { dialog, id ->
                        pl!!.onPositiveAction(
                            dialog,
                            id
                        )
                    })
            }
            if (nl != null) {
                builder.setNegativeButton(negativeStr,
                    DialogInterface.OnClickListener { dialog, id ->
                        nl!!.onNegativeAction(
                            dialog,
                            id
                        )
                    })
            }
        } else {
            builder.setView(dialogView) // Add action buttons
                .setPositiveButton("OK",
                    DialogInterface.OnClickListener { dialog, id -> dialog.dismiss() })
        }
        return builder.create()
    }
}
