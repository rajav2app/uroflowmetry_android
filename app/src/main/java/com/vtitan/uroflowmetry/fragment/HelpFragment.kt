package com.vtitan.uroflowmetry.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView
import com.example.uroflowmetryapp.util.ExpandableAdapter
import com.vtitan.uroflowmetry.R
import com.vtitan.uroflowmetry.databinding.FragmentHelpBinding

class HelpFragment : Fragment() {
    private lateinit var binding: FragmentHelpBinding
    private lateinit var header1 : MutableList<String>
    private lateinit var body1 : HashMap<String,List<String>>
    private lateinit var header2 : MutableList<String>
    private lateinit var body2 : HashMap<String,List<String>>
    private lateinit var header3 : MutableList<String>
    private lateinit var body3 : HashMap<String,List<String>>
    private lateinit var header4 : MutableList<String>
    private lateinit var body4 : HashMap<String,List<String>>




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHelpBinding.inflate(layoutInflater)
        binding.scrollViewView.isNestedScrollingEnabled = true

        provideContentOfList1()
        provideContentOfList2()
        provideContentOfList3()
        provideContentOfList4()

        val listViewAdapter1 = ExpandableAdapter(requireContext(),header1, body1)
        val listViewAdapter2 = ExpandableAdapter(requireContext(),header2, body2)
        val listViewAdapter3 = ExpandableAdapter(requireContext(),header3, body3)
        val listViewAdapter4 = ExpandableAdapter(requireContext(),header4, body4)

        binding.expandableHelpView1.setAdapter(listViewAdapter1)
        binding.expandableHelpView2.setAdapter(listViewAdapter2)
        binding.expandableHelpView3.setAdapter(listViewAdapter3)
        binding.expandableHelpView4.setAdapter(listViewAdapter4)

        binding.expandableHelpView1.setOnGroupClickListener(ExpandableListView.OnGroupClickListener { parent, v, groupPosition, id ->
            setListViewHeight(parent, groupPosition)
            false
        })
        binding.expandableHelpView2.setOnGroupClickListener(ExpandableListView.OnGroupClickListener { parent, v, groupPosition, id ->
            setListViewHeight(parent, groupPosition)
            false
        })
        binding.expandableHelpView3.setOnGroupClickListener(ExpandableListView.OnGroupClickListener { parent, v, groupPosition, id ->
            setListViewHeight(parent, groupPosition)
            false
        })
        binding.expandableHelpView4.setOnGroupClickListener(ExpandableListView.OnGroupClickListener { parent, v, groupPosition, id ->
            setListViewHeight(parent, groupPosition)
            false
        })

        return binding.root
    }

    private fun provideContentOfList1() {
        header1 = ArrayList()
        body1 = HashMap()

        val bodyContent1 =
            "1 Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        val bodyContent2 =
            "2 Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."

        (header1 as ArrayList<String>).add("Device Connection")
        (header1 as ArrayList<String>).add("Test Procedure")

        body1[header1[0]] = listOf(bodyContent1)
        body1[header1[1]] = listOf(bodyContent2)

    }
    private fun provideContentOfList2() {
        header2 = ArrayList()
        body2 = HashMap()

        val bodyContent3 =
            "3 Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        val bodyContent4 =
            "4 Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        val bodyContent5 =
            "5 Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        val bodyContent6 =
            "6 Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."

        (header2 as ArrayList<String>).add("How to connect the device?")
        (header2 as ArrayList<String>).add("How to add new patient?")
        (header2 as ArrayList<String>).add("How to download result?")
        (header2 as ArrayList<String>).add("How to generate report?")

        body2[header2[0]] = listOf(bodyContent3)
        body2[header2[1]] = listOf(bodyContent4)
        body2[header2[2]] = listOf(bodyContent5)
        body2[header2[3]] = listOf(bodyContent6)

    }
    private fun provideContentOfList3() {
        header3 = ArrayList()
        body3 = HashMap()

        val bodyContent7 =
            "7 Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        val bodyContent8 =
            "8 Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."



        (header3 as ArrayList<String>).add("Instructions For Use")
        (header3 as ArrayList<String>).add("Brochure")



        body3[header3[0]] = listOf(bodyContent7)
        body3[header3[1]] = listOf(bodyContent8)
    }
    private fun provideContentOfList4() {
        header4 = ArrayList()
        body4 = HashMap()

        val bodyContent9 =
            "9 Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."

        (header4 as ArrayList<String>).add("Mail/Contanct number")

        body4[header4[0]] = listOf(bodyContent9)
    }

    private fun setListViewHeight(
        listView: ExpandableListView,
        group: Int
    ) {
        val listAdapter = listView.expandableListAdapter as ExpandableListAdapter
        var totalHeight = 0
        val desiredWidth = View.MeasureSpec.makeMeasureSpec(
            listView.width,
            View.MeasureSpec.EXACTLY
        )
        for (i in 0 until listAdapter.groupCount) {
            val groupItem = listAdapter.getGroupView(i, false, null, listView)
            groupItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
            totalHeight += groupItem.measuredHeight
            if (listView.isGroupExpanded(i) && i != group
                || !listView.isGroupExpanded(i) && i == group
            ) {
                for (j in 0 until listAdapter.getChildrenCount(i)) {
                    val listItem = listAdapter.getChildView(
                        i, j, false, null,
                        listView
                    )
                    listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
                    totalHeight += listItem.measuredHeight
                }
            }
        }
        val params = listView.layoutParams
        var height = (totalHeight
                + listView.dividerHeight * (listAdapter.groupCount - 1))
        if (height < 10) height = 200
        params.height = height
        listView.layoutParams = params
        listView.requestLayout()
    }

}