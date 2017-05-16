package com.projetinfo.android.watchint;

import android.os.Message;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.widget.Button;
import android.bluetooth.BluetoothDevice;
import java.util.Set;
import android.widget.TextView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothSocket;
import android.os.Build;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    //
    private TextView text;
    private BluetoothAdapter myBluetoothAdapter;
    private BluetoothAdapter btAdapter = null;														// L'adaptateur bluetooth de l'appareil
    private BluetoothSocket  btSocket	 = null;														// Interface de connexion (socket) vers le bluetooth
    private Handler handler;

    private static String address = "20:16:11:29:58:02";										    // Adresse MAC du module bluetooth de l'arduino
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");	// SPP UUID service

    private ConnectedThread connectThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new TextHandler();
        text = (TextView) findViewById(R.id.textView);
        final TextView tmpText = text;
        checkBluetooth();
        visible();

        Button sendBtn = (Button) findViewById(R.id.button_send);
        sendBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                handler.sendMessage(Message.obtain(handler, 0, "Texte envoyé"));
            }
        });

        Button clearBtn = (Button) findViewById(R.id.clearBtn);
        clearBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                tmpText.setText("");
            }
        });

        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void onResume()
    {
        super.onResume();                                                                           // Appeler la fonction par défault

        DEBUG_showMessage("Fonction onResume - tentative de connection...");

        BluetoothDevice arduinoBt = myBluetoothAdapter.getRemoteDevice(address);			        // Obtenir un pointeur vers le module bluetooth de l'arduino

        //-------------------------------- Ouvre le socket ---------------------------------------//
        try
        {
            btSocket = createBluetoothSocket(arduinoBt);
            Toast.makeText(getApplicationContext(),
                    "Succès socket", Toast.LENGTH_LONG)
                    .show();
        } catch (IOException e)
        {
            showErrorExit("Fonction onResume - Le socket n'a pas pu être créé : " + e.getMessage());
        }

        myBluetoothAdapter.cancelDiscovery();														// Désactiver le mode discovery inutile ici

        //--------------------------- Commencer la connection ------------------------------------//
        DEBUG_showMessage("Connection en cours...");
        try
        {
            btSocket.connect();
            Toast.makeText(getApplicationContext(),
                    "succès connexion", Toast.LENGTH_LONG)
                    .show();
            DEBUG_showMessage("Succès connection");
        } catch (IOException e)
        {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                showErrorExit("Fonction onResume - Le socket n'a pas pu être fermé : " + e2.getMessage());
            }
        }

        //--------------- Création d'un flux de données pour parler avec le module ---------------//
        DEBUG_showMessage("Obtention du flux");
        connectThread = new ConnectedThread(btSocket);
        connectThread.start();
    }

    public void onPause()
    {
        super.onPause();                                                                            // Appeler la fonction par défault

        DEBUG_showMessage("Fonction onPause");

        try
        {
            btSocket.close();                                                                       // Nettoye le flux (supprime toutes les données restantes)
            Toast.makeText(getApplicationContext(),
                    "Suppression socket", Toast.LENGTH_LONG)
                    .show();
        } catch (IOException e2)
        {
            showErrorExit("Fonction onPause - Le flux n'a pas pu être nettoyé : " + e2.getMessage());
        }
    }

    //----------Vérifie que le bluetooth est supporté puis si il est activé sur l'appareil--------//
    private void checkBluetooth()
    {
        if(btAdapter == null)
            showErrorExit("Le bluetooth n'est pas supporté");
        else
        {
            if (btAdapter.isEnabled())
                DEBUG_showMessage("Bluetooth ON");
            else
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);			// Afficher la fenètre pour demander à l'utilisateur
                startActivityForResult(enableBtIntent, 1);											//\ d'activer le bluetooth
            }
        }
    }



    @Override
    public void onClick(View v) {
    }


    //--------------------------------Pour rendre visible notre appareil--------------------------//
    public void visible() {
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //---------------------------Ouvre un socket vers le module spécifié--------------------------//
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        if(Build.VERSION.SDK_INT >= 10)
            return device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    private class TextHandler extends Handler
    {
        public void handleMessage(Message msg)
        {
            String textStr = (String) msg.obj;
            text.append(textStr+"\n");
        }
    }



    //--------------------------------Processus de lecture et écriture----------------------------//
    private class ConnectedThread extends Thread
    {
        private final InputStream  inStream;
        private final OutputStream outStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream  tmpIn  = null;
            OutputStream tmpOut = null;
                                                                                                    //Les fluxs in et out doivent être finaux, on utilise donc des variables temporaires
            try
            {
                tmpIn  = socket.getInputStream();
                tmpOut = socket.getOutputStream();

                Toast.makeText(getApplicationContext(),
                        "Succès flux entrée et sortie", Toast.LENGTH_LONG)
                        .show();
            } catch (IOException e) { }

            inStream  = tmpIn;
            outStream = tmpOut;
        }

        //----------------------------------Lancement du processus--------------------------------//
        public void run()
        {
            byte[] buffer = new byte[10];  														    // buffer pour le flux entrant
            int bytes;																				// Nombres d'octets reçus

            while (true)
            {
                try
                {
                                                                                                    // Lire depuis le flux d'entrée
                    bytes = inStream.read(buffer);        										    // Obtenir le nombre d'octets lus,
                                                                                                    // ceux-ci sont stockés dans le buffer
                    if (bytes > 0)
                    {

                        if (buffer[0] == 'C'){
                            String msg1 = "TI000";
                            write(msg1);
                        }

                        //TODO: réception données
                        handler.sendMessage(Message.obtain(handler, 0, "Arduino: " + stringOf(buffer, bytes)));

                        String data = "T" + ((int) (System.currentTimeMillis() / 1000L));
                        write(data);

                        handler.sendMessage(Message.obtain(handler, 0, "Smartphone: " + data));
                    }
                } catch (IOException e)
                {
                    break;
                }
            }
        }

        private String stringOf(byte[] bytes, int n)
        {
            char[] tmp = new char[n];
            for (int i = 0; i < n; ++i)
                tmp[i] = (char)bytes[i];
            return new String(tmp);
        }

        //--------------------------Envoyer des données vers le module arduino--------------------//
        private void write(String message)
        {
            byte[] msgBuffer = message.getBytes();													// Obtenir le message en binaire

            try
            {
                outStream.write(msgBuffer);														    // Ecrit les données binaires sur le flux
            } catch (IOException e)
            {
                showErrorExit("Envoi de données - les données n'ont pas pu être écrites : " + e.getMessage()
                        + "\nVérifiez que le SPP UUID: " + MY_UUID.toString() + " existe.");
            }
        }
    }

    //--------------------------------------------------------------------------------------------//
    private static final String TAG = "testBluetooth";												// Constante utilisée pour les logs

    private void showErrorExit(String message)
    {
        Toast.makeText(getBaseContext(), "Erreur" + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private void DEBUG_showMessage(String message)
    {
        Log.d(TAG, message);
    }
    //--------------------------------------------------------------------------------------------//
}


