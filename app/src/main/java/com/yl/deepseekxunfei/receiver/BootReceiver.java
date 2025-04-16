package com.yl.deepseekxunfei.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.yl.deepseekxunfei.service.VoiceWakeupService;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            //implement your code
            Intent serviceIntent = new Intent(context, VoiceWakeupService.class);
            context.startService(serviceIntent);
        }
    }
}
