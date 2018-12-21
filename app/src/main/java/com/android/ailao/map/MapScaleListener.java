package com.android.ailao.map;

import android.widget.ImageButton;

import com.esri.arcgisruntime.mapping.view.MapScaleChangedEvent;
import com.esri.arcgisruntime.mapping.view.MapScaleChangedListener;
import com.esri.arcgisruntime.mapping.view.MapView;


/**
 * MapView加载过程缩放监听
 */
public class MapScaleListener implements MapScaleChangedListener {

    private ImageButton zoomIn;
    private ImageButton zoomOut;

    public MapScaleListener(ImageButton zoomIn, ImageButton zoomOut) {
        this.zoomIn = zoomIn;
        this.zoomOut = zoomOut;
    }

    @Override
    public void mapScaleChanged(MapScaleChangedEvent mapScaleChangedEvent) {
        MapView mMapView = mapScaleChangedEvent.getSource();

        double mapScale = mMapView.getMapScale();

        if(mapScale <= 2000.0){
            zoomIn.setClickable(false);
            mMapView.setViewpointScaleAsync(2000.0);
        }else if(mapScale >= 10000000.0){
            zoomOut.setClickable(false);
            mMapView.setViewpointScaleAsync(10000000.0);
        }else {
            zoomIn.setClickable(true);
            zoomOut.setClickable(true);
        }
    }
}
