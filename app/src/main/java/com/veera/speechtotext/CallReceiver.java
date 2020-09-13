package com.veera.speechtotext;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;


public class CallReceiver extends BroadcastReceiver  {


    @Override
    public void onReceive(final Context context, final Intent intent) {


        if(intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_OFFHOOK))
        {
            context.startService(new Intent(context, FloatingWindow.class));
        }
        else if(intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE))
        {
           // Intent i=new Intent(context,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //context.startActivity(i);
        }
        else if(intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING))
        {
            context.startService(new Intent(context, FloatingWindow.class));
        }

        }

}
