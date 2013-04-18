package com.lilli.gulliver

import android.app.DialogFragment
import android.os.Bundle
import android.app.Dialog
import android.app.AlertDialog
import android.content.DialogInterface

class TrashDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState : Bundle?) : Dialog? {
        val activity = getActivity() as? MyActivity
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(R.string.delete_title)
        builder.setPositiveButton(R.string.delete, object :  DialogInterface.OnClickListener {
            override fun onClick(dialog : DialogInterface?, which : Int) {
                activity?.backupDb()
                activity?.deleteDatabase(StatDbHelper.DATABASE_NAME)
            }
        })
        builder.setNegativeButton(R.string.cancel, null)
        return builder.create()
    }
}