package com.example.uroflowmetryapp.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.vtitan.uroflowmetry.R
import com.vtitan.uroflowmetry.databinding.FragmentDetailSetupCalibrationBinding
import com.vtitan.uroflowmetry.util.Constants
import com.vtitan.uroflowmetry.util.SessionManager
import java.math.RoundingMode
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class DetailSetupCalibrationFragment : Fragment() {

    private lateinit var binding: FragmentDetailSetupCalibrationBinding
    private lateinit var sessionManager: SessionManager
    private var  calibration =0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentDetailSetupCalibrationBinding.inflate(layoutInflater)
        sessionManager= SessionManager(requireContext())
        binding.checkBox1.setOnCheckedChangeListener { buttonView, check ->
            buttonView.isEnabled = false
            binding.ensureThereIsLoad.setTextColor(resources.getColor(R.color.font_green))
            binding.place5gLoad.setTextColor(resources.getColor(R.color.font_black))
            binding.checkBox2Container.visibility = View.VISIBLE
            binding.checkBox2.isEnabled = true
            calibrationBroadCast(Constants.CALIBRATION_NO_LOAD,0)
        }
        binding.checkBox2.setOnCheckedChangeListener { buttonView, check ->
            buttonView.isEnabled = false
            binding.place5gLoad.setTextColor(resources.getColor(R.color.font_green))
            binding.place10gLoad.setTextColor(resources.getColor(R.color.font_black))
            binding.checkBox3Container.visibility = View.VISIBLE
            binding.checkBox3.isEnabled = true
            calibrationBroadCast(Constants.CALIBRATION_5G_LOAD,0)
        }
        binding.checkBox3.setOnCheckedChangeListener { buttonView, check ->
            buttonView.isEnabled = false
            binding.place10gLoad.setTextColor(resources.getColor(R.color.font_green))
            binding.place20gLoad.setTextColor(resources.getColor(R.color.font_black))
            binding.checkBox4Container.visibility = View.VISIBLE
            binding.checkBox4.isEnabled = true
            calibrationBroadCast(Constants.CALIBRATION_10G_LOAD,0)
        }
        binding.checkBox4.setOnCheckedChangeListener { buttonView, check ->
            buttonView.isEnabled = false
            binding.place20gLoad.setTextColor(resources.getColor(R.color.font_green))
            binding.place50gLoad.setTextColor(resources.getColor(R.color.font_black))
            binding.checkBox5Container.visibility = View.VISIBLE
            binding.checkBox5.isEnabled = true
            calibrationBroadCast(Constants.CALIBRATION_20G_LOAD,0)
        }
        binding.checkBox5.setOnCheckedChangeListener { buttonView, check ->
            buttonView.isEnabled = false
            binding.place50gLoad.setTextColor(resources.getColor(R.color.font_green))
            binding.place100gLoad.setTextColor(resources.getColor(R.color.font_black))
            binding.checkBox6Container.visibility = View.VISIBLE
            binding.checkBox6.isEnabled = true
            calibrationBroadCast(Constants.CALIBRATION_50G_LOAD,0)
        }
        binding.checkBox6.setOnCheckedChangeListener { buttonView, check ->
            buttonView.isEnabled = false
            binding.place100gLoad.setTextColor(resources.getColor(R.color.font_green))
            binding.place200gLoad.setTextColor(resources.getColor(R.color.font_black))
            binding.checkBox7Container.visibility = View.VISIBLE
            binding.checkBox7.isEnabled = true
            calibrationBroadCast(Constants.CALIBRATION_100G_LOAD,0)
        }
        binding.checkBox7.setOnCheckedChangeListener { buttonView, check ->
            buttonView.isEnabled = false
            binding.place200gLoad.setTextColor(resources.getColor(R.color.font_green))
            binding.place400gLoad.setTextColor(resources.getColor(R.color.font_black))
            binding.checkBox8Container.visibility = View.VISIBLE
            binding.checkBox8.isEnabled = true
            calibrationBroadCast(Constants.CALIBRATION_200G_LOAD,0)
        }
        binding.checkBox8.setOnCheckedChangeListener { buttonView, check ->
            buttonView.isEnabled = false
            binding.place400gLoad.setTextColor(resources.getColor(R.color.font_green))
            binding.place500gLoad.setTextColor(resources.getColor(R.color.font_black))
            binding.checkBox9Container.visibility = View.VISIBLE
            binding.checkBox9.isEnabled = true
            calibrationBroadCast(Constants.CALIBRATION_400G_LOAD,0)
        }
        binding.checkBox9.setOnCheckedChangeListener { buttonView, check ->
            buttonView.isEnabled = false
            binding.place500gLoad.setTextColor(resources.getColor(R.color.font_green))
            binding.place1000gLoad.setTextColor(resources.getColor(R.color.font_black))
            binding.checkBox10Container.visibility = View.VISIBLE
            binding.checkBox10.isEnabled = true
            calibrationBroadCast(Constants.CALIBRATION_500G_LOAD,0)
        }
        binding.checkBox10.setOnCheckedChangeListener { buttonView, check ->
            buttonView.isEnabled = false
            binding.place1000gLoad.setTextColor(resources.getColor(R.color.font_green))
            binding.stack500gAnd1000gLoads.setTextColor(resources.getColor(R.color.font_black))
            binding.checkBox11Container.visibility = View.VISIBLE
            binding.checkBox11.isEnabled = true
            calibrationBroadCast(Constants.CALIBRATION_1000G_LOAD,0)
        }
        binding.checkBox11.setOnCheckedChangeListener { buttonView, check ->
            buttonView.isEnabled = false
            binding.stack500gAnd1000gLoads.setTextColor(resources.getColor(R.color.font_green))
            binding.confirmButton.setBackgroundColor(resources.getColor(R.color.font_green))
            binding.confirmButton.isEnabled = true
            calibrationBroadCast(Constants.CALIBRATION_1500G_LOAD,0)

        }
        if(sessionManager.getCalibrationDate() > 0){
            binding.txtCalibirationDate.text="Last calibrated on : ${longToTime(sessionManager.getCalibrationDate())}"
        }else{
            binding.txtCalibirationDate.text="Last calibrated on : "
        }
        //binding.confirmButton.setBackgroundColor(resources.getColor(R.color.font_green))
        //binding.confirmButton.isEnabled = true

        binding.confirmButton.setOnClickListener {
          //  println("G5Load"+sessionManager.get5gloadValue())
            if( sessionManager.get5gloadValue() <=0  || sessionManager.get10gloadValue() <= 0
                ||sessionManager.get20gloadValue() <= 0 || sessionManager.get50gloadValue() <= 0
                ||sessionManager.get100gloadValue() <= 0 || sessionManager.get200gloadValue() <= 0
                ||sessionManager.get400gloadValue() <= 0 || sessionManager.get500gloadValue() <= 0
                ||sessionManager.get1000gloadValue() <= 0 || sessionManager.get1500gloadValue() <= 0){
                calibirationReset()
                sessionManager.clearCalibirationDetails()
                showCalibirationFailed(binding.ctMain)

            }else{
                if(getCalibrationFactor() >= 0.75 && getCalibrationFactor() <= 0.85){
                    val df = DecimalFormat("#.####")
                    df.roundingMode = RoundingMode.CEILING
                    calibration  = df.format(getCalibrationFactor()).toDouble()
                    calibrationBroadCast(0,Constants.CALIBRATION_CONFIRM)
                    binding.txtCalibirationDate.text="Last calibrated on : ${longToTime(System.currentTimeMillis())}"
                    sessionManager.saveCalibrationDate(System.currentTimeMillis())
                    Toast.makeText(requireContext(),"Calibration successful.", Toast.LENGTH_SHORT).show()
                    Log.i("TAG", "onCreateView: "+Constants.CALIBRATION_CONFIRM)
                    //calibirationReset()
             }else{
                    sessionManager.clearCalibirationDetails()
                    calibirationReset()
                    showCalibirationFailed(binding.ctMain)
                }

            }

        }
        // Inflate the layout for this fragment
        return binding.root
    }

    fun calibirationReset(){
        binding.checkBox1Container.visibility = View.VISIBLE
        binding.checkBox1.isChecked=false
        binding.checkBox2.isChecked=false
        binding.checkBox3.isChecked=false
        binding.checkBox4.isChecked=false
        binding.checkBox5.isChecked=false
        binding.checkBox6.isChecked=false
        binding.checkBox7.isChecked=false
        binding.checkBox8.isChecked=false
        binding.checkBox9.isChecked=false
        binding.checkBox10.isChecked=false
        binding.checkBox11.isChecked=false
        binding.checkBox1.isEnabled = true
        binding.checkBox2.isEnabled = false
        binding.checkBox3.isEnabled = false
        binding.checkBox4.isEnabled = false
        binding.checkBox5.isEnabled = false
        binding.checkBox6.isEnabled = false
        binding.checkBox7.isEnabled = false
        binding.checkBox8.isEnabled = false
        binding.checkBox9.isEnabled = false
        binding.checkBox10.isEnabled = false
        binding.checkBox11.isEnabled = false
        binding.ensureThereIsLoad.setTextColor(resources.getColor(R.color.font_black))
        binding.place5gLoad.setTextColor(resources.getColor(R.color.background_alpha24_white))
        binding.place10gLoad.setTextColor(resources.getColor(R.color.background_alpha24_white))
        binding.place20gLoad.setTextColor(resources.getColor(R.color.background_alpha24_white))
        binding.place50gLoad.setTextColor(resources.getColor(R.color.background_alpha24_white))
        binding.place100gLoad.setTextColor(resources.getColor(R.color.background_alpha24_white))
        binding.place200gLoad.setTextColor(resources.getColor(R.color.background_alpha24_white))
        binding.place400gLoad.setTextColor(resources.getColor(R.color.background_alpha24_white))
        binding.place500gLoad.setTextColor(resources.getColor(R.color.background_alpha24_white))
        binding.place1000gLoad.setTextColor(resources.getColor(R.color.background_alpha24_white))
        binding.stack500gAnd1000gLoads.setTextColor(resources.getColor(R.color.background_alpha24_white))
        binding.checkBox2Container.visibility = View.GONE
        binding.checkBox3Container.visibility = View.GONE
        binding.checkBox4Container.visibility = View.GONE
        binding.checkBox5Container.visibility = View.GONE
        binding.checkBox6Container.visibility = View.GONE
        binding.checkBox7Container.visibility = View.GONE
        binding.checkBox8Container.visibility = View.GONE
        binding.checkBox9Container.visibility = View.GONE
        binding.checkBox10Container.visibility = View.GONE
        binding.checkBox11Container.visibility = View.GONE
        binding.confirmButton.isEnabled=false
        binding.confirmButton.setBackgroundColor(resources.getColor(R.color.background_alpha24_green))

    }

    fun calibrationBroadCast(calibrationLoad : Int,calibrationStatus:Int) {
        Intent().also { intent ->
            intent.setAction(Constants.ACTION_CALIBRATION)
            intent.putExtra(Constants.CALIBIRATION_LOAD, calibrationLoad)
            intent.putExtra(Constants.CALIBIRATION_FACTOR,calibration)
            Log.i("TAG", "calibrationBroadCast: "+calibrationStatus)
            intent.putExtra(Constants.CALIBIRATION_STATUS,calibrationStatus)
            context?.sendBroadcast(intent)
        }
    }
   fun getCalibrationFactor():Double{
       val calibirationList :MutableList<Float> = mutableListOf()
       calibirationList.add(sessionManager.get5gloadValue())
       calibirationList.add(sessionManager.get10gloadValue())
       calibirationList.add(sessionManager.get20gloadValue())
       calibirationList.add(sessionManager.get50gloadValue())
       calibirationList.add(sessionManager.get100gloadValue())
       calibirationList.add(sessionManager.get200gloadValue())
       calibirationList.add(sessionManager.get400gloadValue())
       calibirationList.add(sessionManager.get500gloadValue())
       calibirationList.add(sessionManager.get1000gloadValue())
       calibirationList.add(sessionManager.get1500gloadValue())
       val avgload = calibirationList.average()
       return avgload
   }

    fun longToTime(num:Long):String{
        val simple: DateFormat = SimpleDateFormat("dd MMM yyyy HH:mm:ss aa")
        val result = Date(num)
        return simple.format(result)
    }
    @SuppressLint("RestrictedApi")
    fun showCalibirationFailed(view: View){
        val snackbar = Snackbar.make(view,"", Snackbar.LENGTH_LONG)
        val customSnackbar = layoutInflater.inflate(R.layout.snackbar_orange_layout,null)
        val snackbarLayout= snackbar.view as Snackbar.SnackbarLayout
        val llDeviceConnection= customSnackbar.findViewById<LinearLayout>(R.id.llDeviceConnection)
        val deviceconnection= customSnackbar.findViewById<TextView>(R.id.device_connection_failed)
        val imgAlert=customSnackbar.findViewById<ImageView>(R.id.imgAlert)
        llDeviceConnection.background = resources.getDrawable(R.drawable.background_snackbar_orange)
        imgAlert.setImageDrawable(resources.getDrawable(R.drawable.ic_warning))
        deviceconnection.text = resources.getString(R.string.msg_calibration_failed)
        snackbarLayout.setPadding(0,0,0,0)
        snackbarLayout.addView(customSnackbar,0)
        snackbar.show()
    }

}