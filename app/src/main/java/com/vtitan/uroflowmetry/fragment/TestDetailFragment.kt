package com.vtitan.uroflowmetry.fragment

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ApplicationProvider
import com.example.uroflowmetryapp.activity.ColorBarDrawable
import com.example.uroflowmetryapp.fragment.MsgDialogFragment
import com.example.uroflowmetryapp.fragment.TabContainerFragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.vtitan.uroflowmetry.BuildConfig
import com.vtitan.uroflowmetry.R
import com.vtitan.uroflowmetry.databinding.FragmentTestDetailBinding
import com.vtitan.uroflowmetry.model.PatientDetails
import com.vtitan.uroflowmetry.model.UroInfoModel
import com.vtitan.uroflowmetry.model.relations.UroTestWithUroInfo
import com.vtitan.uroflowmetry.util.SessionManager
import com.vtitan.uroflowmetry.viewmodel.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates


class TestDetailFragment : Fragment() {
    private lateinit var binding: FragmentTestDetailBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var uroLineChart : LineChart
    private var previousVolume = 0.0
    private lateinit var uroinfoViewModel : UroInfoViewModel
    private lateinit var uroinfoViewModelProvider : UroInfoViewModelFactory
    private lateinit var uroTestInfoViewModel: UroTestInfoViewModel
    private lateinit var uroTestInfoViewModelFactory: UroTestInfoViewModelFactory
    private  var testID=0L
    private lateinit var patientViewModel: PatientViewModel
    private lateinit var patientViewModelFactory: PatientViewModelFactory
    private var hesitancy by Delegates.notNull<String>()
    private lateinit var clickListener : MenuBackChangeListener
    private val colArray:List<String> = listOf("#EE2B2B","#EE2B2B"  ,"#C6DB28",  "#2CBC31","#2CBC31","#2CBC31","#2CBC31","#2CBC31","#2CBC31","#2CBC31")

    private val col = ColorBarDrawable(colArray)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentTestDetailBinding.inflate(layoutInflater)
        val application = requireNotNull(this.activity).application
        val bundle = arguments
        val testdetails = bundle?.getSerializable(getString(R.string.key_test_details))as? UroTestWithUroInfo
        val testId = testdetails?.uroTestModel?.test_id
        val patid =testdetails?.uroTestModel?.pat_id
        binding.statusSlider.background = col
        sessionManager = SessionManager(requireContext())

        if (testId != null) {
            testID= testId
        }

        binding.imgBack.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }
        uroinfoViewModelProvider = UroInfoViewModelFactory(application)
        uroinfoViewModel = ViewModelProvider(this,uroinfoViewModelProvider).get(UroInfoViewModel::class.java)
        uroTestInfoViewModelFactory = UroTestInfoViewModelFactory(application)
        uroTestInfoViewModel = ViewModelProvider(this,uroTestInfoViewModelFactory).get(UroTestInfoViewModel::class.java)
        patientViewModelFactory = PatientViewModelFactory(application)
        patientViewModel = ViewModelProvider(this,patientViewModelFactory).get(PatientViewModel::class.java)
        uroLineChart=binding.uroLineChart
        val patientdetails = patientViewModel.getPatientDetails(patid)
        binding.txtPatName.text="${patientdetails.patName}"
        binding.txtReportDate.text="${getTime(testdetails?.uroTestModel?.startTime!!.toLong())}"

//        uroinfoViewModel.getVoidTiming(patid.toString(),testId.toString().toLong()).observe(viewLifecycleOwner,
//            androidx.lifecycle.Observer {
//                if(it !=null) {
//                    hesitancy = "${(it/5).toString()}"
//                }
//            })

        hesitancy = getHesitancy(testdetails.uroInfoModel)

        if(testdetails.uroInfoModel.size !=0){
            val flowrate=getFlow(testdetails?.uroInfoModel)
            if(flowrate.toString().toFloat() > 0) {
                binding.statusSlider.value = flowrate.toString().toFloat()
            }else{
                binding.statusSlider.value = 1F
            }
            //updateProgress(flowrate.toString().toFloat().toInt())
            initializeFlowRateChart(testdetails.uroInfoModel)
            binding.maxFlowRate.text = "${getFlow(testdetails.uroInfoModel)}"
            binding.avgFlowRate.text = "${getAvgFlow(testdetails.uroInfoModel)}"
            binding.txtVoidVolume.text = "${getVolume(testdetails.uroInfoModel)}"
            binding.txtPeakTime.text = "${getPeakTime(testdetails.uroInfoModel)}"
            binding.txtVoidingTime.text = getVoidingTime(testdetails.uroInfoModel,testdetails.uroTestModel.wait_time!!,hesitancy)
        }



        binding.imgDownload.setOnClickListener {
            if(testdetails.uroInfoModel.size !=0 ) {
                dialogConfirm(testdetails, patientdetails)
            }else{
                Toast.makeText(requireContext(),"Test not found.",Toast.LENGTH_LONG).show()
            }
        }

        binding.imgExport.setOnClickListener {
            if(testdetails.uroInfoModel.size !=0 ) {
                dialogExportConfirm(testdetails.uroInfoModel, patientdetails)
            }else{
                Toast.makeText(requireContext(),"Test not found.",Toast.LENGTH_LONG).show()
            }
        }

        return binding.root
    }

    private fun initializeFlowRateChart(uroInfoValues: List<UroInfoModel>) {
        uroLineChart.setDrawGridBackground(false)
        uroLineChart.description.isEnabled = false
        uroLineChart.setTouchEnabled(true)
        uroLineChart.isDragEnabled = true
        uroLineChart.setScaleEnabled(true)
        uroLineChart.setPinchZoom(false)
        uroLineChart.setDrawBorders(true)
        uroLineChart.setBorderWidth(2f)
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
        set2.axisDependency= YAxis.AxisDependency.RIGHT
        // create a data object with the data sets
        val data = LineData()
        data.addDataSet(set1)
        data.addDataSet(set2)
        uroLineChart.data = data
        //chartVolume.setVisibleXRangeMaximum(1000);
    }

    private fun dialogConfirm(testdetails: UroTestWithUroInfo, patientdetails: PatientDetails){
        val dialog = MsgDialogFragment("Confirmation",resources.getString(R.string.do_you_want_to_download))
        dialog.setPositiveStr("View")
        dialog.setNegativeStr("Mail")
        dialog.setConfirmActions(object : MsgDialogFragment.PositiveActionListener {
            override fun onPositiveAction(dialog: DialogInterface?, id: Int) {
                if (sessionManager.getDefaultSetting()){
                    createPdfReport(testdetails,patientdetails,sessionManager,0)
                }
                else{
                    Toast.makeText(requireContext(),"Please Enter Information in Hospital Information",Toast.LENGTH_LONG).show()
                    clickListener.buttonClicked(TabContainerFragment())
                }
            }
        }, object : MsgDialogFragment.NegativeActionListener {
            override fun onNegativeAction(dialog: DialogInterface?, id: Int) {
                if (sessionManager.getDefaultSetting()){
                    createPdfReport(testdetails,patientdetails,sessionManager,1)
                }
                else{
                    Toast.makeText(requireContext(),"Please Enter Information in Hospital Information",Toast.LENGTH_LONG).show()
                    clickListener.buttonClicked(HospitalSettingsFragment())
                }
                dialog!!.cancel()
            }
        })
        val ft: FragmentTransaction? = activity?.supportFragmentManager?.beginTransaction()
        val prev: Fragment? = activity?.supportFragmentManager?.findFragmentByTag("ConfirmDialog")
        if (prev == null) {
            ft?.addToBackStack(null)
            dialog.show(ft!!, "ConfirmDialog")
        }

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

    private fun createPdfReport(testdetails: UroTestWithUroInfo, patientdetails: PatientDetails, sessionManager: SessionManager,mail : Int) {
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
                if(mail == 1) {
                    sendEmail( fileName)
                }else{
                    openPDF( fileName)
                }
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

    private fun getGender(gen:String):String{
        return if (gen=="Male") "M" else if(gen=="Female") "F" else "T"
    }
    private fun getDateTime(): String {
        val date = Calendar.getInstance().timeInMillis
        val simple: DateFormat = SimpleDateFormat("dd/MM/yyyy")
        val result = Date(date)
        return simple.format(result)
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
                peakT = (i.sec)/5
            }
        }
        return peakT!!
    }

  /*  fun getAvgFlow(temp: List<UroInfoModel>):String{
        val avgflow :MutableList<Double> = mutableListOf()
        for (i in temp){
            avgflow.add(i.flow_rate!!)
        }
        val maxFlow=avgflow.average()
        return String.format("%.2f", maxFlow)
    }*/

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

    fun getFlow(temp :List<UroInfoModel>):String{
        val flow :MutableList<Double> = mutableListOf()
        for (i in temp){
            flow.add(i.flow_rate!!)
        }
        val max = flow.maxOrNull()
        return String.format("%.2f", max)
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

    fun getVoidingTime(uroInfoValues: List<UroInfoModel>, mode: Int, hesitancyx: String):String{
        var time = 0
        val lastVolume = uroInfoValues[uroInfoValues.lastIndex].volume_info
        for (i in 0 until uroInfoValues.size){
            if (uroInfoValues[i].volume_info?.equals(lastVolume)!!){
                time = (uroInfoValues[i].sec)/5
                break
            }
        }
        val mode = getMode(mode)
        if (mode.equals("Manual")){
            return (time - hesitancyx.toInt()).toString()
        }
        else{
            return time.toString()
        }
    }

    fun getHesitancy(uroInfoValues: List<UroInfoModel>):String{
        var time = 0
        val firstVolume = uroInfoValues.firstOrNull()?.volume_info
        for (i in 0 until uroInfoValues.size){
            if (!uroInfoValues[i].volume_info?.equals(firstVolume)!!){
                time = (uroInfoValues[i].sec)/5
                break
            }
        }
        return time.toString()
    }

    fun getModeTime(time:Int):String{
        if(time==1)
            return "120 s"
        else
            return "180 s"
    }

    interface MenuBackChangeListener{
        fun buttonClicked(fragment: Fragment)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
         clickListener = context as MenuBackChangeListener
    }

    private suspend fun ExportUroTestDetails(data:List<UroInfoModel>,patientDetails: PatientDetails) {

        ActivityCompat.requestPermissions(requireActivity(), arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE), PackageManager.PERMISSION_GRANTED)

        lateinit var directory_path:String
        if (Build.VERSION_CODES.R > Build.VERSION.SDK_INT) {
            directory_path = Environment.getExternalStorageDirectory().path + "/UroflowmetryReport/TestReport"
        } else {
            directory_path =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath + "/UroflowmetryReport/TestReport"
        }
        val direc = File(directory_path)

        var pathOfFile: File? = context?.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        var fileName: String = "UroTest_test.csv"
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
            val header = "sec,volume,rate,counter,adc,cumulativeVolume"
            if(patientDetails!=null){
                val pat_details="PatientID : ${patientDetails.patId},Name : ${patientDetails.patName},Age : ${patientDetails.patAge},Gender : ${patientDetails.patGender}"
                fileWriter.write(pat_details+"\n")
                println(pat_details)
            }
            fileWriter.write(header+"\n")
            for (i in data!!){
                val point = "${i.sec},${i.volume_info},${i.flow_rate},${i.sequence_no}," +
                        "${i.adc_value},${i.cumulative_volume}"
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

    fun dialogExportConfirm(listItems:List<UroInfoModel>,patientDetails: PatientDetails){
        val dialog = MsgDialogFragment("Export Reports","Do you want to Export data?"+ "\nThe report has been generated successfully.\nLocation :UroTest/Log \nFilename : UroTest_test.csv")
        dialog.setPositiveStr("OK")
        dialog.setNegativeStr("Cancel")
        dialog.setConfirmActions(object : MsgDialogFragment.PositiveActionListener {
            override fun onPositiveAction(dialog: DialogInterface?, id: Int) {
                GlobalScope.async(Dispatchers.IO) {
                    ExportUroTestDetails(listItems,patientDetails)
                }
                dialog?.dismiss()
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

    private fun openPDF(fileName : String) {
        // Get the File location and file name.
        val file = File(Environment.getExternalStorageDirectory().path + "/UroflowmetryReport/PDF/" + fileName)
        // Get the URI Path of file.
        val uriPdfPath =FileProvider.getUriForFile(
            requireContext(),
            BuildConfig.APPLICATION_ID.toString() + ".provider",
            file)
        val pdfOpenIntent = Intent(Intent.ACTION_VIEW)
        pdfOpenIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        //pdfOpenIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        pdfOpenIntent.clipData = ClipData.newRawUri("", uriPdfPath)
        pdfOpenIntent.setDataAndType(uriPdfPath, "application/pdf")
        pdfOpenIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        try {
            startActivity(pdfOpenIntent)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "There is no app to load corresponding PDF", Toast.LENGTH_LONG)
                .show()
        }
         //Log.i("TAGFILE",fileName)
    }


}