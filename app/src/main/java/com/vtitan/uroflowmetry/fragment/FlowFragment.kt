package com.vtitan.uroflowmetry.fragment

import android.annotation.SuppressLint
import android.content.*
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.*
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.example.uroflowmetryapp.fragment.MsgDialogFragment
import com.example.uroflowmetryapp.fragment.TabContainerFragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.snackbar.Snackbar
import com.vtitan.uroflowmetry.BuildConfig
import com.vtitan.uroflowmetry.R
import com.vtitan.uroflowmetry.adapter.BleDeviceNameListAdapter.DCListItemListener
import com.vtitan.uroflowmetry.databinding.FragmentFlowBinding
import com.vtitan.uroflowmetry.model.PatientDetails
import com.vtitan.uroflowmetry.model.UroInfoModel
import com.vtitan.uroflowmetry.model.UroTestModel
import com.vtitan.uroflowmetry.model.relations.UroTestWithUroInfo
import com.vtitan.uroflowmetry.services.BluetoothLeServiceKt
import com.vtitan.uroflowmetry.util.Constants
import com.vtitan.uroflowmetry.util.SessionManager
import com.vtitan.uroflowmetry.viewmodel.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.RoundingMode
import java.text.DateFormat
import java.text.DecimalFormat
import java.util.*
import kotlin.properties.Delegates


class FlowFragment : Fragment(), DCListItemListener {
    private lateinit var binding: FragmentFlowBinding
    private lateinit var sessionManager:SessionManager
    private  var mBluetoothLeService:BluetoothLeServiceKt? = null
    private  var mBLEService :Messenger? =null
    private lateinit var mDeviceAddress : String
    private lateinit var uroLineChart : LineChart
    private var previousVolume = 0.0
    private lateinit var uroinfoViewModel : UroInfoViewModel
    private lateinit var uroinfoViewModelProvider : UroInfoViewModelFactory
    private lateinit var uroTestInfoViewModel: UroTestInfoViewModel
    private lateinit var uroTestInfoViewModelFactory: UroTestInfoViewModelFactory
    private lateinit var patientViewModel: PatientViewModel
    private lateinit var patientViewModelFactory: PatientViewModelFactory
    private lateinit var testStatusReciver : BroadcastReceiver
    private var isTestStarted =false
    private  var testID=0L
    private var patID: String? =null
    private var hesitancy by Delegates.notNull<String>()
    private var mode by Delegates.notNull<Int>()
    private var isfirst=true
    private var density=1F
    private var detail : MutableList<UroTestWithUroInfo> = mutableListOf()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.getWindow()?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        binding = FragmentFlowBinding.inflate(layoutInflater)

        binding.imgBack.setOnClickListener {
            if(isTestStarted){
               dialogStopTestConfirm()
            }else {
                activity?.supportFragmentManager?.popBackStack()
            }
        }
        val application = requireNotNull(this.activity).application
        val df = DecimalFormat("#.####")
        sessionManager=SessionManager(requireContext())
        df.roundingMode = RoundingMode.CEILING
        //calibration  = df.format(getCalibrationFactor()).toDouble()
        val bundle = arguments
        val testDetails=bundle?.getSerializable(resources.getString(R.string.key_test_details)) as? UroTestModel
        val densityval=bundle?.getFloat(resources.getString(R.string.key_density))
        density=densityval!!.toFloat()
        val patid = testDetails?.pat_id
        val waitingTime =testDetails?.wait_time
        val position =testDetails?.position
        val testMode =testDetails?.test_mode
        val testId = testDetails?.test_id
        val patName=bundle?.get(getString(R.string.key_pat_name))
        binding.txtPatName.text=patName.toString()
        testID= testId.toString().toLong()
        patID=patid.toString()
        mode=testMode.toString().toInt()
        uroinfoViewModelProvider = UroInfoViewModelFactory(application)
        uroinfoViewModel = ViewModelProvider(this,uroinfoViewModelProvider).get(UroInfoViewModel::class.java)
        uroTestInfoViewModelFactory = UroTestInfoViewModelFactory(application)
        uroTestInfoViewModel = ViewModelProvider(this,uroTestInfoViewModelFactory).get(UroTestInfoViewModel::class.java)
        patientViewModelFactory = PatientViewModelFactory(application)
        patientViewModel = ViewModelProvider(this,patientViewModelFactory).get(PatientViewModel::class.java)
        uroLineChart=binding.uroLineChart
        if(testMode==2) {
            if(testDetails != null) {
                uroTestInfoViewModel.insert(testDetails)
            }
            binding.imgStartTest.visibility = View.GONE
            binding.txtStartTest.visibility=View.GONE
            val intent = Intent(activity, BluetoothLeServiceKt::class.java)
            intent.putExtra(getString(R.string.key_is_first), true)
            intent.putExtra(getString(R.string.key_pat_id),patid.toString())
            intent.putExtra(getString(R.string.key_waiting_time), waitingTime.toString().toInt())
            intent.putExtra(getString(R.string.key_mode),testMode.toString().toInt())
            intent.putExtra(getString(R.string.key_position),position.toString().toInt())
            intent.putExtra(getString(R.string.key_is_record),true)
            intent.putExtra(getString(R.string.key_test_id),testId.toString().toLong())
            intent.putExtra(getString(R.string.key_density),df.format(density).toDouble())
            activity?.startService(intent)
        }else{
            binding.imgStartTest.visibility = View.VISIBLE
            binding.txtStartTest.visibility=View.VISIBLE
        }

        binding.imgStartTest.setOnClickListener{
            if (testDetails != null) {
                uroTestInfoViewModel.insert(testDetails)
            }
            val intent = Intent(activity, BluetoothLeServiceKt::class.java)
            intent.putExtra(getString(R.string.key_is_first), true)
            intent.putExtra(getString(R.string.key_pat_id),patid.toString())
            intent.putExtra(getString(R.string.key_waiting_time), waitingTime.toString().toInt())
            intent.putExtra(getString(R.string.key_mode),testMode.toString().toInt())
            intent.putExtra(getString(R.string.key_position),position.toString().toInt())
            intent.putExtra(getString(R.string.key_is_record),true)
            intent.putExtra(getString(R.string.key_test_id),testId.toString().toLong())
            intent.putExtra(getString(R.string.key_density),df.format(density).toDouble())
            activity?.startService(intent)
        }

        binding.imgStopTest.setOnClickListener {
            isTestStarted=false
            val intent = Intent(requireContext(), BluetoothLeServiceKt::class.java)
            intent.putExtra(getString(R.string.key_is_record),false)
            context?.startService(intent)
            binding.imgStartTest.visibility = View.GONE
            binding.txtStartTest.visibility=View.GONE
            binding.txtStopTest.visibility=View.GONE
            binding.imgStopTest.visibility = View.GONE
            dialogConfirm(System.currentTimeMillis(),testID,patID!!)
        }

        uroinfoViewModel.getCurrentTestDetails(patid.toString(),testId.toString().toLong()).observe(viewLifecycleOwner,
            androidx.lifecycle.Observer {
                if(it.size > 0) {
                    initializeFlowRateChart(it)
                    binding.maxFlowRate.text = "${getFlow(it)}"
                    binding.avgFlowRate.text = "${getAvgFlow(it)}"
                    binding.txtVoidVolume.text = "${getVolume(it)}"
                    binding.txtPeakTime.text = "${getPeakTime(it)}"
                    hesitancy=getHesitancy(it)
                    binding.txtVoidingTime.text = getVoidingTime(it,
                        testMode.toString().toInt()!!,
                        hesitancy
                    )
                }
            })
        registerTestStatusReceiver()
        // Inflate the layout for this fragment
        return binding.root
    }

    // Code to manage Service lifecycle.
    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            mBLEService = Messenger(service)
            mBluetoothLeService = (service as BluetoothLeServiceKt.LocalBinder).serviceKt
            if (!(mBluetoothLeService?.initialize()!!)) {
                println("Unable to initialize Bluetooth")
            }
            // Automatically connects to the device upon successful start-up initialization.
            val b: Boolean = mBluetoothLeService?.connect(sessionManager.getMacId())!!
            if (b) {
                println("Success")
            } else {
                println("failed")
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mBluetoothLeService = null
            mBLEService = null
        }
    }

    override fun onItemClick(v: View?, mac_id: String?) {
        println(mac_id)
        mDeviceAddress=mac_id!!
        val gattServiceIntent = Intent(requireContext(), BluetoothLeServiceKt::class.java)
        gattServiceIntent.putExtra("dev_mac", mac_id)
        context?.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun OnItemDiconnectClick(v: View?, mac_id: String?) {
        // TODO("Not yet implemented")
    }

    private fun initializeFlowRateChart(uroInfoValues: List<UroInfoModel>) {
        uroLineChart.setDrawGridBackground(false)
        uroLineChart.description.isEnabled = false
        uroLineChart.setTouchEnabled(false)
        uroLineChart.isDragEnabled = true
        uroLineChart.setScaleEnabled(true)
        uroLineChart.setPinchZoom(true)
        val xl = uroLineChart.xAxis
        xl.position = XAxis.XAxisPosition.BOTTOM
        xl.setAvoidFirstLastClipping(true)
        //xl.setValueFormatter(valueFormatter);
        xl.setAvoidFirstLastClipping(false)
        xl.setDrawGridLines(false)
        xl.labelCount = 5
        // xl.setAxisMinimum(0f);
        val leftAxis =uroLineChart.axisLeft
        leftAxis.setAxisMinimum(0f);
        // this replaces setStartAtZero(true)
        val rightAxis = uroLineChart.axisRight
        rightAxis.isEnabled = true
        rightAxis.setDrawGridLines(false)
        val l = uroLineChart.legend
        l.form = Legend.LegendForm.SQUARE
        l.formSize=10f
        l.textSize=14f
        if (uroInfoValues.size > 0) {
            setChartValues(uroInfoValues)
        } else {
            uroLineChart.data = null
        }
        uroLineChart.invalidate()
    }

    private fun setChartValues(uroInfoValues: List<UroInfoModel>) {
        val entries = ArrayList<Entry>()
        val volumentries=ArrayList<Entry>()
        for (i in 0 .. uroInfoValues.size-1) {
            val vxVal = uroInfoValues[i].sec.toFloat() / 5
            val vyVal = uroInfoValues[i].volume_info!!.toFloat()
            volumentries.add(Entry(vxVal, vyVal))
            if (uroInfoValues[i].sec % 5 == 0) {
                val xVal = uroInfoValues[i].sec.toFloat() / 5
                val yVal = (uroInfoValues[i].volume_info!! - previousVolume).toFloat()
                entries.add(Entry(xVal, yVal))
                Log.i("YVALUE", "$yVal:$previousVolume")
                previousVolume = uroInfoValues[i].volume_info!!
            }
        }
        previousVolume = 0.0
        val set1 = LineDataSet(entries, resources.getString(R.string.txt_flow_rate))
        val set2 = LineDataSet(volumentries, resources.getString(R.string.txt_volume))

        set1.color = resources.getColor(R.color.font_pink)
        set1.setDrawValues(false)
        set1.setDrawCircleHole(false)
        set1.setDrawCircles(false)
        set1.disableDashedLine()
        set1.mode = LineDataSet.Mode.CUBIC_BEZIER

        set2.color = resources.getColor(R.color.purple_200)
        set2.setDrawValues(false)
        set2.setDrawCircleHole(false)
        set2.setDrawCircles(false)
        set2.disableDashedLine()
        set2.mode = LineDataSet.Mode.CUBIC_BEZIER
        set2.axisDependency=YAxis.AxisDependency.RIGHT
        // create a data object with the data sets
        val data = LineData()
        data.addDataSet(set1)
        data.addDataSet(set2)
        uroLineChart.data = data
        //chartVolume.setVisibleXRangeMaximum(1000);
    }

    override fun onResume() {
        if (mBluetoothLeService != null) {
            val result = mBluetoothLeService!!.connect(mDeviceAddress)
            println("Connect request result=$result")
        }
        super.onResume()
    }

    override fun onDestroy() {
        if(testStatusReciver!=null){
            context?.unregisterReceiver(testStatusReciver)
        }
        super.onDestroy()
    }

    private fun registerTestStatusReceiver() {
        Log.i(ContentValues.TAG, "teststatusReceiver :: Start ")
        testStatusReciver = object : BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != null && intent.action == Constants.START_TEST) {
                    var test= intent.getBooleanExtra(Constants.TEST_STATUS, false)
                    if(test){
                        isTestStarted=true
                        binding.txtStartTest.visibility=View.GONE
                        binding.imgStartTest.visibility=View.GONE
                        binding.imgStopTest.visibility=View.VISIBLE
                        binding.txtStopTest.visibility=View.VISIBLE
                        println("TestStarted_flow")
                    }else{
                        isTestStarted=false
                        binding.imgStartTest.visibility=View.GONE
                        binding.imgStopTest.visibility=View.GONE
                        binding.txtStartTest.visibility=View.GONE
                        binding.txtStopTest.visibility=View.GONE
                         dialogConfirm(System.currentTimeMillis(),testID,patID!!)
                        println("TestCompleted_Flow")
                    }
                }
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(Constants.START_TEST)
        context?.registerReceiver(testStatusReciver, intentFilter)
    }



    @SuppressLint("RestrictedApi")
    fun showVoidStarted(view: View){
        val snackbar = Snackbar.make(view,"",Snackbar.LENGTH_LONG)
        val customSnackbar = layoutInflater.inflate(R.layout.snackbar_dark_layout,null)
        val snackbarLayout= snackbar.view as Snackbar.SnackbarLayout
        snackbarLayout.setPadding(0,0,0,0)
        snackbarLayout.addView(customSnackbar,0)
        snackbarLayout.setOnClickListener(View.OnClickListener { snackbar.dismiss() })
        snackbar.show()
    }


    fun dialogConfirm(stopTime:Long,testId:Long,patid:String){
        val dialog = MsgDialogFragment("Test Status",resources.getString(R.string.test_completed))
        dialog.setPositiveStr("View")
        dialog.setNegativeStr("Mail")
        dialog.setConfirmActions(object : MsgDialogFragment.PositiveActionListener {
            override fun onPositiveAction(dialog: DialogInterface?, id: Int) {
                    uroTestInfoViewModel.updateTestStatus(stopTime, testId)
                val testDetails=uroTestInfoViewModel.getUroTestWithUroInfo(testId)
               // val patientdetails = patientViewModel.getPatientDetails(patid)
                val testDetailfragment = TestDetailFragment()
                val bundle = Bundle()
                bundle.putSerializable(getString(R.string.key_test_details),testDetails)
                testDetailfragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, testDetailfragment)?.commit()
                    /*activity?.supportFragmentManager?.beginTransaction()
                        ?.replace(R.id.main_content, HomeFragment())?.commit()*/
                dialog?.dismiss()

            }
        }, object : MsgDialogFragment.NegativeActionListener {
            override fun onNegativeAction(dialog: DialogInterface?, id: Int) {
                uroTestInfoViewModel.updateTestStatus(stopTime, testId)
                val testDetails=uroTestInfoViewModel.getUroTestWithUroInfo(testId)
                 val patientdetails = patientViewModel.getPatientDetails(patid)
                if (sessionManager.getDefaultSetting()){
                    createPdfReport(testDetails,patientdetails,sessionManager)
                }
                else{
                    Toast.makeText(requireContext(),"Please Enter Information in Hospital Information",Toast.LENGTH_LONG).show()
                    //clickListener.buttonClicked(TabContainerFragment())
                }
                dialog!!.cancel()
            }
        })
        val ft: FragmentTransaction? = activity?.getSupportFragmentManager()?.beginTransaction()
        val prev: Fragment? = activity?.getSupportFragmentManager()?.findFragmentByTag("ConfirmDialog")
        if (prev != null) {
            ft?.remove(prev)
        }
        ft?.addToBackStack(null)
        dialog.show(ft!!, "ConfirmDialog")
    }

    fun sendEmail(fPath: String?) {
        val date = Date()
        val formatter = android.icu.text.SimpleDateFormat("dd-MM-yyyy hh:mm aa")
        val strDate = formatter.format(date)
        val subject = "UroFlow Reports - $strDate"
        val message = "Hi, \n\n Attached the Uroflow Reports generated at $strDate"
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, message)
        //intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{""});
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        val fExp = File(Environment.getExternalStorageDirectory().path + "/UroflowmetryReport/PDF/" + fPath)
        val uris = ArrayList<Uri>()
        uris.add(
            FileProvider.getUriForFile(
                requireContext(),
                BuildConfig.APPLICATION_ID.toString() + ".provider",
                fExp
            )
        )
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        startActivity(intent)
    }

    fun getVolume(temp :List<UroInfoModel>):String{
        var maxVolume=0.0
        val vol :MutableList<Double> = mutableListOf()
        for (i in temp){
            vol.add(i.volume_info!!)
        }
        val max = vol.maxOrNull()
        if(max != null){
            maxVolume= max
        }
        return String.format("%.2f", maxVolume)
    }

    fun getPeakTime(temp:List<UroInfoModel>):Int{
        var peakT=0
        val flow :MutableList<Double> = mutableListOf()
        for (i in temp){
            flow.add(i.flow_rate!!)
        }
        val maxFlow=flow.maxOrNull()
        for(i in temp){
            if(maxFlow!! > 0){
                if (i.flow_rate?.equals(maxFlow) == true){
                    peakT = (i.sec)/5
                }
            }
        }
        return peakT
    }
    fun getAvgFlow(temp: List<UroInfoModel>):String{
        var avgFlowRate=0.0
        var avgflow : MutableList<Double> = mutableListOf()
        val timeFlow: MutableList<Int> = mutableListOf()
        var index1 = 0
        var index2 = 0
        var check = 0.0
        for (i in temp){
            avgflow.add(i.flow_rate!!)
            timeFlow.add(i.sec)
        }
        for (i in 0 until avgflow.size){
            if (avgflow[i]!=check){
                index1 = i
                break
            }
        }
        for (i in avgflow.size until 0 step -1){
            if (avgflow[i]!=check){
                index2 = i
                break
            }
        }
        avgflow = avgflow.subList(index1,index2)
        val time = (timeFlow[index2]-timeFlow[index1])/5
        avgFlowRate = avgflow.sum()/time
        return String.format("%.2f", avgFlowRate)
    }
   /* fun getAvgFlow(temp: List<UroInfoModel>):String{
        var avgFlowRate=0.0
        val avgflow :MutableList<Double> = mutableListOf()
        for (i in temp){
            if(i.sec % 5 == 0 ) {
                avgflow.add(i.flow_rate!!)
            }
        }
        val avgFlow=avgflow.average()
        avgFlowRate=avgFlow
        return String.format("%.2f", avgFlowRate)
    }*/

    fun getFlow(temp :List<UroInfoModel>):String{
        var maxFlowRate=0.0
        val flow :MutableList<Double> = mutableListOf()
        for (i in temp){
            flow.add(i.flow_rate!!)
        }
        val max = flow.maxOrNull()
        if(max !=null){
            maxFlowRate=max
        }
        return String.format("%.2f", maxFlowRate)
    }
    fun getVoidingTime(uroInfoValues: List<UroInfoModel>, mode: Int, hesitancyx: String):String{
        var time = 0
        val lastVolume = uroInfoValues.lastOrNull()?.volume_info
        if (lastVolume!=null) {
            for (i in 0 until uroInfoValues.size) {
                if (uroInfoValues[i].volume_info?.equals(lastVolume)!!) {
                    time = (uroInfoValues[i].sec) / 5
                    break
                }
            }
        }
        val mode = getMode(mode)
        if (mode.equals("Manual") && hesitancyx !="NA"){
            return (time - hesitancyx.toInt()).toString()
        }
        else{
            return time.toString()
        }
    }

    fun getHesitancy(uroInfoValues: List<UroInfoModel>):String{
        var time = 0
        val firstVolume = uroInfoValues.firstOrNull()?.volume_info
        if (firstVolume!=null) {
            for (i in 0 until uroInfoValues.size) {
                if (!uroInfoValues[i].volume_info?.equals(firstVolume)!!) {
                    time = (uroInfoValues[i].sec) / 5
                    break
                }
            }
            return time.toString()
        }
        else{
            return time.toString()
        }
    }

    fun getMode(mode:Int):String{
        if(mode==1)
            return "Manual"
        else
            return "Auto"
    }
    private fun createPdfReport(testdetails: UroTestWithUroInfo, patientdetails: PatientDetails, sessionManager: SessionManager) {
        val pdfDocument = PdfDocument()
        val paint = Paint()

        var xCur = 15f
        var yCur = 15f

        val pageInfo = PdfDocument.PageInfo.Builder(710,1000,1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas



        var logoW = 58
        var logoH = 58
        val logoToBytes = Base64.decode(sessionManager.getLogo(), Base64.DEFAULT)
        val decodedLogoBytes = BitmapFactory.decodeByteArray(logoToBytes, 0, logoToBytes.size)
        val scaledBmp = Bitmap.createScaledBitmap(decodedLogoBytes,logoW,logoH,false)
        canvas.drawBitmap(scaledBmp,xCur,yCur,paint)



        val hosName = proccessingHospitalName(sessionManager.getHospitalName().uppercase())
        xCur += logoW +15
        yCur += 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(0,0,0)
        paint.textSize = 15f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("${hosName[0]}",xCur,yCur,paint)
        yCur+= 15
        canvas.drawText("${hosName[1]}",xCur,yCur,paint)



        val hosAddress = proccessingHospitalAddress(sessionManager.getAddress().uppercase())
        yCur += 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10f
        paint.color = Color.rgb(0,0,0)
        canvas.drawText("${hosAddress[0]}",xCur,yCur,paint)
        yCur += 13f
        canvas.drawText("${hosAddress[1]}",xCur,yCur,paint)



        xCur = (pageInfo.pageWidth/2).toFloat()
        yCur+= 35f
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 18f
        paint.color = Color.rgb(0,0,0 )
        canvas.drawText("Uroflowmeter Report",xCur,yCur,paint)



        xCur = pageInfo.pageWidth-15f
        yCur += 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(0,0,0)
        paint.textSize = 15f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Date : ${getDateTime()}",xCur,yCur,paint)



        yCur += 15f
        xCur = 15f
        paint.color = Color.rgb(0,0,0)
        canvas.drawLine(xCur,yCur,pageInfo.pageWidth - xCur,yCur,paint)
        canvas.drawLine(xCur,yCur,xCur,yCur+50,paint)
        canvas.drawLine(pageInfo.pageWidth - xCur,yCur,pageInfo.pageWidth - xCur,yCur+50,paint)

        yCur += 20f
        xCur=30f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(0,0,0)
        paint.textSize = 15f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Patient ID       : ${patientdetails.patId}",xCur,yCur,paint)

        paint.textAlign = Paint.Align.LEFT
        xCur+= 250f
        canvas.drawText("Age/Gender : ${patientdetails.patAge} years/${getGender(patientdetails.patGender!!)}",xCur,yCur,paint)

        paint.textAlign = Paint.Align.LEFT
        xCur+= 255f
        canvas.drawText("Test Mode     : ${getMode(testdetails.uroTestModel.test_mode!!)}",xCur,yCur,paint)

        yCur += 20f
        xCur=30f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Patient Name : ${patientdetails.patName}",xCur,yCur,paint)

        paint.textAlign = Paint.Align.LEFT
        xCur+= 250f
        canvas.drawText("Weight         : ${patientdetails.patWeight} kg",xCur,yCur,paint)

        paint.textAlign = Paint.Align.LEFT
        xCur+= 255f
        canvas.drawText("Test Position : ${getPosition(testdetails.uroTestModel.position!!)}",xCur,yCur,paint)


        yCur += 10f
        xCur = 15f
        canvas.drawLine(xCur,yCur,pageInfo.pageWidth - xCur,yCur,paint)



        yCur += 25f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(0,0,0)
        paint.textSize = 15f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Reffered By : ${patientdetails.patReffered}",xCur,yCur,paint)
        xCur = pageInfo.pageWidth-15f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Examined By : ${patientdetails.patExamined}",xCur,yCur,paint)



        yCur += 40f
        logoW = 670
        logoH = 400
        canvas.drawLine(xCur,yCur,pageInfo.pageWidth - xCur,yCur,paint)
        canvas.drawLine(xCur,yCur,xCur,yCur+405,paint)
        canvas.drawLine(pageInfo.pageWidth - xCur,yCur,pageInfo.pageWidth - xCur,yCur+405,paint)
        yCur +=2f
        xCur = 18f
        var bitmap = uroLineChart.chartBitmap
//        val graphByte = Base64.decode(testdetails.uroTestModel.test_img, Base64.DEFAULT)
//        val graphDecoded = BitmapFactory.decodeByteArray(graphByte, 0, graphByte.size)
        if (bitmap!=null){
            val scaledgraph = Bitmap.createScaledBitmap(bitmap,logoW,logoH,false)
            canvas.drawBitmap(scaledgraph,xCur,yCur,paint)
        }
        yCur += 403f
        xCur = 15f
        canvas.drawLine(xCur,yCur,pageInfo.pageWidth - xCur,yCur,paint)


        yCur += 25f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(0,0,0)
        paint.textSize = 15f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Uroflowmetry Summary",xCur,yCur,paint)

        yCur += 25f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(0,0,0)
        paint.textSize = 15f
        xCur=15f
        paint.textAlign = Paint.Align.LEFT
        if (testdetails.uroTestModel.test_mode==1){
            canvas.drawText("Hesitancy                           : ${hesitancy} sec",xCur,yCur,paint)
        }
        else{
            canvas.drawText("Hesitancy                           : NA",xCur,yCur,paint)
        }

        paint.textAlign = Paint.Align.LEFT
        xCur=pageInfo.pageWidth.toFloat()-170f
        canvas.drawText("Void Volume : ${getVolume(testdetails.uroInfoModel)} ml",xCur,yCur,paint)

        yCur += 20f
        xCur=15f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Maximum flow rate(Qmax): ${getFlow(testdetails.uroInfoModel)} ml/sec",xCur,yCur,paint)

        paint.textAlign = Paint.Align.LEFT
        xCur=pageInfo.pageWidth.toFloat()-170f
        canvas.drawText("Voiding Time : ${getVoidingTime(
            testdetails.uroInfoModel,
            testdetails.uroTestModel.wait_time!!,
            hesitancy
        )} sec",xCur,yCur,paint)

        yCur += 20f
        xCur=15f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Average flow rate(Qavg)    : ${getAvgFlow(testdetails.uroInfoModel)} ml/sec",xCur,yCur,paint)

        paint.textAlign = Paint.Align.LEFT
        xCur=pageInfo.pageWidth.toFloat()-170f
        canvas.drawText("Peak Time     : ${getPeakTime(testdetails.uroInfoModel)} sec",xCur,yCur,paint)


        yCur += 25f
        xCur=15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(0,0,0)
        paint.textSize = 15f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Remarks",xCur,yCur,paint)
        yCur+=15f
        paint.color = Color.rgb(0,0,0)
//        canvas.drawLine(xCur,yCur,pageInfo.pageWidth - xCur,yCur,paint)
//        canvas.drawLine(xCur,yCur,xCur,yCur+50,paint)
//        canvas.drawLine(pageInfo.pageWidth - xCur,yCur,pageInfo.pageWidth - xCur,yCur+50,paint)
        yCur+=50
        canvas.drawLine(xCur,yCur,pageInfo.pageWidth - xCur,yCur,paint)


        yCur+=50f
        canvas.drawLine(pageInfo.pageWidth - (6*xCur),yCur,pageInfo.pageWidth - xCur,yCur,paint)
        yCur+=20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(0,0,0)
        paint.textSize = 15f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("(Signature)",pageInfo.pageWidth - xCur,yCur,paint)
        yCur=pageInfo.pageHeight.toFloat() - 85
        xCur = pageInfo.pageWidth.toFloat()/2 - 40
        logoW = 100
        logoH = 25
        val logoBmp = BitmapFactory.decodeResource(resources,R.drawable.vtitan_logo)
        val logoScaledBmp = Bitmap.createScaledBitmap(logoBmp,logoW,logoH,false)
        canvas.drawBitmap(logoScaledBmp,xCur,yCur,paint)
        yCur=pageInfo.pageHeight.toFloat() - 40
        xCur = pageInfo.pageWidth.toFloat()/2
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 13f
        paint.color = Color.rgb(0,0,0 )
        canvas.drawText("This report requires clinical correlation for any clinical actions.",xCur,yCur,paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        yCur=pageInfo.pageHeight.toFloat() - 20
        canvas.drawText("vTitan Corporation Pvt Ltd | www.vtitan.com",xCur,yCur,paint)


        pdfDocument.finishPage(page)
        lateinit var directory_path:String
        if (Build.VERSION_CODES.R > Build.VERSION.SDK_INT) {
            directory_path = Environment.getExternalStorageDirectory().path + "/UroflowmetryReport/PDF"
        } else {
            directory_path =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath + "/UroflowmetryReport/PDF"
        }

        val direc = File(directory_path)
        var fileName: String = "${testdetails.uroTestModel.pat_id} ${getTime(testdetails.uroTestModel.startTime!!)}.pdf"
        if (direc.exists()!! && direc?.isDirectory!!){
            for (child in direc.listFiles()) {
//                child.delete()
            }
        }
        try {
            direc.mkdirs()
            val file = File(directory_path,fileName)
            file.createNewFile()
            pdfDocument.writeTo(FileOutputStream(file))
            GlobalScope.launch(Dispatchers.Main) {
                sendEmail( fileName)
                Toast.makeText(requireContext(), "Downloaded", Toast.LENGTH_SHORT).show()
            }
        }catch (e : IOException){
            GlobalScope.launch(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Download failed", Toast.LENGTH_SHORT).show()
            }
            Log.i("ERROR","${e.printStackTrace()}")
            e.printStackTrace()
        }
        pdfDocument.close()
//        openPdfFile("/Uroflowmetry/log/$fileName")
    }

    private fun getGender(gen:String):String{
        return if (gen=="Male") "M" else if(gen=="Female") "F" else "T"
    }
    private fun proccessingHospitalName(str: String): MutableList<String> {
        val tem = str.split(' ')
        val toRet = mutableListOf("","")
        tem.subList(0,tem.size-2)
        toRet[0]=tem.subList(0,tem.size-2).joinToString(" ")
        toRet[1]=tem.subList(tem.size-2,tem.size).joinToString(" ")
        return toRet
    }

    private fun proccessingHospitalAddress(str: String): MutableList<String> {
        val tem = str.split(',')
        val toRet = mutableListOf("","")
        tem.subList(0,tem.size-2)
        toRet[0]=tem.subList(0,tem.size-2).joinToString(",")
        toRet[1]=tem.subList(tem.size-2,tem.size).joinToString(",")
        return toRet
    }

    private fun getDateTime(): String {
        val date = Calendar.getInstance().timeInMillis
        val simple: DateFormat = java.text.SimpleDateFormat("dd/MM/yyyy")
        val result = Date(date)
        return simple.format(result)
    }

    fun getPosition(value:Int):String {
        if(value==1){
            return "Sitting"
        }else{
            return "Standing"
        }
    }

    fun getTime(num:Long):String{
        val simple: DateFormat = java.text.SimpleDateFormat("dd MMM yyyy HH:mm:ss aa")
        val result = Date(num)
        return simple.format(result)
    }


    fun dialogStopTestConfirm(){
        val dialog = MsgDialogFragment("Confirmation",resources.getString(R.string.test_in_progress))
        dialog.setPositiveStr("Yes")
        dialog.setNegativeStr("No")
        dialog.setConfirmActions(object : MsgDialogFragment.PositiveActionListener {
            override fun onPositiveAction(dialog: DialogInterface?, id: Int) {
                isTestStarted=false
                val intent = Intent(requireContext(), BluetoothLeServiceKt::class.java)
                intent.putExtra(getString(R.string.key_is_record),false)
                context?.startService(intent)
                uroinfoViewModel.deleteTestInfo(testID)
                uroTestInfoViewModel.deleteTestDetails(testID)
                activity?.supportFragmentManager?.beginTransaction()?.replace(
                    R.id.device_setup_container,
                    HomeFragment()
                )?.commit()
            }
        }, object : MsgDialogFragment.NegativeActionListener {
            override fun onNegativeAction(dialog: DialogInterface?, id: Int) {
                dialog!!.cancel()
            }
        })
        val ft: FragmentTransaction? = activity?.getSupportFragmentManager()?.beginTransaction()
        val prev: Fragment? = activity?.getSupportFragmentManager()?.findFragmentByTag("ConfirmDialog")
        if (prev != null) {
            ft?.remove(prev)
        }
        ft?.addToBackStack(null)
        dialog.show(ft!!, "ConfirmDialog")
    }



}