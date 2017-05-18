package com.projetinfo.android.watchint;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

/*

<receiver class="com.projetinfo.android.watchint.SMSReceiver"
                android:name="com.projetinfo.android.watchint.SMSReceiver">
                <intent-filter android:priority="100">
                    <action android:name="android.provider.Telephony.SMS_RECEIVED" />
                </intent-filter>
            </receiver>
 */

public class SMSReceiver extends BroadcastReceiver
{
        private MainActivity activity;

        public SMSReceiver(MainActivity activity)
        {
            this.activity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
            // get the bundles in the message
            final Bundle bundle = intent.getExtras();

            SmsMessage[] messages = null;

            if   (   intent.getAction().equalsIgnoreCase("android.provider.Telephony.SMS_RECEIVED"))
            {
                if (bundle != null) {

                    final Object[] pdusObj = (Object[]) bundle.get("pdus");                         // On récupère le message grâce aux extras de l’intent
                    messages = new SmsMessage[pdusObj.length];                                      // Puis on récupère dans le bundle , l’extra correspondant à  l’indentifiant “pdus”
                    for (int i = 0; i < messages.length; i++)
                    {

                        // On récupère et on parcourt tous les messages pour obtenir le dernier

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {                       // Puis on récupère les infos souhaitées dans le dernier message
                            String format = bundle.getString("format");
                            messages[i] = SmsMessage.createFromPdu((byte[]) pdusObj[i], format);
                        }
                        else {
                            messages[i] = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                        }

                        activity.smsReceived(messages[i].getOriginatingAddress());
                        //activity.appendText(strMessage);
                    }

                    //Log.e("SMS", strMessage);
                    //Toast.makeText(context, strMessage, Toast.LENGTH_LONG).show();
                }
            }
        }
}
