package com.projetinfo.android.watchint;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

/**
 * Created by Seven on 16/05/2017.
 */

public class TestReceiver extends BroadcastReceiver {

    public TestReceiver()
    {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, intent.getAction(), Toast.LENGTH_LONG).show();
    }
}
