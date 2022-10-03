package com.example.uroflowmetryapp.util


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.vtitan.uroflowmetry.R


class ExpandableAdapter(private val context: Context,private val header:List<String>,private val body:HashMap<String,List<String>>):
    BaseExpandableListAdapter() {
    override fun getGroupCount(): Int {
        return header.size
    }

    override fun getChildrenCount(groundPosition: Int): Int {
        return this.body[this.header[groundPosition]]!!.size
    }

    override fun getGroup(groupPosition: Int): Any {
        return header[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return this.body[this.header[groupPosition]]!![childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean,  convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        val headerTitle = getGroup(groupPosition) as String
        if (convertView == null){
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.layout_group_header,null)
        }
        val headerTv = convertView!!.findViewById<TextView>(R.id.ex_head)
        headerTv.setText(headerTitle)
        return convertView!!
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        val bodyTitle = getChild(groupPosition, childPosition) as String
        if (convertView == null){
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.layout_group_body,null)
        }
        val bodyTv = convertView!!.findViewById<TextView>(R.id.ex_body)
        bodyTv.setText(bodyTitle)
        return convertView!!
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return false
    }

}