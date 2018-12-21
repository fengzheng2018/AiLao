package com.android.ailao.gps;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.android.ailao.R;

import java.lang.ref.WeakReference;

public class GpsHandler extends Handler {

    private final WeakReference<TextView> gpsTextReference;

    public GpsHandler(TextView gpsStatusTxt){
        this.gpsTextReference = new WeakReference<>(gpsStatusTxt);
    }

    @Override
    public void handleMessage(Message msg){
        super.handleMessage(msg);

        switch (msg.what){
            case 806:{
                int satelliteNum = msg.arg1;

                if(satelliteNum < 3){
                    gpsTextReference.get().setText(R.string.gps_status_0);
                }else if(satelliteNum > 3 && satelliteNum <= 6){
                    gpsTextReference.get().setText(R.string.gps_status_1);
                }else if(satelliteNum > 6 && satelliteNum <= 12){
                    gpsTextReference.get().setText(R.string.gps_status_2);
                }else if(satelliteNum > 12 && satelliteNum <= 18){
                    gpsTextReference.get().setText(R.string.gps_status_3);
                }else if(satelliteNum > 18 && satelliteNum <= 20){
                    gpsTextReference.get().setText(R.string.gps_status_4);
                }else if(satelliteNum > 20){
                    gpsTextReference.get().setText(R.string.gps_status_5);
                }

                break;
            }
        }
    }
}
