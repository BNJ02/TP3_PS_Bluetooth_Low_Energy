package com.example.tp3_bluetooth_low_energy

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.tp3_bluetooth_low_energy.adapter.DeviceAdapter
import com.example.tp3_bluetooth_low_energy.databinding.ActivityDeviceScanBinding

class DeviceScanActivity : AppCompatActivity() {
    /*
     * TODO: private lateinit var binding : ...
     */
    private lateinit var binding: ActivityDeviceScanBinding
    private lateinit var recyclerAdapter: DeviceAdapter
    /**
     * Méthode appelée à la création de l'activité
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*
         * TODO: Utiliser le view binding pour lier l'activité à un layout
         */
        binding = ActivityDeviceScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialisation de l'affichage du RecyclerView
        recyclerAdapter = DeviceAdapter(
            bleDevicesFoundList = bleDevicesFoundList,
            onDeviceClickedDA = { device -> this.onDeviceClicked(device) }
        )

        // Initialisation de l'affichage du RecyclerView
        binding.recyclerView.adapter = recyclerAdapter
    }

    // Méthode appelée lorsque l'utilisateur clique sur un périphérique/device
    fun onDeviceClicked(device: BluetoothDevice) {
        val intent = Intent(this, SupervisorActivity::class.java)
        intent.putExtra("device", device)
        this.startActivity(intent)
    }

    /**
     * Méthode appelée lorsque l'activité repasse complètement au premier plan
     */
    private var permissionsAccepted = true
    override fun onResume() {
        super.onResume()
        if (permissionsAccepted) {
            permissionsAccepted = false
            if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                // Test si l'appareil est compatible BLE, si ça n'est pas le cas : finish()
                Toast.makeText(this, "Appareil non compatible BLE", Toast.LENGTH_SHORT).show()
                finish()
            } else if(!hasPermission()) {
                // Test si on a les permissions
                // Si non, on demande les demande à l'utilisateur
                askForPermission()
            } else if (!locationServiceEnabled()) {
                // Test si la localisation est activée
                // Sinon on ouvre les parametres pour demander d'activer la localisation
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            } else {
                // La méthode setupBLE() va initialiser le BluetoothAdapter et lancera le scan
                setupBLE()
            }
        }
    }


    /* -------------------------------------------------------------------------------------------
     *                                                                                           *
     *                                   GESTION DES PERMISSIONS                                 *
     *                                                                                           */

    /**
     * Cette méthode doit vérifier si l'application possède la permission « Localisation ».
     * OBLIGATOIRE pour scanner en BLE
     */
    private fun hasPermission(): Boolean {
        /* TODO:
        * À partir de Android 11 (API 30), il faut les permissions « BLUETOOTH_CONNECT » et « BLUETOOTH_SCAN ».
        * Sur Android 10 (API 29) et inférieur, il faut la permission « ACCESS_FINE_LOCATION » qui permet de scanner en BLE.
        */
        val isAllow = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            // À partir de Android 12 (API 31), il faut les permissions « BLUETOOTH_CONNECT » et « BLUETOOTH_SCAN ».
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            // Sur Android 11 (API 30) et inférieur, il faut la permission « ACCESS_FINE_LOCATION » qui permet de scanner en BLE.
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        }
        return isAllow
    }

    // La demande de permission doit être faîte avec le « requestCode » PERMISSION_REQUEST_LOCATION
    private val PERMISSION_REQUEST_LOCATION = 1
    /**
     * Cette méthode doit demander la (ou les) permissions à l'utilisateur nécessaire(s) pour scanner en BLE.
     */
    private fun askForPermission() {
        /* TODO:
        * À partir de Android 11 (API 30), il faut demander les permissions « BLUETOOTH_CONNECT » et « BLUETOOTH_SCAN ».
        * Sur Android 10 (API 29) et inférieur, il faut demander la permission « ACCESS_FINE_LOCATION » qui permet de scanner en BLE.
	    */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            // Sur Android 11 (API 30) et inférieur, il faut la permission « ACCESS_FINE_LOCATION » qui permet de scanner en BLE.
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_LOCATION
            )
        } else {
            // À partir de Android 12 (API 31), il faut les permissions « BLUETOOTH_CONNECT » et « BLUETOOTH_SCAN ».
            requestPermissions(
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN), PERMISSION_REQUEST_LOCATION
            )
        }

    }

    /**
     * Cette méthode est appelée après la demande de permission.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // On vérifie que l'appel correspond à la requête de permission de Localisation
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // L'utilisateur n'a pas accepté la permission : on lui affiche un AlertDialog
                // pour lui signaler que la permission est nécessaire
                // - s'il clique sur "Annuler" on quitte
                // - s'il clique sur "Ok" on lui redemande la permission
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setMessage("L'application ne peut fonctionner sans cette permission")
                    .setTitle("Permission nécessaire")
                    .setPositiveButton("Ok") { dialog, which ->
                        askForPermission()
                    }
                    .setNegativeButton("Annuler") { dialog, which ->
                        finish()
                    }
                val dialog: AlertDialog = builder.create()
                dialog.show()
            } else {
                // On vérifie que le service de localisation est activé
                if (!locationServiceEnabled()) {
                    // Si ça n'est pas le cas on invite à activer la localisation
                    // en ouvrant les paramètres systèmes
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                } else {
                    // On a la permission et la localisation est activée
                    // La méthode setupBLE() va initialiser le BluetoothAdapter et lancera le scan
                    setupBLE()
                }
            }
        }
    }

    /**
     * Cette méthode vérifie si le service de localisation est actif sur l'appareil
     */
    private fun locationServiceEnabled(): Boolean {
        // Là encore le code dépend de la version de la SDK
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // À partir de Android 9 (API 28)
            val lm : LocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.isLocationEnabled
        } else {
            // Sur Android 8.1 (API 27) et inférieur
            val mode = Settings.Secure.getInt(this.contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF)
            (mode != Settings.Secure.LOCATION_MODE_OFF)
        }
    }
    /*                                                                                           *
     * ----------------------------------------------------------------------------------------- */

    /* -------------------------------------------------------------------------------------------
     *                                                                                           *
     *                                  ACTIVATION DU BLUETOOTH                                  *
     *                                                                                           */

    // Le BluetoothAdapter permet (entre autre) de vérifier si le bluetooth est actif
    private var bluetoothAdapter: BluetoothAdapter? = null

    /**
     * La méthode « registerForActivityResult » permet de gérer le résultat d'une activité.
     * Ce code est appelé à chaque fois que l'utilisateur répond à la demande d'activation du Bluetooth (visible ou non)
     */
    /*
    5/ Si l'utilisateur refuse d'activer le Bluetooth, une nouvelle fenêtre apparaît pour lui demander à nouveau car
    l'application a besoin d'utiliser le Bluetooth pour fonctionner correctement.
    Cependant, il est important de ne pas boucler sur une demande d'activation du Bluetooth si l'utilisateur refuse.
     */
    val registerForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            // L'utilisateur a accepté, le Bluetooth est activé, on lance le scan
            scanBLE()
        } else {
            // L'utilisateur a refusé, le Bluetooth n'est pas activé,
            /*
             * TODO: Gérer ce cas
             */
            val message = "Vous devez activer le Bluetooth pour utiliser cette application. Veuillez quitter l'application et réessayer en activant le Bluetooth."
            binding.messageTextView.text = message
            binding.messageTextView.visibility = View.VISIBLE
        }
    }

    /**
     * Cette méthode active le Bluetooth s'il n'est pas encore activé
     */
    private fun setupBLE() {
        // On récupère le service BluetoothManager
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        // Il nous fourni le BluetoothAdapter
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter != null && !bluetoothManager.adapter.isEnabled) {
            // Le bluetooth n'est pas activé, on demande à l'utilisateur de l'activer
            registerForResult.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        } else {
            // Le bluetooth est activé, on lance le scan
            scanBLE()
        }
    }
    /*                                                                                           *
     * ----------------------------------------------------------------------------------------- */

    /* -------------------------------------------------------------------------------------------
     *                                                                                           *
     *                                           SCAN                                            *
     *                                                                                           */

    // BluetoothLeScanner permet de scan les appareils BLE à proximiter
    private var bluetoothLeScanner: BluetoothLeScanner? = null

    // Parametrage du scan BLE
    private val scanSettings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

    // On ne retourne que les appareils proposant le bon UUID
    private var scanFilters: List<ScanFilter> = arrayListOf(
        //  ScanFilter.Builder().setServiceUuid(ParcelUuid(BluetoothLEManager.DEVICE_UUID)).build()
    )

    // Liste d'appareils BLE découvert
    private val bleDevicesFoundList = arrayListOf<BluetoothDevice>()

    // Callback appelé à chaque périphérique trouvé
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            val device = result.device
            if (!bleDevicesFoundList.contains(device)) {
                /*
                 * TODO: Ajouter l'appareil découvert à la liste des appareils
                 */
                bleDevicesFoundList.add(device)
                /*
                 * TODO: Mettre à jour l'affichage du RecyclerView (remplacer le recyclerViewID)
		         * binding.recyclerViewID.adapter?.notifyItemInserted(bleDevicesFoundList.size - 1)
                 */
                binding.recyclerView.adapter?.notifyItemInserted(bleDevicesFoundList.size - 1)
            }
        }
    }

    // Variable pour ne lancer le scan qu'une seule fois
    private var mScanning = false

    // Handler pour mesurer l'écoulement du temps dans un thread séparé
    private val handler = Handler(Looper.getMainLooper())

    /**
     * Cette méthode va initier le scan pour une période déterminé par scanPeriod
     */
    private fun scanBLE(scanPeriod: Long = 10000) {
        if (!mScanning) {
            bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
            // On vide la liste qui contient les appareils actuellement trouvés
            bleDevicesFoundList.clear()
            // Évite de scanner en double
            mScanning = true
            // On lance un thread qui au bout de scanPeriod (par défaut 10s) stop le scan
            handler.postDelayed({
                mScanning = false
                bluetoothLeScanner?.stopScan(leScanCallback)
                Toast.makeText(this, "Arrêt du scan bluetooth", Toast.LENGTH_SHORT).show()
            }, scanPeriod)
            // On lance le scan
            bluetoothLeScanner?.startScan(scanFilters, scanSettings, leScanCallback)
        }
    }
    /*                                                                                           *
     * ----------------------------------------------------------------------------------------- */
}