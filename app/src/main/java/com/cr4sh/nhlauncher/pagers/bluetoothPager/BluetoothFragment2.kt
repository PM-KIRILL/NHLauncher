package com.cr4sh.nhlauncher.pagers.bluetoothPager

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.CompoundButtonCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cr4sh.nhlauncher.R
import com.cr4sh.nhlauncher.bridge.Bridge.Companion.createExecuteIntent
import com.cr4sh.nhlauncher.pagers.bluetoothPager.BluetoothFragment1.Companion.selectedIface
import com.cr4sh.nhlauncher.pagers.bluetoothPager.BluetoothFragment1.Companion.selectedTarget
import com.cr4sh.nhlauncher.utils.DialogUtils
import com.cr4sh.nhlauncher.utils.NHLPreferences
import com.cr4sh.nhlauncher.utils.ToastUtils.showCustomToast
import com.cr4sh.nhlauncher.utils.VibrationUtils.vibrate
import kotlinx.coroutines.launch

class BluetoothFragment2 : Fragment() {
    var nhlPreferences: NHLPreferences? = null
    private var dialogUtils: DialogUtils? = null
    private var flood: String = ""
    private var reverse: String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bt_layout2, container, false)
        nhlPreferences = NHLPreferences(requireActivity())
        dialogUtils = DialogUtils(requireActivity().supportFragmentManager)
        val l2pingContainer = view.findViewById<LinearLayout>(R.id.l2pingContainer)
        val redfangContainer = view.findViewById<LinearLayout>(R.id.redfangContainer)
        val bluerangerContainer = view.findViewById<LinearLayout>(R.id.bluerangerContainer)
        val sdpContainer = view.findViewById<LinearLayout>(R.id.sdpContainer)
        val rfcommContainer = view.findViewById<LinearLayout>(R.id.rfcommContainer)
        val l2pingText = view.findViewById<TextView>(R.id.l2pingText)
        val redfangText = view.findViewById<TextView>(R.id.redfangText)
        val bluerangerText = view.findViewById<TextView>(R.id.bluerangerText)
        val sdpText = view.findViewById<TextView>(R.id.sdpText)
        val rfcommText = view.findViewById<TextView>(R.id.rfcommText)
        l2pingText.setTextColor(Color.parseColor(nhlPreferences!!.color80()))
        redfangText.setTextColor(Color.parseColor(nhlPreferences!!.color80()))
        bluerangerText.setTextColor(Color.parseColor(nhlPreferences!!.color80()))
        sdpText.setTextColor(Color.parseColor(nhlPreferences!!.color80()))
        rfcommText.setTextColor(Color.parseColor(nhlPreferences!!.color80()))
        setContainerBackground(l2pingContainer, nhlPreferences!!.color50())
        setContainerBackground(redfangContainer, nhlPreferences!!.color50())
        setContainerBackground(bluerangerContainer, nhlPreferences!!.color50())
        setContainerBackground(sdpContainer, nhlPreferences!!.color50())
        setContainerBackground(rfcommContainer, nhlPreferences!!.color50())
        val l2pingInfo = view.findViewById<TextView>(R.id.l2pingInfo)
        val redfangInfo = view.findViewById<TextView>(R.id.redfangInfo)
        val bluerangerInfo = view.findViewById<TextView>(R.id.bluerangerInfo)
        val sdpInfo = view.findViewById<TextView>(R.id.sdpInfo)
        //        TextView rfcommInfo = view.findViewById(R.id.rfcomm)
        l2pingInfo.setTextColor(Color.parseColor(nhlPreferences!!.color80()))
        redfangInfo.setTextColor(Color.parseColor(nhlPreferences!!.color80()))
        bluerangerInfo.setTextColor(Color.parseColor(nhlPreferences!!.color80()))
        sdpInfo.setTextColor(Color.parseColor(nhlPreferences!!.color80()))
        //        rfcommInfo.setTextColor(Color.parseColor(myPreferences.color50()));

        // Find Buttons and set background and text colors
        val l2pingButton = view.findViewById<Button>(R.id.startPingButton)
        val redfangButton = view.findViewById<Button>(R.id.startRedfangButton)
        val bluerangerButton = view.findViewById<Button>(R.id.startBlueranger)
        val sdpButton = view.findViewById<Button>(R.id.startSdp)
        val rfcommButton = view.findViewById<Button>(R.id.startRfcomm)
        setButtonColors(l2pingButton)
        setButtonColors(redfangButton)
        setButtonColors(bluerangerButton)
        setButtonColors(sdpButton)
        setButtonColors(rfcommButton)
        val sizeText = view.findViewById<TextView>(R.id.l2ping_size)
        val sizeEdit = view.findViewById<EditText>(R.id.l2ping_size_edit)
        val countText = view.findViewById<TextView>(R.id.l2ping_count)
        val countEdit = view.findViewById<TextView>(R.id.l2ping_count_edit)
        sizeText.setTextColor(Color.parseColor(nhlPreferences!!.color80()))
        sizeEdit.setTextColor(Color.parseColor(nhlPreferences!!.color80()))
        countText.setTextColor(Color.parseColor(nhlPreferences!!.color80()))
        countEdit.setTextColor(Color.parseColor(nhlPreferences!!.color80()))
        //
        sizeEdit.setHintTextColor(Color.parseColor(nhlPreferences!!.color50()))
        countEdit.setHintTextColor(Color.parseColor(nhlPreferences!!.color50()))
        sizeEdit.background.mutate().setTint(Color.parseColor(nhlPreferences!!.color50()))
        countEdit.background.mutate().setTint(Color.parseColor(nhlPreferences!!.color50()))
        val floodPingCheckbox = view.findViewById<CheckBox>(R.id.flood)
        val reversePingCheckBox = view.findViewById<CheckBox>(R.id.reversePing)
        floodPingCheckbox.setTextColor(Color.parseColor(nhlPreferences!!.color80()))
        reversePingCheckBox.setTextColor(Color.parseColor(nhlPreferences!!.color80()))
        val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val colors = intArrayOf(
            Color.parseColor(nhlPreferences!!.color80()), Color.parseColor(
                nhlPreferences!!.color80()
            )
        )
        CompoundButtonCompat.setButtonTintList(floodPingCheckbox, ColorStateList(states, colors))
        CompoundButtonCompat.setButtonTintList(reversePingCheckBox, ColorStateList(states, colors))
        val rangeText = view.findViewById<TextView>(R.id.redfang_range)
        val rangeEdit = view.findViewById<EditText>(R.id.redfang_range_edit)
        rangeText.setTextColor(Color.parseColor(nhlPreferences!!.color80()))
        rangeEdit.setTextColor(Color.parseColor(nhlPreferences!!.color80()))
        rangeEdit.setHintTextColor(Color.parseColor(nhlPreferences!!.color50()))
        rangeEdit.background.mutate().setTint(Color.parseColor(nhlPreferences!!.color50()))
        floodPingCheckbox.setOnClickListener {
            flood = if (floodPingCheckbox.isChecked) "-f" else ""
        }
        reversePingCheckBox.setOnClickListener {
            reverse = if (reversePingCheckBox.isChecked) "-r" else ""
        }
        l2pingButton.setOnClickListener {
            vibrate(requireActivity(), 10)

            if (sizeEdit.text.isEmpty() or countEdit.text.isEmpty()) {
                requireActivity().lifecycleScope.launch {
                    showCustomToast(
                        requireActivity(),
                        "Are you dumb?"
                    )
                }
            } else {
                if (checkForSelectedTarget()) {
                    val l2pingSize = sizeEdit.text.toString()
                    val l2pingCount = countEdit.text.toString()
                    runCmd("echo -ne \"\\033]0;Pinging BT device\\007\" && clear;l2ping -i $selectedIface -s $l2pingSize -c $l2pingCount $flood $reverse $selectedTarget")
                }
            }
        }

        redfangButton.setOnClickListener {
            vibrate(requireActivity(), 10)

            if (rangeEdit.text.isEmpty()) {
                requireActivity().lifecycleScope.launch {
                    showCustomToast(
                        requireActivity(),
                        "Are you dumb?"
                    )
                }
            } else {
                if (checkForSelectedInterface()) {
                    val redfangRange = rangeEdit.text.toString()
                    val redfangLogPath = "/root/redfang.log"
                    runCmd("echo -ne \"\\033]0;RedFang\\007\" && clear;fang -r $redfangRange -o $redfangLogPath")
                }
            }
        }

        bluerangerButton.setOnClickListener {
            if (checkForSelectedTarget()) {
                runCmd("echo -ne \"\\033]0;Blueranger\\007\" && clear;blueranger $selectedIface $selectedTarget")
            }
        }

        sdpButton.setOnClickListener {
            if (checkForSelectedTarget()) {
                requireActivity().lifecycleScope.launch {
                    dialogUtils!!.openSdpToolDialog()
                }
            }
        }

        rfcommButton.setOnClickListener {
            if (checkForSelectedTarget()) {
                runCmd("echo -ne \"\\033]0;RFComm scan\\007\" && clear;rfcomm_scan $selectedTarget")
            }
        }

        return view
    }

    private fun runCmd(cmd: String?) {
        @SuppressLint("SdCardPath") val intent =
            createExecuteIntent("/data/data/com.offsec.nhterm/files/usr/bin/kali", cmd!!)
        requireActivity().startActivity(intent)
    }

    private fun setContainerBackground(container: LinearLayout, color: String?) {
        val drawable = GradientDrawable()
        drawable.cornerRadius = 60f
        drawable.setStroke(8, Color.parseColor(color))
        container.background = drawable
    }

    private fun checkForSelectedTarget(): Boolean {
        return when {
            selectedTarget == null && selectedIface == "None" -> {
                showToast("No interface & target address selected!")
                false
            }

            selectedTarget == null && selectedIface != "None" -> {
                showToast("No target address selected!")
                false
            }

            selectedTarget != null && selectedIface == "None" -> {
                showToast("No interface selected!")
                false
            }

            else -> true
        }
    }

    private fun showToast(message: String) {
        requireActivity().lifecycleScope.launch {
            showCustomToast(requireActivity(), message)
        }
    }


    private fun checkForSelectedInterface(): Boolean {
        // Check for interface only
        return if (selectedIface == "None") {
            requireActivity().lifecycleScope.launch {
                showCustomToast(
                    requireActivity(),
                    "No selected interface!"
                )
            }
            false
        } else {
            true
        }
    }

    private fun setButtonColors(button: Button) {
        button.setBackgroundColor(Color.parseColor(nhlPreferences!!.color50()))
        button.setTextColor(Color.parseColor(nhlPreferences!!.color80()))
    }
}
