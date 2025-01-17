package com.cr4sh.nhlauncher.dialogs

import android.annotation.SuppressLint
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDialogFragment
import com.cr4sh.nhlauncher.R
import com.cr4sh.nhlauncher.activities.MainActivity
import com.cr4sh.nhlauncher.database.DBHandler
import com.cr4sh.nhlauncher.database.DBHandler.Companion.deleteTool
import com.cr4sh.nhlauncher.utils.NHLManager
import com.cr4sh.nhlauncher.utils.NHLPreferences
import com.cr4sh.nhlauncher.utils.NHLUtils
import com.cr4sh.nhlauncher.utils.ToastUtils.showCustomToast
import com.cr4sh.nhlauncher.utils.VibrationUtils.vibrate
import java.util.Locale

class DeleteToolDialog : AppCompatDialogFragment() {
    val mainActivity: MainActivity? = NHLManager.getInstance().getMainActivity()

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.delete_tool_dialog, container, false)
        val mainUtils = NHLUtils((requireActivity() as MainActivity))
        val nhlPreferences = NHLPreferences(requireActivity())
        assert(arguments != null)
        val name = requireArguments().getString("name")
        val bkg = view.findViewById<LinearLayout>(R.id.custom_theme_dialog_background)
        val title = view.findViewById<TextView>(R.id.dialog_title)
        val text2 = view.findViewById<TextView>(R.id.text_view2)
        val cancelButton = view.findViewById<Button>(R.id.cancel_button)
        val deleteButton = view.findViewById<Button>(R.id.delete_button)
        bkg.setBackgroundColor(Color.parseColor(nhlPreferences.color20()))
        title.setTextColor(Color.parseColor(nhlPreferences.color80()))
        text2.setTextColor(Color.parseColor(nhlPreferences.color50()))
        cancelButton.setBackgroundColor(Color.parseColor(nhlPreferences.color80()))
        cancelButton.setTextColor(Color.parseColor(nhlPreferences.color50()))
        deleteButton.setBackgroundColor(Color.parseColor(nhlPreferences.color50()))
        deleteButton.setTextColor(Color.parseColor(nhlPreferences.color80()))
        assert(name != null)
        title.text = name!!.uppercase(Locale.getDefault())
        deleteButton.setOnClickListener {
            if (mainActivity != null) {
                vibrate(mainActivity, 10)
            }
            // Some idiot protection
            try {
                val dbHandler: SQLiteOpenHelper = DBHandler(activity)
                val db = dbHandler.readableDatabase
                val cursor = db.query(
                    "TOOLS",
                    arrayOf("SYSTEM", "NAME"),
                    "NAME = ?",
                    arrayOf<String?>(name),
                    null,
                    null,
                    null,
                    null
                )
                var isSystem: String? = null
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    isSystem = cursor.getString(0)
                    cursor.moveToNext()
                }
                assert(isSystem != null)
                if (isSystem == "1") {
                    showCustomToast(
                        requireActivity(),
                        requireActivity().resources.getString(R.string.cant_delete)
                    )
                } else {
                    deleteTool(db, name)
                    showCustomToast(
                        requireActivity(),
                        requireActivity().resources.getString(R.string.deleted)
                    )
                    mainUtils.restartSpinner()
                }

                // Close connection
                cursor.close()
                db.close()
            } catch (e: SQLiteException) {
                // Display error
                showCustomToast(requireActivity(), "E: $e")
                Log.d("SQLITE", e.toString())
            }
            dialog?.cancel()
        }
        cancelButton.setOnClickListener {
            if (mainActivity != null) {
                vibrate(mainActivity, 10)
            }
            dialog?.cancel()
        }
        return view
    }
}
