package com.paar.ch9;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;

import org.takanolab.ar.search.Document;

public class SearchDataSource extends NetworkDataSource {
//	private static final String BASE_URL = "http://localhost/xampp/rocky/get_data.php";
	private static final String BASE_URL = "http://www2.mkm.ic.kanagawa-it.ac.jp/~takano/rocky/get_data.php";

//	private static Bitmap icon = null;
	
	public SearchDataSource(Resources res) {        
	    if (res==null) throw new NullPointerException();
        
        createIcon(res);
    }

    protected void createIcon(Resources res) {
        if (res==null) throw new NullPointerException();
        
        //icon=BitmapFactory.decodeResource(res, R.drawable.wikipedia);
//        icon=BitmapFactory.decodeResource(res, R.drawable.machingunlily_s);
    }

	@Override
	public String createRequestURL(double lat, double lon, double alt, float radius, String keywords) {
		return BASE_URL+
        "?query=" + keywords;

	}

	public List<Document> parseSearchResult(JSONObject root) {
		if (root==null) return null;
		
		JSONObject jo = null;
		JSONArray dataArray = null;
    	List<Document> documents=new ArrayList<Document>();

		try {
			if(root.has("docs")) dataArray = root.getJSONArray("docs");
			if (dataArray == null) return documents;
				int top = Math.min(MAX, dataArray.length());
				for (int i = 0; i < top; i++) {					
					jo = dataArray.getJSONObject(i);

					Document doc = new Document();
					doc.setName(jo.getString("name"));
					doc.setDescription(jo.getString("description"));
					if(doc!=null) documents.add(doc);
				}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return documents;
	}
	
	// Not used
	@Override
	public List<Marker> parse(JSONObject root) {
//		if (root==null) return null;
//		
//		JSONObject jo = null;
//		JSONArray dataArray = null;
    	List<Marker> markers=new ArrayList<Marker>();

//		try {
//			if(root.has("docs")) dataArray = root.getJSONArray("docs");
//			if (dataArray == null) return markers;
//				int top = Math.min(MAX, dataArray.length());
//				for (int i = 0; i < top; i++) {					
//					jo = dataArray.getJSONObject(i);
//					Marker ma = processJSONObject(jo);
//					if(ma!=null) markers.add(ma);
//				}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
		return markers;
	}
	
	// Not used
	private Marker processJSONObject(JSONObject jo) {
//		if (jo==null) return null;
//		
        Marker ma = null;
//        if (	jo.has("title") && 
//        		jo.has("lat") && 
//        		jo.has("lng") && 
//        		jo.has("elevation")
//        ) {
//        	try {
//        		ma = new IconMarker(
//        				jo.getString("title"),
//        				jo.getDouble("lat"),
//        				jo.getDouble("lng"),
//        				jo.getDouble("elevation"),
//        				Color.WHITE,
//        				icon);
//        	} catch (JSONException e) {
//        		e.printStackTrace();
//        	}
//        }
        return ma;
	}
	

    public List<Document> parseSearchResult(String url) {
        if (url == null)
            throw new NullPointerException();

        InputStream stream = null;
        stream = getHttpGETInputStream(url);
        if (stream == null)
            throw new NullPointerException();

        String string = null;
        string = getHttpInputString(stream);
        if (string == null)
            throw new NullPointerException();

        JSONObject json = null;
        try {
            json = new JSONObject(string);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (json == null)
            throw new NullPointerException();
        
        return parseSearchResult(json);
    }
}
