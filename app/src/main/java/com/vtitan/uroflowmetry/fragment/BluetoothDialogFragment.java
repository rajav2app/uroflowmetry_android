package com.vtitan.uroflowmetry.fragment;

import static android.content.ContentValues.TAG;
import static android.content.Context.BIND_AUTO_CREATE;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vtitan.uroflowmetry.R;
import com.vtitan.uroflowmetry.adapter.BleDeviceNameListAdapter;
import com.vtitan.uroflowmetry.model.BleDeviceListModel;
import com.vtitan.uroflowmetry.repository.DeviceInfoRepository;
import com.vtitan.uroflowmetry.services.BluetoothLeServiceKt;
import com.vtitan.uroflowmetry.viewmodel.DeviceInfoViewModel;
import com.vtitan.uroflowmetry.viewmodel.DeviceInfoViewModelFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class BluetoothDialogFragment extends Fragment implements BleDeviceNameListAdapter.DCListItemListener {

    private static final long SCAN_PERIOD = 1000;

    private static final long DEVICE_DETECTION_PERID = 15000;
    private static final String MAC_PREFIX="48:23:35";

    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothLeScanner mBluetoothLeScanner;

    private ScanCallback mScanCallback;

    private Handler mHandler;

    private RecyclerView rvDeviceList;

    private HashMap<String, String> namevale = new HashMap<>();

    private RelativeLayout customProgress;

    private String mDeviceAddress;
    private BluetoothLeServiceKt mBluetoothLeService;
    BleDeviceNameListAdapter bleDeviceNameListAdapter = null;
    private BleDeviceListModel bleDeviceListModel=null;
    private List<BleDeviceListModel>bleDeviceListModelList=new ArrayList<>();
    private DeviceInfoViewModelFactory deviceInfoViewModelFactory;
    private DeviceInfoViewModel deviceInfoViewModel;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setBluetoothAdapter(BluetoothAdapter btAdapter) {
        this.mBluetoothAdapter = btAdapter;
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    public BluetoothDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mHandler = new Handler();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_bluetooth_dialog, container, false);
        deviceInfoViewModelFactory=new DeviceInfoViewModelFactory(getActivity().getApplication());
        deviceInfoViewModel=new DeviceInfoViewModel(getActivity().getApplication());
        rvDeviceList = root.findViewById(R.id.rvDeviceList);
        customProgress = root.findViewById(R.id.customProgress);
        rvDeviceList.setLayoutManager(new LinearLayoutManager(getContext()));

        deviceInfoViewModel.getAllDeviceList().observe(getViewLifecycleOwner(), new Observer<List<BleDeviceListModel>>() {
            @Override
            public void onChanged(List<BleDeviceListModel> bleDeviceListModels) {
                if(bleDeviceListModels!=null && !bleDeviceListModels.isEmpty()){
                    bleDeviceNameListAdapter = new BleDeviceNameListAdapter(bleDeviceListModels, getContext(), BluetoothDialogFragment.this, BluetoothDialogFragment.this);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                    rvDeviceList.setLayoutManager(layoutManager);
                    rvDeviceList.setAdapter(bleDeviceNameListAdapter);
                    bleDeviceNameListAdapter.notifyDataSetChanged();
                   // bleDeviceListModelList.clear();
                }
            }
        });

        final ImageButton imgrefresh=root.findViewById(R.id.imgrefresh);
        imgrefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScanning();
                customProgress.setVisibility(View.VISIBLE);
            }
        });
        return root;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Trigger refresh on app's 1st load
        startScanning();
        customProgress.setVisibility(View.VISIBLE);
        //registerDeviceConnectionReceiver();

    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void startScanning() {
        if (mScanCallback == null) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startScanning();

                }
            }, SCAN_PERIOD);

            // Kick off a new scan.
            mScanCallback = new SampleScanCallback();
            mBluetoothLeScanner.startScan(buildScanFilters(), buildScanSettings(), mScanCallback);
        } else {
            //  Toast.makeText(getContext(),"Already Scanning" /*getActivity().getString(R.string.already_scanning)*/, Toast.LENGTH_SHORT);
        }
    }

    /**
     * Return a List of {@link ScanFilter} objects to filter by Service UUID.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<>();
        ScanFilter.Builder builder = new ScanFilter.Builder();
        scanFilters.add(builder.build());
        return scanFilters;
    }

    /**
     * Return a {@link ScanSettings} object set to use low power (to preserve battery life).
     */

    @RequiresApi(api = Build.VERSION_CODES.M)
    private ScanSettings buildScanSettings() {
        return new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
                .setReportDelay(0L)
                .build();
    }



    /**
     * Custom ScanCallback object - adds to adapter on success, displays error on failure.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class SampleScanCallback extends ScanCallback {

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results) {
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            Log.d("textresult", result.getDevice().getName() + "   " + result.toString());
            //result.getScanRecord().
            BluetoothDevice device = result.getDevice();
            if (device.getAddress().startsWith(MAC_PREFIX)) {
                customProgress.setVisibility(View.GONE);
               // namevale.put(result.getDevice().getAddress(),result.getDevice().getName());
               // bleDeviceListModelList.clear();
                bleDeviceListModel=new BleDeviceListModel(result.getDevice().getAddress(),result.getDevice().getName(),0);
                bleDeviceListModelList.add(bleDeviceListModel);
                if(!bleDeviceListModelList.isEmpty()) {
                    final List<BleDeviceListModel> oldList = deviceInfoViewModel.getAllDevice();

                    List<String> newArrayList;
                    List<String> oldArrayList;
                    newArrayList = bleDeviceListModelList.stream().map(BleDeviceListModel::getDevId).collect(Collectors.toList());
                    oldArrayList = oldList.stream().map(BleDeviceListModel::getDevId).collect(Collectors.toList());

                    List<String> insert = new ArrayList<>(newArrayList);
                    List<String> update = new ArrayList<>(newArrayList);
                    List<String> delete = new ArrayList<>(oldArrayList);

                    List<BleDeviceListModel> insertlist = new ArrayList<>();
                    List<BleDeviceListModel> updateList = new ArrayList<>();


                    insert.removeAll(oldArrayList);
                    update.retainAll(oldArrayList);
                    delete.removeAll(newArrayList);

                    insertlist = getInsertList(insert, bleDeviceListModelList);
                    updateList = getUpdateList(update, bleDeviceListModelList);

                    deviceInfoViewModel.deviceSQLOperation(insertlist, updateList, delete);

                }

                /*if (namevale.size() > 0) {
                    bleDeviceNameListAdapter = new BleDeviceNameListAdapter(namevale, getContext(), BluetoothDialogFragment.this, BluetoothDialogFragment.this);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                    rvDeviceList.setLayoutManager(layoutManager);
                    rvDeviceList.setAdapter(bleDeviceNameListAdapter);
                    bleDeviceNameListAdapter.notifyDataSetChanged();
                } else {
                    rvDeviceList.setVisibility(View.GONE);
                }*/
                // bleDeviceNameListAdapter.setListener((BleDeviceNameListAdapter.DCListItemListener)getActivity());
                stopScanning();
                //stopScanning();
            } else {

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Toast.makeText(getContext(), R.string.text_device_not_found, Toast.LENGTH_SHORT).show();
                        customProgress.setVisibility(View.GONE);
                        stopScanning();
                        //BluetoothDialogFragment.this.dismiss();
                    }
                }, DEVICE_DETECTION_PERID);

            }


        }

        @SuppressLint("MissingPermission")
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            mBluetoothAdapter.disable();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    customProgress.setVisibility(View.GONE);
                    stopScanning();
                    // BluetoothDialogFragment.this.dismiss();
                }
            }, DEVICE_DETECTION_PERID);

            Toast.makeText(getActivity(), "Scan failed with error: " + errorCode, Toast.LENGTH_LONG)
                    .show();
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void stopScanning() {
        // Stop the scan, wipe the callback.
        if (mScanCallback != null) {
            mBluetoothLeScanner.stopScan(mScanCallback);
            mScanCallback = null;
        }

    }

    @Override
    public void onItemClick(View v, String mac_id) {
        Log.i("TAG", "onItemClick: " + mac_id);
        if(mac_id!=null) {
            mDeviceAddress=mac_id;
            //Log.i(TAG, "GETSUCESS "+mac_id);
            Intent gattServiceIntent = new Intent(getContext(), BluetoothLeServiceKt.class);
            gattServiceIntent.putExtra(getString(R.string.key_dev_mac), mac_id);
                if (getContext().bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE)) {
                    Log.i(TAG, "GETSUCCESS bindservice :: success ");
                    v.setEnabled(false);
                    //Toast.makeText(getContext(), "Device Connected successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    v.setEnabled(true);
                  //  Toast.makeText(getContext(), "Device Connection failed.", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "GETSUCCESS bindservice :: failed ");
                }
          bleDeviceNameListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void OnItemDiconnectClick(View v, String mac_id) {
        if(mBluetoothLeService!=null){
            v.setEnabled(false);
            mBluetoothLeService.disconnect();
        }

    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            //mBLEService = new Messenger(service);
            mBluetoothLeService = ((BluetoothLeServiceKt.LocalBinder) service).getServiceKt();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");

            }
            // Automatically connects to the device upon successful start-up initialization.
            boolean b = mBluetoothLeService.connect(mDeviceAddress);
            if(b){
                Log.i(TAG, " initialize Bluetooth and connection success ");
            }else{
                Log.i(TAG, " initialize Bluetooth and connection failed ");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
           // mBLEService = null;
        }
    };

    @Override
    public void onResume() {
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
        super.onResume();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onPause() {
        if (mScanCallback != null) {
            mBluetoothLeScanner.stopScan(mScanCallback);
            mScanCallback = null;
        }
        stopScanning();
        super.onPause();
    }
    @SuppressLint("MissingPermission")
    @Override
    public void onDestroy() {
        if (mScanCallback != null) {
            mBluetoothLeScanner.stopScan(mScanCallback);
            mScanCallback = null;
        }
        if (mBluetoothLeService != null) {
            Intent gattServiceIntent = new Intent(getContext(), BluetoothLeServiceKt.class);
            mBluetoothLeService.stopService(gattServiceIntent);
            mBluetoothLeService=null;
        }
        stopScanning();
        super.onDestroy();
    }

    private List<BleDeviceListModel> getUpdateList(List<String> update, List<BleDeviceListModel> dcList) {
        List<BleDeviceListModel> arraylist = new ArrayList<>();
        for (BleDeviceListModel dropCounter : dcList) {
            for (String str : update) {
                if (str.equals(dropCounter.getDevId())){
                    arraylist.add(dropCounter);
                }
            }
        }
        return arraylist;
    }

    private List<BleDeviceListModel> getInsertList(List<String> insert, List<BleDeviceListModel> dcList) {
        List<BleDeviceListModel> arraylist = new ArrayList<>();
        for (BleDeviceListModel dropCounter : dcList) {
            for (String str : insert) {
                if (str.equals(dropCounter.getDevId())){
                    arraylist.add(dropCounter);
                }
            }
        }
        return arraylist;
    }

}