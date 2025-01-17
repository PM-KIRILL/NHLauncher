package com.cr4sh.nhlauncher.dialogs

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDialogFragment
import com.cr4sh.nhlauncher.R
import com.cr4sh.nhlauncher.activities.MainActivity
import com.cr4sh.nhlauncher.pagers.bluetoothPager.BluetoothFragment1
import com.cr4sh.nhlauncher.utils.NHLManager
import com.cr4sh.nhlauncher.utils.NHLPreferences
import com.cr4sh.nhlauncher.utils.ToastUtils.showCustomToast
import com.cr4sh.nhlauncher.utils.VibrationUtils.vibrate

class ScanTimeDialog(private val wpsAttack: BluetoothFragment1) : AppCompatDialogFragment() {
    private val mainActivity: MainActivity? = NHLManager.getInstance().getMainActivity()

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.wps_custom_pin_dialog, container, false)

//        MainUtils mainUtils = new MainUtils((MainActivity) requireActivity());
        val nhlPreferences = NHLPreferences(requireActivity())
        assert(arguments != null)
        val option = requireArguments().getInt("option")
        isCancelable = false
        val bkg = view.findViewById<LinearLayout>(R.id.custom_theme_dialog_background)
        val title = view.findViewById<TextView>(R.id.dialog_title)
        val setupButton = view.findViewById<Button>(R.id.setup_button)
        val cancelButton = view.findViewById<Button>(R.id.cancel_button)
        val customPin = view.findViewById<EditText>(R.id.customPin)
        if (option == 1) {
            cancelButton.text = "Use default (10s)"
            title.text = "Custom Scan Time"
            customPin.hint = "Scan Time"
        }

        // Apply custom themes
        bkg.setBackgroundColor(Color.parseColor(nhlPreferences.color20()))
        title.setTextColor(Color.parseColor(nhlPreferences.color80()))
        customPin.setTextColor(Color.parseColor(nhlPreferences.color80()))
        customPin.background.mutate().setTint(Color.parseColor(nhlPreferences.color50()))
        customPin.setHintTextColor(Color.parseColor(nhlPreferences.color50()))
        setupButton.setBackgroundColor(Color.parseColor(nhlPreferences.color50()))
        setupButton.setTextColor(Color.parseColor(nhlPreferences.color80()))
        cancelButton.setBackgroundColor(Color.parseColor(nhlPreferences.color80()))
        cancelButton.setTextColor(Color.parseColor(nhlPreferences.color50()))
        setupButton.setOnClickListener {
            if (mainActivity != null) {
                vibrate(mainActivity, 10)
            }
            if (customPin.text.toString().isEmpty()) {
                showCustomToast(requireActivity(), "Empty input")
            } else {
                if (option == 1) {
                    val number = customPin.text.toString().toInt()
                    if (number > 0) {
                        wpsAttack.scanTime = customPin.text.toString()
                        dialog?.cancel()
                    } else {
                        showCustomToast(requireActivity(), "Are you dumb?")
                    }
                }
            }
        }
        cancelButton.setOnClickListener {
            if (mainActivity != null) {
                vibrate(mainActivity, 10)
            }
            dialog?.cancel()
            if (option == 1) {
                wpsAttack.scanTime = "15"
            }
        }
        return view
    }
}
