package com.lilli.gulliver

import android.app.DialogFragment
import android.os.Bundle
import android.app.Dialog
import android.app.AlertDialog
import android.content.DialogInterface
import android.util.Log

public class DeleteFileDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState : Bundle?) : Dialog? {
        val activity = getActivity() as? MyActivity
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(R.string.delete_files)
        builder.setPositiveButton(R.string.delete, object :  DialogInterface.OnClickListener {
            override fun onClick(dialog : DialogInterface?, which : Int) {
                for (res in array("resource1.bin", "resource2.bin", "resource3.bin")) {
                    val result = activity?.deleteFile(res)
                    Log.d("DeleteFile", "${res} delete? ${result}")
                }
            }
        })
        builder.setNegativeButton(R.string.cancel, null)
        return builder.create()
    }
}