package com.example.tp3_bluetooth_low_energy

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.UUID


class SupervisorActivity : AppCompatActivity() {
    /*
     * TODO: private lateinit var binding : ...
     */
    /**
     * Méthode appelée à la création de l'activité
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*
         * TODO: Utiliser le view binding pour lier l'activité à un layout
         */
        /*
         * TODO: Récupérer le BluetoothDevice transmis par l'activité précédente
         *  Puis l'envoyer à la fonction connectToDevice()
         */
    }



    private val handler = Handler(Looper.getMainLooper())

    companion object {
        /**
         * Les UUIDS sont des identifiants uniques qui permettent d'identifier les attributs :
         * les services, les charactéristics et les descriptors
         * Ces UUIDs sont définis dans le code du STM32.
         */
        val SERVICE_LED_UUID: UUID = UUID.fromString("00000000-0002-11e1-9ab4-0002a5d5c51b")
        val CHARACTERISTIC_LED_UUID: UUID = UUID.fromString("00000100-0001-11e1-ac36-0002a5d5c51b")

        val SERVICE_NOTIFY_UUID: UUID = UUID.fromString("00000000-0001-11e1-9ab4-0002a5d5c51b")
        val CHARACTERISTIC_RANDOM_UUID: UUID = UUID.fromString("00e00000-0001-11e1-ac36-0002a5d5c51b")
        val DESCRIPTOR_RANDOM_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        val CHARACTERISTIC_AMBIENT_UUID: UUID = UUID.fromString("00140000-0001-11e1-ac36-0002a5d5c51b")
        val DESCRIPTOR_AMBIENT_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
    // Le BluetoothGatt gère la connexion avec le serveur GATT
    private var currentBluetoothGatt: BluetoothGatt? = null
    /**
     * Cette méthode permet de se connecter au serveur BLE GATT
     * Elle instancie le BluetoothGatt et défini son comportement
     */
    @SuppressLint("MissingPermission")
    private fun connectToDevice(device : BluetoothDevice) {
        Toast.makeText(this, "Connexion en cours … $device", Toast.LENGTH_SHORT).show()
        // Création du BluetoothGatt
        currentBluetoothGatt = device.connectGatt(
            this,
            false,
            object : BluetoothGattCallback() {
                /**
                 * Méthode appelée au moment ou les « services » ont été découvert
                 */
                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                    super.onServicesDiscovered(gatt, status)
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        runOnUiThread {
                            // Nous sommes connecté et la découverte des services est terminée
                            /*
                             * TODO: Signifier à l'utilisateur que nous sommes connecté
                             *  Par exemple : changer le texte d'un TextView
                             */

                            /*
                             * TODO: Activer les notifications pour les characteristics
                             *  Random et Ambient
                             */
                        }
                    } else {
                        runOnUiThread { disconnectFromCurrentDevice() }
                    }
                }
                /**
                 * Méthode appelée au moment du changement d'état de la stack BLE
                 */
                @SuppressLint("MissingPermission")
                override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                    super.onConnectionStateChange(gatt, status, newState)
                    when (newState) {
                        BluetoothGatt.STATE_CONNECTED -> { gatt.discoverServices() }
                        BluetoothGatt.STATE_DISCONNECTED -> runOnUiThread { disconnectFromCurrentDevice() }
                    }
                }
                /**
                 * Méthodes appelée à chaque notifications BLE
                 */
                override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                    super.onCharacteristicChanged(gatt, characteristic)
                    runOnUiThread {
                        // Nous avons reçu une notification
                        // Il suffit de regarder l'UUID de la characteristic pour savoir de laquelle il s'agit
                        /*
                         * TODO: Gérer la réception de notification de Random puis de Ambient
                         */
                    }
                }
            }
        )
    }
    /**
     * Cette méthode permet de se déconnecter du serveur GATT
     */
    @SuppressLint("MissingPermission")
    private fun disconnectFromCurrentDevice() {
        currentBluetoothGatt?.disconnect()
    }


    // État de la LED allumé/éteinte
    private var ledState : Boolean = false
    /**
     * Cette méthode change l'état de la LED en écrivant sur la characteristic du serveur Gatt
     */
    @SuppressLint("MissingPermission")
    private fun toggleLed() {
        /*
         * TODO: Récupérer le service puis la characteristic en utilisant les bons UUID
         *  Écrire la bonne séquence d'octet pour changer l'état de la LED
         *  Mettre à jour l'état de la LED
         */
    }

    // type énuméré permettant de différencier les deux characteristics du service
    enum class CharacteristicSelected {
        AMBIENT, RANDOM
    }
    /**
     * Cette méthode active les notifications d'une characteristic en fonction de la valeur passée
     * Soit CharacteristicSelected.AMBIENT, soit CharacteristicSelected.RANDOM
     */
    @SuppressLint("MissingPermission")
    private fun enableListenBleNotify(cs : CharacteristicSelected) {
        val characteristicUUID = if (cs == CharacteristicSelected.AMBIENT) CHARACTERISTIC_AMBIENT_UUID else CHARACTERISTIC_RANDOM_UUID
        val descriptorUUID = if (cs == CharacteristicSelected.AMBIENT) DESCRIPTOR_AMBIENT_UUID else DESCRIPTOR_RANDOM_UUID
        val service = currentBluetoothGatt?.getService(SERVICE_NOTIFY_UUID)
        /*
         * TODO: Récupérer la characteristic puis le descriptor
         *  Activer les notifications côté client
         *  Signaler au serveur qu'il peut démarrer les notifications en écrivant sur le descriptor
         */
    }

    /**
     * Méthode appelée à la destruction de l'activité
     */
    override fun onDestroy() {
        super.onDestroy()
        disconnectFromCurrentDevice()
    }
}
