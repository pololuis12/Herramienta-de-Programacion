package com.example.dogcare;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Las alarmas se pierden al reiniciar el teléfono y quedan desfasadas si cambia la hora o el
 * permiso de alarmas exactas: en esos casos se vuelven a programar todos los recordatorios.
 */
public class ArranqueReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String accion = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(accion)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(accion)
                || Intent.ACTION_TIME_CHANGED.equals(accion)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(accion)
                || AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED.equals(accion)) {
            Recordatorios.reprogramarTodas(context);
        }
    }
}
