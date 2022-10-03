package com.vtitan.uroflowmetry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vtitan.uroflowmetry.R
import com.vtitan.uroflowmetry.model.UroInfoModel
import com.vtitan.uroflowmetry.model.relations.UroTestWithUroInfo
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class HistoryRecyclerAdapter(private val items:List<UroTestWithUroInfo>):RecyclerView.Adapter<HistoryRecyclerAdapter.HistoryViewHolder>() {
    private lateinit var clicklistener :HistoryItemClickListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context).inflate(R.layout.history_detail_list,parent,false)
        return HistoryViewHolder(layoutInflater,clicklistener)
    }
    fun setListener(listener:HistoryItemClickListener){
        this.clicklistener=listener
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]
        holder.recyclerTime.text = longToTime(item.uroTestModel.startTime.toString().toLong())
        holder.recyclerFlow.text = getFlow(item.uroInfoModel)
        holder.recyclerVolume.text = getVolume(item.uroInfoModel)
        holder.llHistory.setOnClickListener {
            if(clicklistener!=null) {
                clicklistener.onHistoryItemClick(it,item)
            }
        }

    }

    override fun getItemCount(): Int {
        return items.size
    }


    class HistoryViewHolder(itemView: View,listener: HistoryItemClickListener):RecyclerView.ViewHolder(itemView) {
        val recyclerTime = itemView.findViewById<TextView>(R.id.recycler_time)
        val recyclerFlow = itemView.findViewById<TextView>(R.id.recycler_flow_rate)
        val recyclerVolume = itemView.findViewById<TextView>(R.id.recycler_volume)
        val llHistory=itemView.findViewById<LinearLayout>(R.id.llHistory)
    }
    fun getVolume(temp :List<UroInfoModel>):String{
        val vol :MutableList<Double> = mutableListOf()
        for (i in temp){
            vol.add(i.volume_info!!)
        }
        val max = vol.maxOrNull()
        return String.format("%.2f", max)
    }
    fun getFlow(temp :List<UroInfoModel>):String{
        val flow :MutableList<Double> = mutableListOf()
        for (i in temp){
            flow.add(i.flow_rate!!)
        }
        val max = flow.maxOrNull()
        return String.format("%.2f", max)
    }
    fun longToTime(num:Long):String{
        val simple: DateFormat = SimpleDateFormat("dd MMM yyyy HH:mm:ss aa")
        val result = Date(num)
        return simple.format(result)
    }
     interface HistoryItemClickListener{
        fun onHistoryItemClick(v: View?, uroInfo: UroTestWithUroInfo)
    }


}