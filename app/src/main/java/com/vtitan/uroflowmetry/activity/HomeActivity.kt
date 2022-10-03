package com.vtitan.uroflowmetry.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.example.uroflowmetryapp.fragment.*
import com.google.android.material.snackbar.Snackbar
import com.vtitan.uroflowmetry.R
import com.vtitan.uroflowmetry.app.UroFlowApplication
import com.vtitan.uroflowmetry.databinding.ActivityHomeBinding
import com.vtitan.uroflowmetry.databinding.ContentHomeActivityBinding
import com.vtitan.uroflowmetry.fragment.*
import com.vtitan.uroflowmetry.services.BluetoothLeServiceKt
import com.vtitan.uroflowmetry.util.Constants
import com.vtitan.uroflowmetry.util.SessionManager
import com.vtitan.uroflowmetry.viewmodel.*


class HomeActivity : AppCompatActivity(),TestDetailFragment.MenuBackChangeListener,HomeFragment.HomeInterface {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var menuBinding: ContentHomeActivityBinding
    private lateinit var deviceConnectionReceiver: BroadcastReceiver
    private lateinit var sessionManager: SessionManager
    private lateinit var snackbar :Snackbar
    private lateinit var menuItem: MenuItem

    private lateinit var testStatusReciver : BroadcastReceiver
    private var isTestStarted =false
    private var isFirstCalibration=true
    private var isFirstCalibrationSuccess=true
    private var testId = 0L
    private lateinit var uroinfoViewModel : UroInfoViewModel
    private lateinit var uroinfoViewModelProvider : UroInfoViewModelFactory
    private lateinit var uroTestInfoViewModel: UroTestInfoViewModel
    private lateinit var uroTestInfoViewModelFactory: UroTestInfoViewModelFactory
    private lateinit var deviceInfoViewModel: DeviceInfoViewModel
    private lateinit var deviceInfoViewModelFactory: DeviceInfoViewModelFactory
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }
    val isLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

       /*********** Resource Binding binding activity_main.xml menuBinding content_home_activity *********/
        binding = ActivityHomeBinding.inflate(layoutInflater)
        menuBinding = binding.sideMenu
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolBar)
        binding.toolBar.title = getString(R.string.app_name)
        binding.toolBar.setTitleTextColor(resources.getColor(R.color.font_black))

        /***********Init Sesseion Manager Viewmodel and ViewModel factory **********/
        sessionManager = SessionManager(this)
        uroinfoViewModelProvider = UroInfoViewModelFactory(application)
        uroinfoViewModel = ViewModelProvider(this,uroinfoViewModelProvider).get(UroInfoViewModel::class.java)
        uroTestInfoViewModelFactory = UroTestInfoViewModelFactory(application)
        uroTestInfoViewModel = ViewModelProvider(this,uroTestInfoViewModelFactory).get(UroTestInfoViewModel::class.java)
        deviceInfoViewModelFactory = DeviceInfoViewModelFactory(application)
        deviceInfoViewModel = ViewModelProvider(this,deviceInfoViewModelFactory).get(DeviceInfoViewModel::class.java)

        /********************* Requesting location permission and storage permission **********************/
        requestLocationPermissions()
        /******************* If bluetooth is not enabled request the user to enable the bluetooth *****************/
        if (!bluetoothAdapter.isEnabled) {
            enableBluetooth()
        }
        /******************** Register the connection status broad cast receiver ************/
        registerDeviceConnectionReceiver()
        /******************** Register the test status broad cast receiver ************/
        registerTestStatusReceiver()
        /******************** side menu button click events ************/
        menuBinding.homeLlLayout.setOnClickListener {
            if(isTestStarted){
                dialogTestInProgressConfirm(HomeFragment(),it)
            }else {
                backgroundChange(it)
                supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, HomeFragment())?.commit()
            }
        }

        menuBinding.historyLlLayout.setOnClickListener {
            if(isTestStarted){
                dialogTestInProgressConfirm(HistoryFragment(),it)
            }else {
                backgroundChange(it)
               supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, HistoryDetailFragment())?.addToBackStack("History")?.commit()
            }
        }

        menuBinding.deviceSetupLlLayout.setOnClickListener{
            if(isTestStarted){
                dialogTestInProgressConfirm(TabContainerFragment(),it)
            }else {
                backgroundChange(it)
                val tabContainerFragment = TabContainerFragment()
                val bundle = Bundle()
                bundle.putInt(resources.getString(R.string.key_tab_frag_change),1)
                tabContainerFragment.arguments = bundle
                supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, tabContainerFragment)?.commit()
            }
        }

        menuBinding.aboutLlLayout.setOnClickListener {
            if(isTestStarted){
                dialogTestInProgressConfirm(AboutUsFragment(),it)
            }else {
                backgroundChange(it)
              supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, AboutUsFragment())?.addToBackStack("About")?.commit()
            }
        }

        menuBinding.helpLlLayout.setOnClickListener {
            if(isTestStarted){
                dialogTestInProgressConfirm(HelpFragment(),it)
            }else {
                backgroundChange(it)
                supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, HelpFragment())?.addToBackStack("Help")?.commit()
            }
        }

        menuBinding.hospitalSettingLlLayout.setOnClickListener {
            if(isTestStarted){
                dialogTestInProgressConfirm(HospitalSettingsFragment(),it)
            }else {
                backgroundChange(it)
                supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, HospitalSettingsFragment())?.addToBackStack("Hospital")?.commit()
            }
        }

        /************** Theme Selection **************************/
        menuBinding.themeSwitch.setOnCheckedChangeListener { compoundButton, b ->
           if(b){
            sessionManager.savelightmode(true)
              UroFlowApplication().setAppTheme(1)
           }else{
               sessionManager.savelightmode(false)
               UroFlowApplication().setAppTheme(0)
           }
       }
        /******************** By default load the HomeFragment  ************/
        supportFragmentManager.beginTransaction().replace(R.id.main_content, HomeFragment())
            .commit()

    }

    /****************************** Optionmenu initilization and option menu click *******************************/

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        menuItem=menu.get(0)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        /*return when (item.itemId) {
            R.id.scan_device -> {
                false
            }
            else -> super.onOptionsItemSelected(item)
        }*/

        return super.onOptionsItemSelected(item)
    }

    /****************************** This part is for the permissions *******************************/

    private fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun Activity.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }

    private fun requestLocationPermissions() {
        if (isLocationPermissionGranted == true) {
            return
        }
        runOnUiThread {
            val builder = AlertDialog.Builder(this)
            builder.setPositiveButton("Yes") { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.BLUETOOTH_SCAN
                    ),
                    PackageManager.PERMISSION_GRANTED
                )
            }
            builder.setNegativeButton("No") { _, _ ->

            }
            builder.setTitle("Permission Required")
            builder.setMessage(
                "Starting from Android M (6.0), the system requires apps to be granted " +
                        "location access in order to scan for BLE deices."
            )
            builder.setCancelable(false)
            builder.create().show()
        }
    }

    private fun enableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableIntent!!)
        }
    }

    /****************************** BroadCast reciver to update the device connection status *******************************/

    private fun registerDeviceConnectionReceiver(){
        deviceConnectionReceiver= object :BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != null && intent.action == Constants.ACTION_DEVICE_CONNECTION) {
                    val connectionStatus = intent.getIntExtra(Constants.DEVICE_CONNECTED, 0)
                    if (connectionStatus == Constants.CONNECTED) {
                        menuItem.icon = resources.getDrawable(R.drawable.ic_bluetooth_connected)
                        sessionManager.saveConnectionStatus(Constants.CONNECTED)
                        showDeviceStatus(binding.llmain, connectionStatus)
                        binding.toolBar.title=sessionManager.getDeviceName()
                        deviceInfoViewModel.updateConnectionStatus(1,sessionManager.getMacId())
                        //Log.i(ContentValues.TAG, "registerReceiver :: onReceive " + true)
                    } else if (connectionStatus == Constants.NOTCONNECTED) {
                        sessionManager.saveConnectionStatus(Constants.NOTCONNECTED)
                        binding.toolBar.title=resources.getString(R.string.app_name)
                        menuItem.icon = resources.getDrawable(R.drawable.ic_bluetooth_not_connected)
                        showDeviceDisconnected(binding.llmain, getString(R.string.device_connection_failed))
                        deviceInfoViewModel.updateConnectionStatus(0,sessionManager.getMacId())
                    } else if (connectionStatus == Constants.DEVICEDISPLACED) {
                        sessionManager.saveConnectionStatus(Constants.DEVICEDISPLACED)
                        showDeviceDisconnected(binding.llmain, getString(R.string.device_displaced))
                    }else if(connectionStatus == Constants.LOWBATTERY){
                        showDeviceStatus(binding.llmain, connectionStatus)
                    }else if(connectionStatus == Constants.BATTERYALARM){
                        showDeviceStatus(binding.llmain, connectionStatus)
                    }else if(connectionStatus == Constants.NOTCALIBIRATED){
                        if(isFirstCalibration) {
                            showDeviceDisconnected(binding.llmain, getString(R.string.msg_not_calibirated))
                            isFirstCalibration=false
                            redirectCalibrationFragment(TabContainerFragment())

                        }
                    }else if(connectionStatus == Constants.CALIBIRATED){
                        if(isFirstCalibrationSuccess) {
                            showDeviceStatus(binding.llmain, connectionStatus)
                            isFirstCalibrationSuccess=false
                        }
                    }

                }
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(Constants.ACTION_DEVICE_CONNECTION)
        registerReceiver(deviceConnectionReceiver, intentFilter)

    }


    override fun onResume() {
       /* if(!isBluetoothHeadsetConnected()){
            showDeviceDisconnected(binding.llmain)
        }*/
        super.onResume()
    }

    override fun onPause() {
        /*if(deviceConnectionReciver!=null) {
            unregisterReceiver(deviceConnectionReciver)
        }*/
        super.onPause()
    }

    override fun onDestroy() {
        if(deviceConnectionReceiver!=null) {
            unregisterReceiver(deviceConnectionReceiver)
        }
        super.onDestroy()
    }
    /****************************** Custom Snack bar with click listener *******************************/
    @SuppressLint("RestrictedApi")
    fun showDeviceDisconnected(view: View,status: String){
        snackbar = Snackbar.make(view,"", Snackbar.LENGTH_INDEFINITE)
        val customSnackbar = layoutInflater.inflate(R.layout.snackbar_orange_layout,null)
        val snackbarLayout= snackbar.view as Snackbar.SnackbarLayout
        val deviceconnection= customSnackbar.findViewById<TextView>(R.id.device_connection_failed)
        if(status == null || status.equals("")){

        }else{
            deviceconnection.text="${status}"
        }
        snackbarLayout.setPadding(0,0,0,0)
        snackbarLayout.addView(customSnackbar,0)
        snackbarLayout.setOnClickListener(View.OnClickListener { snackbar.dismiss() })
        snackbar.show()
    }

    @SuppressLint("RestrictedApi")
    fun showDeviceStatus(view: View,status: Int){
        val snackbar = Snackbar.make(view,"",Snackbar.LENGTH_LONG)
        val customSnackbar = layoutInflater.inflate(R.layout.snackbar_orange_layout,null)
        val snackbarLayout= snackbar.view as Snackbar.SnackbarLayout
        val llDeviceConnection= customSnackbar.findViewById<LinearLayout>(R.id.llDeviceConnection)
        val deviceconnection= customSnackbar.findViewById<TextView>(R.id.device_connection_failed)
        val imgAlert=customSnackbar.findViewById<ImageView>(R.id.imgAlert)
       if(status == Constants.CONNECTED) {
           llDeviceConnection.background = resources.getDrawable(R.drawable.snackbar_green)
           imgAlert.setImageDrawable(resources.getDrawable(R.drawable.ic_info_light))
           deviceconnection.text = resources.getString(R.string.device_connection_success)
       }else if(status == Constants.LOWBATTERY){
           llDeviceConnection.background = resources.getDrawable(R.drawable.background_snackbar_orange)
           imgAlert.setImageDrawable(resources.getDrawable(R.drawable.ic_warning))
           deviceconnection.text = resources.getString(R.string.device_low_battery)
       }else if(status == Constants.BATTERYALARM){
           llDeviceConnection.background = resources.getDrawable(R.drawable.snack_bar_warnning)
           imgAlert.setImageDrawable(resources.getDrawable(R.drawable.ic_warning))
           deviceconnection.text = resources.getString(R.string.device_low_battery_alarm)
       }else if(status == Constants.CALIBIRATED){
           llDeviceConnection.background = resources.getDrawable(R.drawable.snackbar_green)
           imgAlert.setImageDrawable(resources.getDrawable(R.drawable.ic_info_light))
           deviceconnection.text = resources.getString(R.string.msg_calibration_success)
       }
        snackbarLayout.setPadding(0,0,0,0)
        snackbarLayout.addView(customSnackbar,0)
        snackbar.show()
    }

    private fun registerTestStatusReceiver() {
        Log.i(ContentValues.TAG, "teststatusReceiver :: Start ")
        testStatusReciver = object : BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != null && intent.action == Constants.START_TEST) {
                    var test= intent.getBooleanExtra(Constants.TEST_STATUS, false)
                     testId=intent.getLongExtra(Constants.TEST_ID,0)
                    if(test){
                        isTestStarted=true
                        println("TestStarted")
                    }else{
                        isTestStarted=false
                        println("TestCompleted")
                    }
                }
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(Constants.START_TEST)
       registerReceiver(testStatusReciver, intentFilter)
    }

    fun dialogTestInProgressConfirm(fragment : Fragment, view :View){
        val dialog = MsgDialogFragment("Confirmation",resources.getString(R.string.test_in_progress))
        dialog.setPositiveStr("Yes")
        dialog.setNegativeStr("No")
        dialog.setConfirmActions(object : MsgDialogFragment.PositiveActionListener {
            override fun onPositiveAction(dialog: DialogInterface?, id: Int) {
                isTestStarted=false
                val intent = Intent(this@HomeActivity, BluetoothLeServiceKt::class.java)
                intent.putExtra(getString(R.string.key_is_record),false)
                startService(intent)
                uroinfoViewModel.deleteTestInfo(testId)
                uroTestInfoViewModel.deleteTestDetails(testId)
                backgroundChange(view)
                if(fragment.toString().contains("TabContainerFragment")) {
                    val bundle = Bundle()
                    bundle.putInt(resources.getString(R.string.key_tab_frag_change),1)
                    fragment.arguments = bundle
                }
                supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, fragment)?.commit()
                // Toast.makeText(requireContext(),"Yes",Toast.LENGTH_SHORT).show()

            }
        }, object : MsgDialogFragment.NegativeActionListener {
            override fun onNegativeAction(dialog: DialogInterface?, id: Int) {
                dialog!!.cancel()
            }
        })
        val ft: FragmentTransaction? = getSupportFragmentManager()?.beginTransaction()
        val prev: Fragment? = getSupportFragmentManager()?.findFragmentByTag("ConfirmDialog")
        if (prev != null) {
            ft?.remove(prev)
        }
        ft?.addToBackStack(null)
        dialog.show(ft!!, "ConfirmDialog")
    }

    fun backgroundChange(view: View){
        when(view){
            menuBinding.homeLlLayout -> {
                buttonClickedOn(view)
                buttonClickedOff( menuBinding.historyLlLayout)
                buttonClickedOff( menuBinding.deviceSetupLlLayout)
                buttonClickedOff( menuBinding.aboutLlLayout)
                buttonClickedOff( menuBinding.helpLlLayout)
                buttonClickedOff(menuBinding.hospitalSettingLlLayout)
            }
            menuBinding.historyLlLayout -> {
                buttonClickedOn(view)
                buttonClickedOff( menuBinding.homeLlLayout)
                buttonClickedOff( menuBinding.deviceSetupLlLayout)
                buttonClickedOff( menuBinding.aboutLlLayout)
                buttonClickedOff( menuBinding.helpLlLayout)
                buttonClickedOff(menuBinding.hospitalSettingLlLayout)
            }
            menuBinding.deviceSetupLlLayout -> {
                buttonClickedOn(view)
                buttonClickedOff( menuBinding.historyLlLayout)
                buttonClickedOff( menuBinding.homeLlLayout)
                buttonClickedOff( menuBinding.aboutLlLayout)
                buttonClickedOff( menuBinding.helpLlLayout)
                buttonClickedOff(menuBinding.hospitalSettingLlLayout)
            }
            menuBinding.aboutLlLayout -> {
                buttonClickedOn(view)
                buttonClickedOff( menuBinding.historyLlLayout)
                buttonClickedOff( menuBinding.deviceSetupLlLayout)
                buttonClickedOff( menuBinding.homeLlLayout)
                buttonClickedOff( menuBinding.helpLlLayout)
                buttonClickedOff(menuBinding.hospitalSettingLlLayout)
            }
            menuBinding.helpLlLayout -> {
                buttonClickedOn(view)
                buttonClickedOff( menuBinding.historyLlLayout)
                buttonClickedOff( menuBinding.deviceSetupLlLayout)
                buttonClickedOff( menuBinding.aboutLlLayout)
                buttonClickedOff( menuBinding.homeLlLayout)
                buttonClickedOff(menuBinding.hospitalSettingLlLayout)
            }
            menuBinding.hospitalSettingLlLayout ->{
                buttonClickedOn(view)
                buttonClickedOff( menuBinding.historyLlLayout)
                buttonClickedOff( menuBinding.deviceSetupLlLayout)
                buttonClickedOff( menuBinding.aboutLlLayout)
                buttonClickedOff( menuBinding.homeLlLayout)
                buttonClickedOff(menuBinding.helpLlLayout)
            }
        }
    }
    fun buttonClickedOn(view: View){
        view.background = (resources.getDrawable(R.drawable.elevated_radius))
        view.elevation = 5F
    }

    fun buttonClickedOff(view: View){
        view.setBackgroundColor(resources.getColor(R.color.background_splash))
        view.elevation = 0F
    }

    override fun buttonClicked(fragment: Fragment) {
        buttonClickedOn(menuBinding.hospitalSettingLlLayout)
        buttonClickedOff( menuBinding.historyLlLayout)
        buttonClickedOff( menuBinding.aboutLlLayout)
        buttonClickedOff( menuBinding.homeLlLayout)
        buttonClickedOff( menuBinding.helpLlLayout)
        buttonClickedOff( menuBinding.deviceSetupLlLayout)
        supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, fragment)?.commit()
    }


    override fun onBackPressed() {
        val manager = supportFragmentManager
        if (manager.backStackEntryCount > 1) {
            manager.popBackStack()
        } else {
            dialogExitConfirm()
            // if there is only one entry in the backstack, show the home screen
            Toast.makeText(this, "No action defined", Toast.LENGTH_LONG).show()
        }
        super.onBackPressed()
    }

    fun dialogExitConfirm( ){
        val dialog = MsgDialogFragment(resources.getString(R.string.app_name),resources.getString(R.string.app_exit))
        dialog.setPositiveStr("Yes")
        dialog.setNegativeStr("No")
        dialog.setConfirmActions(object : MsgDialogFragment.PositiveActionListener {
            override fun onPositiveAction(dialog: DialogInterface?, id: Int) {


            }
        }, object : MsgDialogFragment.NegativeActionListener {
            override fun onNegativeAction(dialog: DialogInterface?, id: Int) {
                dialog!!.cancel()
            }
        })
        val ft: FragmentTransaction? = getSupportFragmentManager()?.beginTransaction()
        val prev: Fragment? = getSupportFragmentManager()?.findFragmentByTag("ConfirmDialog")
        if (prev != null) {
            ft?.remove(prev)
        }
        ft?.addToBackStack(null)
        dialog.show(ft!!, "appExitDialog")
    }

    override fun redirectFragment(fragment: Fragment) {
        buttonClickedOn(menuBinding.deviceSetupLlLayout)
        buttonClickedOff( menuBinding.historyLlLayout)
        buttonClickedOff( menuBinding.aboutLlLayout)
        buttonClickedOff( menuBinding.homeLlLayout)
        buttonClickedOff( menuBinding.helpLlLayout)
        val bundle = Bundle()
        bundle.putInt(resources.getString(R.string.key_tab_frag_change),1)
        fragment.arguments = bundle
        supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, fragment)?.commit()
    }

    fun redirectCalibrationFragment(fragment: Fragment){
        buttonClickedOn(menuBinding.deviceSetupLlLayout)
        buttonClickedOff( menuBinding.historyLlLayout)
        buttonClickedOff( menuBinding.aboutLlLayout)
        buttonClickedOff( menuBinding.homeLlLayout)
        buttonClickedOff( menuBinding.helpLlLayout)
        val bundle = Bundle()
        bundle.putInt(resources.getString(R.string.key_tab_frag_change),2)
        fragment.arguments = bundle
        supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, fragment)?.commit()
    }
}



