package com.paar.ch9;

import java.util.ArrayList;
import java.util.List;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import jp.androidgroup.nyartoolkit.R;


public class LocalDataSource extends DataSource{
    private List<Marker> cachedMarkers = new ArrayList<Marker>();
    private static Bitmap[] icons = new Bitmap[3];
    
    private static String[] descriptions = new String[3];
//    private static Bitmap icon = null;
    
    public LocalDataSource(Resources res) {
        if (res==null) throw new NullPointerException();
        
        createIcon(res);
    }
    
    protected void createIcon(Resources res) {
        if (res==null) throw new NullPointerException();
        
//        icon=BitmapFactory.decodeResource(res, R.drawable.ic_launcher);
        icons[0] = BitmapFactory.decodeResource(res, R.drawable.machingunlily);
        icons[1] = BitmapFactory.decodeResource(res, R.drawable.marmot_s);
        icons[2] = BitmapFactory.decodeResource(res, R.drawable.elk_bull_s);
    }
    
    public List<Marker> getMarkers() {
//        Marker atl = new IconMarker("ATL", 39.931269, -75.051261, 0, Color.DKGRAY, icon);
//        cachedMarkers.add(atl);
//
//        Marker home = new Marker("Mt Laurel", 39.95, -74.9, 0, Color.YELLOW);
//        cachedMarkers.add(home);
    	
    	descriptions[0] = "The Columbian Ground Squirrel is the most commonly seen animal in the park during the summer.";
    	descriptions[1] = "Hoary Marmots are colonial animals that live in the alpine zone from 6,800 to 8,000 feet.";
    	descriptions[2] = "Elk are light brown with dark faces, necks and legs.";

        Marker machingunlily = new IconMarker("Machingun Lily", 39.931269, -75.051261, 20, Color.DKGRAY, icons[0], descriptions[0]);
        cachedMarkers.add(machingunlily);
        
        Marker marmot = new IconMarker("Marmot", 39.94, -75.04, 200, Color.DKGRAY, icons[1], descriptions[1]);
        cachedMarkers.add(marmot);

        Marker elk_bull = new IconMarker("Elk bull", 39.926, -75.03, 400, Color.DKGRAY, icons[2], descriptions[2]);
        cachedMarkers.add(elk_bull);
        
        return cachedMarkers;
    }
}