package com.projetinfo.android.watchint;

import android.os.Message;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.widget.Button;
import android.bluetooth.BluetoothDevice;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import android.widget.EditText;
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
    private BluetoothAdapter myBluetoothAdapter;	                                                // L'adaptateur bluetooth de l'appareil
    private BluetoothSocket  btSocket	 = null;													// Interface de connexion (socket) vers le bluetooth
    private Handler handler;
    private SMSReceiver smsReceiver;
    private int S = 0, A = 0, T=0;

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

        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //checkBluetooth();
        //visible();

        final EditText input = (EditText) findViewById(R.id.chatbox);
        Button sendBtn = (Button) findViewById(R.id.button_send);
        sendBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String msg = input.getText().toString();
                input.setText("");
                remindReceived(msg);
                handler.sendMessage(Message.obtain(handler, 0, "Android : " + msg));
                //remindReceived(msg);
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



        smsReceiver = new SMSReceiver(this);
        IntentFilter intent = new IntentFilter();
        intent.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        registerReceiver(smsReceiver, intent);

    /*
        TestReceiver receiver = new TestReceiver();
        IntentFilter intent = new IntentFilter();
        intent.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        intent.addAction(Telephony.Sms.Intents.SMS_DELIVER_ACTION);
        intent.addAction(Telephony.Sms.Intents.SMS_REJECTED_ACTION);
        intent.addAction(Telephony.Sms.Intents.SMS_CB_RECEIVED_ACTION);
        intent.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        intent.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);


        getApplicationContext().registerReceiver(receiver, intent);*/
    }

    public void smsReceived(String from)
    {
        ++S;
        Toast.makeText(getApplicationContext(), "TEST", Toast.LENGTH_SHORT).show();
        connectThread.addMessageToSend("I" + S + A + T + "SMS FROM " + from);
        handler.sendMessage(Message.obtain(handler, 0, "Android : J'ai recu un SMS"));
    }

    public void callReceived(String from)
    {
        ++A;
        connectThread.addMessageToSend("I" + S + A + "CALL FROM " + from);

    }

    public void remindReceived(String from)
    {
        ++T;
        connectThread.addMessageToSend("I" + S + A + T + from);
    }

    public void appendText(String text)
    {
        handler.sendMessage(Message.obtain(handler, 0, text));
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
        if(myBluetoothAdapter == null)
            showErrorExit("Non supporté");
        else
        {
            if (myBluetoothAdapter.isEnabled())
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
        unregisterReceiver(smsReceiver);
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
        private volatile Queue<String> messagesToSend;

        public synchronized void addMessageToSend(String str)
        {
            messagesToSend.add(str);
            while (!messagesToSend.isEmpty())
            {
                String msg = messagesToSend.poll();
                write(msg);
                handler.sendMessage(Message.obtain(handler, 0, "Android : " + msg));
            }
        }

        public ConnectedThread(BluetoothSocket socket)
        {
            messagesToSend = new LinkedList<>();

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
            int bytes;                                                                              // Nombres d'octets reçus

            String data = "T" + ((int) (System.currentTimeMillis() / 1000L));
            write(data);
            handler.sendMessage(Message.obtain(handler, 0, "Android : " + data));



            while (true)
            {
                try
                {
                    // Lire depuis le flux d'entrée
                    bytes = inStream.read(buffer);                                                  // Obtenir le nombre d'octets lus,
                                                                                                    // ceux-ci sont stockés dans le buffer

                    if (bytes > 0) {
                        if (buffer[0] == 'C') {
                            A = 0;
                            S = 0;
                            T = 0;
                            String msg1 = "I000";
                            write(msg1);
                            handler.sendMessage(Message.obtain(handler, 0, "Arduino : " + stringOf(buffer, bytes)));
                            handler.sendMessage(Message.obtain(handler, 0, "Android : " + msg1));

                        }
                        else {
                            handler.sendMessage(Message.obtain(handler, 0, "Arduino : " + stringOf(buffer, bytes)));
                        }
                    }

                    /*while (!messagesToSend.isEmpty())
                    {
                        String msg = messagesToSend.poll();
                        write(msg);
                        handler.sendMessage(Message.obtain(handler, 0, "Android : " + msg));
                    }*/
                        //TODO: réception données
                        //handler.sendMessage(Message.obtain(handler, 0, "Arduino : " + stringOf(buffer, bytes)));

                        //handler.sendMessage(Message.obtain(handler, 0, "Smartphone: " + data));


                    //Thread.sleep(100);
                } catch (IOException/* | InterruptedException*/ e)
                {
                    appendText(e.getMessage());
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
        public void write(String message)
        {
            byte[] msgBuffer = message.getBytes();													// Obtenir le message en binaire

            try
            {
                outStream.write(msgBuffer);														    // Ecrit les données binaires sur le flux
            } catch (IOException e)
            {

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



