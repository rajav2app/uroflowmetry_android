package com.example.uroflowmetryapp.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.vtitan.uroflowmetry.R
import com.vtitan.uroflowmetry.databinding.FragmentHistoryBinding
import com.vtitan.uroflowmetry.viewmodel.*
import java.time.Month
import java.util.*
import kotlin.properties.Delegates

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding


    private lateinit var patientViewModel: PatientViewModel
    private lateinit var patientViewModelFactory: PatientViewModelFactory
    private lateinit var uroInfoViewModel: UroInfoViewModel
    private lateinit var uroInfoViewModelFactory: UroInfoViewModelFactory
    private lateinit var uroTestInfoViewModel: UroTestInfoViewModel
    private lateinit var uroTestInfoViewModelFactory: UroTestInfoViewModelFactory



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHistoryBinding.inflate(layoutInflater)

        val application = requireNotNull(this.activity).application
        patientViewModelFactory = PatientViewModelFactory(application)
        uroInfoViewModelFactory = UroInfoViewModelFactory(application)
        uroTestInfoViewModelFactory = UroTestInfoViewModelFactory(application)
        patientViewModel = ViewModelProvider(this,patientViewModelFactory).get(PatientViewModel::class.java)
        uroInfoViewModel = ViewModelProvider(this,uroInfoViewModelFactory).get(UroInfoViewModel::class.java)
        uroTestInfoViewModel = ViewModelProvider(this,uroTestInfoViewModelFactory).get(UroTestInfoViewModel::class.java)

        val dateSearch = ArrayAdapter(requireContext(), R.layout.spinner_item, resources.getStringArray(R.array.search_date_list))
        binding.searchDate.setAdapter(dateSearch)
        binding.searchDate.setText(dateSearch.getItem(0), false)
        binding.searchDate.isEnabled=false

        val patientList=patientViewModel.getPatientIds()
        val patientIdAdapter = ArrayAdapter(requireContext(),
            R.layout.spinner_item, patientList)
        binding.patientSearchText.setAdapter(patientIdAdapter)
        binding.patientSearchContainer.setEndIconOnClickListener {
            if (binding.patientSearchText.text.toString() in patientList) {
                val historyDetailFragment = HistoryDetailFragment()
                val bundle = Bundle()
                bundle.putString(
                    resources.getString(R.string.key_patid),
                    binding.patientSearchText.text.toString()
                )
                bundle.putLong(
                    resources.getString(R.string.key_time),
                    getDateLong(binding.searchDate.text.toString())
                )
                bundle.putString(
                    resources.getString(R.string.key_time_string),
                    binding.searchDate.text.toString()
                )
                historyDetailFragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()
                    ?.replace(R.id.main_content, historyDetailFragment)?.commit()
            }
            else{
                Toast.makeText(requireContext(),"Enter Valid Patient Id",Toast.LENGTH_SHORT).show()
            }
        }



        return binding.root
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
            "Last 6 Month"->{
                time.add(Calendar.MONTH,-6)
            }
        }
        dt = time.timeInMillis
        return dt!!
    }

}
