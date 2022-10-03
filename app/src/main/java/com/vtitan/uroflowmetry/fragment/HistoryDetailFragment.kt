package com.example.uroflowmetryapp.fragment

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_DOCUMENTS
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.vtitan.uroflowmetry.R
import com.vtitan.uroflowmetry.activity.HomeActivity
import com.vtitan.uroflowmetry.adapter.HistoryRecyclerAdapter
import com.vtitan.uroflowmetry.databinding.FragmentHistoryDetailBinding
import com.vtitan.uroflowmetry.fragment.FlowFragment
import com.vtitan.uroflowmetry.fragment.HomeFragment
import com.vtitan.uroflowmetry.fragment.TestDetailFragment
import com.vtitan.uroflowmetry.model.PatientDetails
import com.vtitan.uroflowmetry.model.UroInfoModel
import com.vtitan.uroflowmetry.model.relations.UroTestWithUroInfo
import com.vtitan.uroflowmetry.viewmodel.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class HistoryDetailFragment : Fragment(){
    private lateinit var binding: FragmentHistoryDetailBinding
    private lateinit var patientViewModel: PatientViewModel
    private lateinit var patientViewModelFactory: PatientViewModelFactory
    private lateinit var uroTestInfoViewModel: UroTestInfoViewModel
    private lateinit var uroTestInfoViewModelFactory: UroTestInfoViewModelFactory
    private lateinit var historyLineChart : LineChart
    private var patTime :Long =0L
    private var patid :String?= null
    private var detail : MutableList<UroTestWithUroInfo> = mutableListOf()
    private var patiendDetails : PatientDetails? =null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHistoryDetailBinding.inflate(layoutInflater)
        val application = requireNotNull(this.activity).application
        patientViewModelFactory = PatientViewModelFactory(application)
        patientViewModel = ViewModelProvider(this,patientViewModelFactory).get(PatientViewModel::class.java)
        uroTestInfoViewModelFactory = UroTestInfoViewModelFactory(application)
        uroTestInfoViewModel = ViewModelProvider(this,uroTestInfoViewModelFactory).get(UroTestInfoViewModel::class.java)
        historyLineChart=binding.historyLineChart
        val dateSearch = ArrayAdapter(requireContext(), R.layout.spinner_item, resources.getStringArray(R.array.search_date_list))
        binding.searchDate.setAdapter(dateSearch)
        binding.searchDate.setText(dateSearch.getItem(0), false)
        binding.searchDate.isEnabled=false
        val patientList=patientViewModel.getPatientIds()
        binding.searchDate.setOnItemClickListener { adapterView, view, i, l ->
            if (binding.patientSearchText.text.toString() in patientList) {
                patid=binding.patientSearchText.text.toString()
                patTime=getDateLong(binding.searchDate.text.toString())

                val test_id = uroTestInfoViewModel.getTestId(patid)
                val test_id_time = uroTestInfoViewModel.getTestIdTime(patid,patTime)
                detail.clear()
                for (i in 0 until test_id_time?.size!!){
                    detail.add(uroTestInfoViewModel.getUroTestWithUroInfo(test_id_time[i]))
                }

                val patientName = patientViewModel.getPatientDetails(patid)
                val adapter = HistoryRecyclerAdapter(detail.toList())
                binding.recyclerView.adapter = adapter
                binding.recyclerView.layoutManager = LinearLayoutManager(this.context,RecyclerView.VERTICAL,false)
                adapter.setListener(object :HistoryRecyclerAdapter.HistoryItemClickListener{
                    override fun onHistoryItemClick(v: View?, uroInfo: UroTestWithUroInfo) {
                        val testDetailfragment = TestDetailFragment()
                        val bundle = Bundle()
                        bundle.putSerializable(getString(R.string.key_test_details),uroInfo)
                        testDetailfragment.arguments = bundle
                        activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, testDetailfragment)?.commit()
                    }

                })
                initializeHistoryChart(detail.toList())
                binding.patientName.setText(patientName.patName)
                //binding.patientTime.setText(patTimeString)
                patiendDetails=patientViewModel.getPatientDetails(patid)
                dateSearch.notifyDataSetChanged()
            }
            else{
                Toast.makeText(requireContext(),"Enter Valid Patient Id",Toast.LENGTH_SHORT).show()
            }
        }

        val patientIdAdapter = ArrayAdapter(requireContext(),
            R.layout.spinner_item, patientList)
        binding.patientSearchText.setAdapter(patientIdAdapter)
        binding.patientSearchContainer.setEndIconOnClickListener {
            if (binding.patientSearchText.text.toString() in patientList) {
                patid=binding.patientSearchText.text.toString()
                patTime=getDateLong(binding.searchDate.text.toString())

                val test_id = uroTestInfoViewModel.getTestId(patid)
                val test_id_time = uroTestInfoViewModel.getTestIdTime(patid,patTime)
                detail.clear()
                for (i in 0 until test_id_time?.size!!){
                    detail.add(uroTestInfoViewModel.getUroTestWithUroInfo(test_id_time[i]))
                }
                val patientName = patientViewModel.getPatientDetails(patid)
                val adapter = HistoryRecyclerAdapter(detail.toList())
                binding.recyclerView.adapter = adapter
                binding.recyclerView.layoutManager = LinearLayoutManager(this.context,RecyclerView.VERTICAL,false)
                adapter.setListener(object :HistoryRecyclerAdapter.HistoryItemClickListener{
                    override fun onHistoryItemClick(v: View?, uroInfo: UroTestWithUroInfo) {
                        val testDetailfragment = TestDetailFragment()
                        val bundle = Bundle()
                        bundle.putSerializable(getString(R.string.key_test_details),uroInfo)
                        testDetailfragment.arguments = bundle
                        activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, testDetailfragment)?.commit()
                    }

                })
                initializeHistoryChart(detail.toList())
                binding.patientName.setText(patientName.patName)
                //binding.patientTime.setText(patTimeString)
                patiendDetails=patientViewModel.getPatientDetails(patid)
            }
            else{
                Toast.makeText(requireContext(),"Enter Valid Patient Id",Toast.LENGTH_SHORT).show()
            }
            hideSoftKeyboard(binding.patientSearchText)
        }

        binding.patientSearchText.setOnItemClickListener { adapterView, view, i, l ->
            hideSoftKeyboard(binding.patientSearchText)
        }

        binding.imgDownload.setOnClickListener(View.OnClickListener {
            if( patTime != null && patid!=null && detail!=null && patiendDetails!=null) {
                showPopup(patTime, patid!!, detail, patiendDetails!!)
            }else{
                Toast.makeText(requireContext(),"Enter valid details",Toast.LENGTH_SHORT).show()
            }
        })


        binding.patientSearchText.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(textView: TextView, i: Int, keyEvent: KeyEvent?): Boolean {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    if (textView.text.toString() in patientList) {
                        patid=binding.patientSearchText.text.toString()
                        patTime=getDateLong(binding.searchDate.text.toString())

                        val test_id = uroTestInfoViewModel.getTestId(patid)
                        val test_id_time = uroTestInfoViewModel.getTestIdTime(patid,patTime)
                        detail.clear()
                        for (i in 0 until test_id_time?.size!!){
                            detail.add(uroTestInfoViewModel.getUroTestWithUroInfo(test_id_time[i]))
                        }
                        val patientName = patientViewModel.getPatientDetails(patid)
                        val adapter = HistoryRecyclerAdapter(detail.toList())
                        binding.recyclerView.adapter = adapter
                        adapter.setListener(object :HistoryRecyclerAdapter.HistoryItemClickListener{
                            override fun onHistoryItemClick(v: View?, uroInfo: UroTestWithUroInfo) {
                                val testDetailfragment = TestDetailFragment()
                                val bundle = Bundle()
                                bundle.putSerializable(getString(R.string.key_test_details),uroInfo)
                                testDetailfragment.arguments = bundle
                                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, testDetailfragment)?.addToBackStack("TestDetails")?.commit()
                            }

                        })
                        binding.recyclerView.layoutManager = LinearLayoutManager(context,RecyclerView.VERTICAL,false)

                        initializeHistoryChart(detail.toList())
                        binding.patientName.setText(patientName.patName)
                        //binding.patientTime.setText(patTimeString)
                        patiendDetails=patientViewModel.getPatientDetails(patid)
                    }
                    else{
                        Toast.makeText(requireContext(),"Enter Valid Patient Id",Toast.LENGTH_SHORT).show()
                    }
                   // etPid.clearFocus()
                    hideSoftKeyboard(textView)
                    return true
                }
                return false
            }
        })

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun initializeHistoryChart(uroInfoValues: List<UroTestWithUroInfo>) {
        historyLineChart.setDrawGridBackground(false)
        historyLineChart.description.isEnabled = false
        historyLineChart.setTouchEnabled(false)
        historyLineChart.isDragEnabled = true
        historyLineChart.setScaleEnabled(true)
        historyLineChart.setPinchZoom(false)
        val xl = historyLineChart.xAxis
        xl.position = XAxis.XAxisPosition.BOTTOM
        xl.setAvoidFirstLastClipping(true)
        //xl.setValueFormatter(valueFormatter);
        xl.setAvoidFirstLastClipping(false)
        xl.setDrawGridLines(false)
        xl.labelCount = 5
        // xl.setAxisMinimum(0f);
        val leftAxis =historyLineChart.axisLeft
        leftAxis.setAxisMinimum(0f);
        // this replaces setStartAtZero(true)
        val rightAxis = historyLineChart.axisRight
        rightAxis.isEnabled = false
        rightAxis.setDrawGridLines(false)
        val l = historyLineChart.legend
        l.formSize=10f
        l.form = Legend.LegendForm.SQUARE
        l.textSize=14f
        l.formToTextSpace=5f
        if (uroInfoValues.size > 0) {
            setChartValues(uroInfoValues)
        } else {
            historyLineChart.data = null
        }
        historyLineChart.invalidate()
    }

    private fun setChartValues(uroInfoValues: List<UroTestWithUroInfo>) {
        val flowratentries=ArrayList<Entry>()
        for (i in 0 .. uroInfoValues.size-1) {
              if(uroInfoValues[i].uroInfoModel.size != 0) {
                  val vxVal = i.toFloat()
                  val vyVal = getFlow(uroInfoValues[i].uroInfoModel).toFloat()
                  Log.i("YVALUE", vyVal.toString())
                  flowratentries.add(Entry(vxVal, vyVal))
              }
        }
        val set1 = LineDataSet(flowratentries, "Max flow rate")
        set1.color = resources.getColor(R.color.black)
        set1.valueTextSize=25f
        set1.setDrawValues(false)
        set1.setDrawCircleHole(true)
        set1.setDrawCircles(true)
        set1.setCircleColor(resources.getColor(R.color.black))
        set1.disableDashedLine()
        set1.mode = LineDataSet.Mode.LINEAR

        // create a data object with the data sets
        val data = LineData(set1)

        historyLineChart.data = data
        //chartVolume.setVisibleXRangeMaximum(1000);
    }
    fun getFlow(temp :List<UroInfoModel>):String{
        val flow :MutableList<Double> = mutableListOf()
        for (i in temp){
            flow.add(i.flow_rate!!)
        }
        val max = flow.maxOrNull()
        return String.format("%.2f", max)
    }


    private fun showPopup(time:Long,patId:String,listItems:List<UroTestWithUroInfo>,patientDetails: PatientDetails){
        val builder : Dialog = Dialog(requireContext())
        builder.setContentView(R.layout.popup_dialog_box)
        val autoStartDate =builder.findViewById<AutoCompleteTextView>(R.id.autoStartDate)
        val autoEndDate=builder.findViewById<AutoCompleteTextView>(R.id.autoEndDate)
        autoStartDate.setText(longToTime(System.currentTimeMillis()))
        autoEndDate.setText(longToTime(time))
        builder.findViewById<Button>(R.id.export).setOnClickListener {
            GlobalScope.async(Dispatchers.IO) {
                ExportUroTestDetails(listItems,patientDetails)
            }
            builder.dismiss()
        }
        builder.findViewById<Button>(R.id.cancel).setOnClickListener {
            builder.dismiss()
        }
        builder.show()
    }

    fun longToTime(num:Long):String{
        val simple: DateFormat = SimpleDateFormat("dd MMM yyyy")
        val result = Date(num)
        return simple.format(result)
    }


    private suspend fun ExportUroTestDetails(data:List<UroTestWithUroInfo>,patientDetails: PatientDetails) {

        ActivityCompat.requestPermissions(requireActivity(), arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE), PackageManager.PERMISSION_GRANTED)

        lateinit var directory_path:String
        if (Build.VERSION_CODES.R > Build.VERSION.SDK_INT) {
            directory_path = Environment.getExternalStorageDirectory().path + "/UroflowmetryReport/Report"
        } else {
            directory_path =
                Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS).absolutePath + "/UroflowmetryReport/Report"
        }
        val direc = File(directory_path)

        var pathOfFile: File? = context?.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        var fileName: String = "UroTest.csv"
        if (direc?.exists()!! && direc?.isDirectory!!){
            for (child in direc.listFiles()) {
                child.delete()
            }
        }
        lateinit var fileWriter: FileWriter
        try {
            direc.mkdirs()
            val file = File(directory_path,fileName)
            file.createNewFile()
            fileWriter =  FileWriter(file,true)
            val header = "test_id,pat_position,test_mode,voiding_time,max_flow_rate,volume,avg_flow_rate,peak_time,start_time,stop_time"
            if(patientDetails!=null){
                val pat_details="PatientID : ${patientDetails.patId},Name : ${patientDetails.patName},Age : ${patientDetails.patAge},Gender : ${patientDetails.patGender}"
                fileWriter.write(pat_details+"\n")
                println(pat_details)
            }
            fileWriter.write(header+"\n")
            for (i in data!!){
                val point = "${i.uroTestModel.test_id},${getPosition(i.uroTestModel.position!!.toInt())},${getMode(i.uroTestModel.test_mode!!.toInt())},${getVoidingTime(i.uroTestModel.wait_time!!.toInt())}," +
                        "${getFlow(i.uroInfoModel)},${getVolume(i.uroInfoModel)},${getAvgFlow(i.uroInfoModel)},${getPeakTime(i.uroInfoModel)},${getTime(i.uroTestModel.startTime!!.toLong())},${getTime(i.uroTestModel.stopTime!!.toLong())}"
                fileWriter.write(point+"\n")
            }
            fileWriter.flush()
            fileWriter.close()
            GlobalScope.launch(Dispatchers.Main) {
                Toast.makeText(context, "Downloaded", Toast.LENGTH_SHORT).show()
            }
        }catch (e : IOException){
            Log.i("EX","${e.printStackTrace()}")
            e.printStackTrace()
        }
    }

    fun getVolume(temp :List<UroInfoModel>):String{
        val vol :MutableList<Double> = mutableListOf()
        for (i in temp){
            vol.add(i.volume_info!!)
        }
        val max = vol.maxOrNull()
        return String.format("%.2f", max)
    }

    fun getPeakTime(temp:List<UroInfoModel>):Int{
        var peakT:Int?=null
        val flow :MutableList<Double> = mutableListOf()
        for (i in temp){
            flow.add(i.flow_rate!!)
        }
        val maxFlow=flow.maxOrNull()
        for(i in temp){
            if (i.flow_rate?.equals(maxFlow) == true){
                peakT = i.sec
            }
        }
        return peakT!!
    }

    fun getAvgFlow(temp: List<UroInfoModel>):String{
        val avgflow :MutableList<Double> = mutableListOf()
        for (i in temp){
            avgflow.add(i.flow_rate!!)
        }
        val maxFlow=avgflow.average()
        return String.format("%.2f", maxFlow)
    }

    fun getTime(num:Long):String{
        val simple: DateFormat = SimpleDateFormat("dd MMM yyyy HH:mm:ss aa")
        val result = Date(num)
        return simple.format(result)
    }

    fun getPosition(value:Int):String {
        if(value==1){
         return "Sitting"
        }else{
            return "Standing"
        }
    }

    fun getMode(mode:Int):String{
        if(mode==1)
          return "Manual"
        else
          return "Auto"
    }

    fun getVoidingTime(time:Int):String{
        if(time==1)
            return "120 s"
        else
            return "180 s"
    }

    private fun getDateLong(opt:String):Long{
        var dt:Long? = null
        val time = Calendar.getInstance();
        when(opt){
            "Last Week" -> {
                time.add(Calendar.DAY_OF_WEEK,-7)
            }
            "Last Month" ->{
                time.add(Calendar.MONTH,-1)
            }
            "Last 2 Months"->{
                time.add(Calendar.MONTH,-2)
            }
            "Last 3 Months"->{
                time.add(Calendar.MONTH,-3)
            }
            "Last 6 Months"->{
                time.add(Calendar.MONTH,-6)
            }
        }
        dt = time.timeInMillis
        return dt!!
    }

    private fun hideSoftKeyboard(view: View) {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

}