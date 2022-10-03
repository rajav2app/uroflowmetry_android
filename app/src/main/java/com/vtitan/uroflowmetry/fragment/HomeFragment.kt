package com.vtitan.uroflowmetry.fragment

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.uroflowmetryapp.fragment.TabContainerFragment
import com.vtitan.uroflowmetry.R
import com.vtitan.uroflowmetry.databinding.FragmentHomeBinding
import com.vtitan.uroflowmetry.model.PatientDetails
import com.vtitan.uroflowmetry.model.UroTestModel
import com.vtitan.uroflowmetry.util.SessionManager
import com.vtitan.uroflowmetry.viewmodel.PatientViewModel
import com.vtitan.uroflowmetry.viewmodel.PatientViewModelFactory
import com.vtitan.uroflowmetry.viewmodel.UroTestInfoViewModel
import com.vtitan.uroflowmetry.viewmodel.UroTestInfoViewModelFactory



class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var patientViewModel: PatientViewModel
    private lateinit var patientViewModelProvider: PatientViewModelFactory
    private lateinit var uroTestViewModel : UroTestInfoViewModel
    private lateinit var uroTestViewModelProvider : UroTestInfoViewModelFactory
    private var isNew=true
    private lateinit var sessionManager: SessionManager
    private lateinit var clickListener: HomeInterface

    interface HomeInterface {
        fun redirectFragment(fragment:Fragment)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(layoutInflater)
        val application = requireNotNull(this.activity).application
        patientViewModelProvider = PatientViewModelFactory(application)
        patientViewModel = ViewModelProvider(this,patientViewModelProvider).get(PatientViewModel::class.java)
        uroTestViewModelProvider = UroTestInfoViewModelFactory(application)
        uroTestViewModel = ViewModelProvider(this,uroTestViewModelProvider).get(UroTestInfoViewModel::class.java)

        sessionManager= SessionManager(requireContext())
        println(sessionManager.getMacId())
        val patientList=patientViewModel.getPatientIds()
        val patientIdAdapter = ArrayAdapter(requireContext(),
            R.layout.spinner_item, patientList)
        binding.patientIdText.setAdapter(patientIdAdapter)

        val positionAdapter = ArrayAdapter(requireContext(),
            R.layout.spinner_item, resources.getStringArray(R.array.position_dropdown_list))
        binding.patientPositionText.setAdapter(positionAdapter)
        binding.patientPositionText.setText(positionAdapter.getItem(0), false)
       // binding.patientPositionText.isEnabled=false

        val genderAdapter = ArrayAdapter(requireContext(),
            R.layout.spinner_item, resources.getStringArray(R.array.gender_dropdown_list))
        binding.patientGenderText.setAdapter(genderAdapter)
        binding.patientGenderText.setText(genderAdapter.getItem(0), false)
        //binding.patientGenderText.isEnabled=false

        val testModeAdapter = ArrayAdapter(requireContext(),
            R.layout.spinner_item, resources.getStringArray(R.array.mode_dropdown_list))
        binding.patientModeText.setAdapter(testModeAdapter)
        binding.patientModeText.setText(testModeAdapter.getItem(0), false)
       // binding.patientModeText.isEnabled=false
        val waitTimeAdapter = ArrayAdapter(requireContext(),
            R.layout.spinner_item, resources.getStringArray(R.array.wait_time_dropdown_list))
        binding.patientWaitTimeText.setAdapter(waitTimeAdapter)
        binding.patientWaitTimeText.setText(waitTimeAdapter.getItem(0), false)

        if(binding.densitySwitch.isChecked){
            binding.densityContainer.isEnabled=true
        }else
        {
            binding.densityContainer.isEnabled=false
        }
        binding.densitySwitch.setOnCheckedChangeListener { compoundButton, b ->
            if(b){
                binding.densityContainer.isEnabled=true
            }else{
                binding.densityContainer.isEnabled=false
            }
        }

        binding.patientIdText.setOnItemClickListener { adapterView, view, i, l ->
            val patiendDetails=patientViewModel.getPatientDetails(patientIdAdapter.getItem(i))
            if(patiendDetails!=null){
                isNew=false
                binding.patientNameText.setText(patiendDetails.patName)
                binding.patientWeightText.setText(patiendDetails.patWeight.toString())
                binding.patientAgeText.setText(patiendDetails.patAge.toString())
                binding.patientGenderText.setText(patiendDetails.patGender)
                binding.patientPositionText.setText(positionAdapter.getItem(patiendDetails.position?.toInt()
                    ?: 0), false)
                binding.patientModeText.setText(testModeAdapter.getItem(patiendDetails.test_mode?.toInt()
                    ?: 0), false)
                binding.patientWaitTimeText.setText(waitTimeAdapter.getItem(patiendDetails.wait_time?.toInt()
                    ?: 0), false)
                binding.patientReferredByText.setText(patiendDetails.patReffered)
                binding.patientExaminedByText.setText(patiendDetails.patExamined)
            }
            hideSoftKeyboard(binding.patientIdText)

        }

        binding.startNewTest.setOnClickListener {

            val patientId=binding.patientIdText.text.toString()
            val patientName=binding.patientNameText.text.toString()
            val patientWeight=binding.patientWeightText.text.toString()
            val patientAge=binding.patientAgeText.text.toString()
            val patientGender=binding.patientGenderText.text.toString()
            val patientPosition=binding.patientPositionText.text.toString()
            val patientRefferedby=binding.patientReferredByText.text.toString()
            val patientExaminedby=binding.patientExaminedByText.text.toString()
            val patientmode=binding.patientModeText.text.toString()
            val patientWaitTime=binding.patientWaitTimeText.text.toString()
            val density=binding.density.text.toString()
            if(patientId.isEmpty()){
                binding.patientIdContainer.error="Patient id should not be empty."
                binding.patientIdContainer.errorIconDrawable=null
            }else if(patientName.isEmpty()){
                binding.patientNameContainer.error="Patient name should not be empty."
                binding.patientNameContainer.errorIconDrawable=null
            }else if(patientWeight.isEmpty()){
                binding.patientWeightContainer.error="Patient weight should not be empty."
                binding.patientWeightContainer.errorIconDrawable=null
            }else if(patientAge.isEmpty()){
                binding.patientAgeContainer.error="Patient age should not be empty."
                binding.patientAgeContainer.errorIconDrawable=null
            }else if(patientGender.equals("Select Gender")){
                binding.patientGenderContainer.error="Please select the gender."
                binding.patientGenderContainer.errorIconDrawable=null
            }else if(patientPosition.equals("Select Position")){
                binding.patientPositionContainer.error="Please select the position."
                binding.patientPositionContainer.errorIconDrawable=null
            }else if(patientmode.equals("Select Test Mode")){
                binding.patientModeContainer.error="Please select the mode."
                binding.patientModeContainer.errorIconDrawable=null
            }else if(patientWaitTime.equals("Select Wait Time")){
                binding.patientWaitTimeContainer.error="Please select wait time."
                binding.patientWaitTimeContainer.errorIconDrawable=null
            } else if(patientRefferedby.isEmpty()){
               binding.patientReferredByContainer.error="Referred by should not be empty. "
               binding.patientReferredByContainer.errorIconDrawable=null
            }else if(patientExaminedby.isEmpty()){
              binding.patientExaminedByContainer.error="Exmained by should not be empty."
              binding.patientExaminedByContainer.errorIconDrawable=null
            }
            else {
                if (binding.densitySwitch.isChecked) {
                    if (density.isEmpty()) {
                        binding.densityContainer.error = "Density should not empty."
                        binding.densityContainer.errorIconDrawable=null
                    } else {
                        if (sessionManager.connectionStatus() == 1) {
                            val patPosition = positionAdapter.getPosition(patientPosition)
                            val testMode = testModeAdapter.getPosition(patientmode)
                            val waittime = waitTimeAdapter.getPosition(patientWaitTime)

                            var testId =
                                if (uroTestViewModel.getTestId(patientId) == null) 1 else uroTestViewModel.getTestId(
                                    patientId
                                )!! + 1
                            val uroTestModel = UroTestModel(
                                0,
                                testId,
                                patientId,
                                patPosition,
                                waittime,
                                testMode,
                                System.currentTimeMillis(),
                                0,
                            )
                            val patientdetails = PatientDetails(
                                0,
                                patientId,
                                patientName,
                                patientWeight.toFloat(),
                                patientAge.toInt(),
                                patientGender,
                                patientRefferedby,
                                patientExaminedby,
                                patPosition,
                                waittime,
                                testMode,
                                System.currentTimeMillis()
                            )
                            if (isNew) {
                                patientViewModel.insert(patientdetails)
                                //uroTestViewModel.insert(uroTestModel)
                            } else {
                                patientViewModel.updatePatientdetails(patientId,
                                    patientName,
                                    patientWeight.toFloat(),
                                    patientAge.toInt(),
                                    patientGender,
                                    patientRefferedby,
                                    patientExaminedby,
                                    patPosition,
                                    waittime,
                                    testMode,
                                    System.currentTimeMillis())
                                //  uroTestViewModel.insert(uroTestModel)
                            }
                            val flofragment = FlowFragment()
                            val bundle = Bundle()
                            bundle.putSerializable(resources.getString(R.string.key_test_details),uroTestModel)
                            bundle.putString(getString(R.string.key_pat_name),patientName)
                            bundle.putFloat(getString(R.string.key_density),density.toFloat())
                            flofragment.arguments = bundle
                            activity?.supportFragmentManager?.beginTransaction()
                                ?.replace(R.id.main_content, flofragment)?.addToBackStack("Flow")?.commit()
                        }else{
                            Toast.makeText(context, getString(R.string.device_connection_error), Toast.LENGTH_SHORT).show();
                            val tabContainerFragment = TabContainerFragment()
                            val bundle = Bundle()
                            bundle.putInt(resources.getString(R.string.key_tab_frag_change),1)
                            tabContainerFragment.arguments = bundle
                            activity?.supportFragmentManager?.beginTransaction()
                                ?.replace(R.id.main_content, tabContainerFragment)?.commit()
                        }
                    }

                } else {
                    if (sessionManager.connectionStatus() == 1) {
                        val patPosition = positionAdapter.getPosition(patientPosition)
                        val testMode = testModeAdapter.getPosition(patientmode)
                        val waittime = waitTimeAdapter.getPosition(patientWaitTime)

                        var testId =
                            if (uroTestViewModel.getTestId(patientId) == null) 1 else uroTestViewModel.getTestId(
                                patientId
                            )!! + 1
                        val uroTestModel = UroTestModel(
                            0,
                            testId,
                            patientId,
                            patPosition,
                            waittime,
                            testMode,
                            System.currentTimeMillis(),
                            0,
                        )
                        val patientdetails = PatientDetails(
                            0,
                            patientId,
                            patientName,
                            patientWeight.toFloat(),
                            patientAge.toInt(),
                            patientGender,
                            patientRefferedby,
                            patientExaminedby,
                            patPosition,
                            waittime,
                            testMode,
                            System.currentTimeMillis()
                        )
                        if (isNew) {
                            patientViewModel.insert(patientdetails)
                            //uroTestViewModel.insert(uroTestModel)
                        } else {
                            patientViewModel.updatePatientdetails(
                                patientId,
                                patientName,
                                patientWeight.toFloat(),
                                patientAge.toInt(),
                                patientGender,
                                patientRefferedby,
                                patientExaminedby,
                                patPosition,
                                waittime,
                                testMode,
                                System.currentTimeMillis()
                            )
                            //  uroTestViewModel.insert(uroTestModel)
                        }
                        val flofragment = FlowFragment()
                        val bundle = Bundle()
                        bundle.putSerializable(
                            resources.getString(R.string.key_test_details),
                            uroTestModel
                        )
                        bundle.putString(getString(R.string.key_pat_name), patientName)
                        bundle.putFloat(getString(R.string.key_density),0F)
                        flofragment.arguments = bundle
                        activity?.supportFragmentManager?.beginTransaction()
                            ?.replace(R.id.main_content, flofragment)?.addToBackStack("Flow")
                            ?.commit()
                    } else {
                        Toast.makeText(
                            context,
                            getString(R.string.device_connection_error),
                            Toast.LENGTH_SHORT
                        ).show();
                        val tabContainerFragment = TabContainerFragment()
                        val bundle = Bundle()
                        bundle.putInt(resources.getString(R.string.key_tab_frag_change), 1)
                        tabContainerFragment.arguments = bundle
                        clickListener.redirectFragment(tabContainerFragment)
                      /*  activity?.supportFragmentManager?.beginTransaction()
                            ?.replace(R.id.main_content, tabContainerFragment)?.commit()*/
                    }
                }
            }
            }


        binding.patientIdText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!s!!.isEmpty()) {
                    binding.patientIdContainer.error = null
                }
            }
        })
        binding.patientNameText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!s!!.isEmpty()) {
                    binding.patientNameContainer.error = null
                }
            }
        })

        binding.patientIdText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!s!!.isEmpty()) {
                    binding.patientIdContainer.error = null
                }
            }
        })

        binding.patientWeightText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!s!!.isEmpty()) {
                    binding.patientWeightContainer.error = null
                }
            }
        })

        binding.patientAgeText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!s!!.isEmpty()) {
                    binding.patientAgeContainer.error = null
                }
            }
        })

        binding.density.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!s!!.isEmpty()) {
                    binding.densityContainer.error = null
                }
            }
        })

        binding.patientReferredByText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!s!!.isEmpty()) {
                    binding.patientReferredByContainer.error = null
                }
            }
        })
        binding.patientExaminedByText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!s!!.isEmpty()) {
                    binding.patientExaminedByContainer.error = null
                }
            }
        })

        binding.patientWaitTimeText.setOnItemClickListener { adapterView, view, i, l ->
           if(i==0){
               binding.patientWaitTimeContainer.error="Please select wait time."
               binding.patientWaitTimeContainer.errorIconDrawable=null
           }else{
               binding.patientWaitTimeContainer.error=null
           }
        }

        binding.patientGenderText.setOnItemClickListener { adapterView, view, i, l ->
            if(i==0){
                binding.patientGenderContainer.error="Please select the gender."
                binding.patientGenderContainer.errorIconDrawable=null
            }else{
                binding.patientGenderContainer.error=null
            }
            hideSoftKeyboard(binding.patientGenderText)
        }
        binding.patientPositionText.setOnItemClickListener { adapterView, view, i, l ->
            if(i==0){
                binding.patientPositionContainer.error="Please select the position."
                binding.patientPositionContainer.errorIconDrawable=null
            }else{
                binding.patientPositionContainer.error=null
            }
        }
        binding.patientModeText.setOnItemClickListener { adapterView, view, i, l ->
           if(i==0){
               binding.patientModeContainer.error="Please select the mode."
               binding.patientModeContainer.errorIconDrawable=null
           }else{
               binding.patientModeContainer.error=null
           }
        }

        binding.btnCancel.setOnClickListener {
            binding.patientIdText.setText("")
            binding.patientNameText.setText("")
            binding.patientWeightText.setText("")
            binding.patientAgeText.setText("")
            binding.patientExaminedByText.setText("")
            binding.patientReferredByText.setText("")
            binding.patientPositionText.setText(positionAdapter.getItem(0), false)
            binding.patientModeText.setText(testModeAdapter.getItem(0), false)
            binding.patientGenderText.setText(genderAdapter.getItem(0), false)
            binding.patientWaitTimeText.setText(waitTimeAdapter.getItem(0), false)
        }
        // Inflate the layout for this fragment
        return binding.root

    }
    private fun hideSoftKeyboard(view: View) {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        clickListener = context as HomeInterface
    }

}