package com.devarshukani.clearmindlauncher.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.devarshukani.clearmindlauncher.Service.AppBlockerService;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, AppBlockerService.class);
            context.startService(serviceIntent);
        }
    }
}
