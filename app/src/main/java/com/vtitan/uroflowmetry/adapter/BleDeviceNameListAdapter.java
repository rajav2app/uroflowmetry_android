package com.vtitan.uroflowmetry.adapter;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.vtitan.uroflowmetry.R;
import com.vtitan.uroflowmetry.fragment.BluetoothDialogFragment;
import com.vtitan.uroflowmetry.model.BleDeviceListModel;
import com.vtitan.uroflowmetry.model.DeviceListModel;
import com.vtitan.uroflowmetry.util.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BleDeviceNameListAdapter extends RecyclerView.Adapter<BleDeviceNameListAdapter.MyViewHolder> {

    Context context;
    //HashMap<String, String> deviceNameList;
    List<BleDeviceListModel>deviceNameList;
    List<DeviceListModel> deviceName = new ArrayList<>();
    BluetoothDialogFragment dialogFragment;
    private SessionManager sessionManager;
    private DCListItemListener listener;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView tvDeviceName;
        private LinearLayout llDeviceName;
        private TextView tvMacId;
        private Button btn_connect;
        private Button btn_dis_connect;
        private TextView tvConnectionStatus;
        private boolean isConnected=false;
        public MyViewHolder(View view) {
            super(view);

            tvDeviceName = view.findViewById(R.id.tvname);
            llDeviceName = view.findViewById(R.id.llDeviceName);
            tvMacId = view.findViewById(R.id.tvMacId);
            btn_connect = view.findViewById(R.id.btn_connect);
            btn_dis_connect=view.findViewById(R.id.btn_dis_connect);
            tvConnectionStatus=view.findViewById(R.id.tvConnectionStatus);
        }
    }

    public BleDeviceNameListAdapter(List<BleDeviceListModel>deviceNameList/*HashMap<String, String> deviceNameList*/, Context context, BluetoothDialogFragment dialogFragment, DCListItemListener listener) {
        this.deviceNameList = deviceNameList;
        this.context = context;
        this.dialogFragment = dialogFragment;
        this.listener = listener;
    }

    //    public void setListener(DCListItemListener listener){
//        this.listener = listener;
//    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_name_list, parent, false);
        sessionManager = new SessionManager(context);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

       /* for (Map.Entry<String, String> name : deviceNameList.entrySet()) {
            DeviceListModel deviceListModel = new DeviceListModel();
            deviceListModel.setDev_name(name.getValue());
            deviceListModel.setMac_id(name.getKey());
            deviceName.add(deviceListModel);
        }*/
        holder.tvDeviceName.setText(deviceNameList.get(position).getDevName()/*deviceName.get(position).getDev_name()*/);
        holder.tvMacId.setText(deviceNameList.get(position).getDevId()/*deviceName.get(position).getMac_id()*/);
       if(deviceNameList.get(position).getConnection_status() == 0){
           holder.tvConnectionStatus.setText(context.getString(R.string.not_connected));
           holder.btn_connect.setVisibility(View.VISIBLE);
           holder.btn_dis_connect.setVisibility(View.GONE);
       }else{
           holder.tvConnectionStatus.setText(context.getString(R.string.connected));
           holder.btn_connect.setVisibility(View.GONE);
           holder.btn_dis_connect.setVisibility(View.VISIBLE);
       }

        /*if(sessionManager.getMacId()!=null && !sessionManager.getMacId().isEmpty()) {
            if (sessionManager.getMacId().equals(deviceName.get(position).getMac_id())) {
                holder.btn_connect.setVisibility(View.GONE);
                holder.btn_dis_connect.setVisibility(View.VISIBLE);
                holder.tvConnectionStatus.setText(context.getString(R.string.connected));
            } else {
                holder.btn_connect.setVisibility(View.VISIBLE);
                holder.btn_dis_connect.setVisibility(View.GONE);
                holder.tvConnectionStatus.setText(context.getString(R.string.not_connected));
            }
        }else{
            holder.btn_connect.setVisibility(View.VISIBLE);
            holder.btn_dis_connect.setVisibility(View.GONE);
            holder.tvConnectionStatus.setText(context.getString(R.string.not_connected));
        }*/

        holder.btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mac_id = deviceNameList.get(position).getDevId();
                sessionManager.savemacId(mac_id);
                sessionManager.saveDeviceName(deviceNameList.get(position).getDevName());
                listener.onItemClick(v, mac_id);
               // Log.i("CLICK_CHECK", mac_id);

            }
        });
        holder.btn_dis_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mac_id = deviceName.get(position).getMac_id();
                listener.OnItemDiconnectClick(view,mac_id);
            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceNameList.size();
    }

    public interface DCListItemListener {
        void onItemClick(View v, String mac_id);
        void OnItemDiconnectClick(View v,String mac_id);
    }

}