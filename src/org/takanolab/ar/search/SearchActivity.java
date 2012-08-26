package org.takanolab.ar.search;

import java.util.ArrayList;
import java.util.List;
import android.widget.EditText;
import android.widget.LinearLayout;


import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.app.ListActivity;
import android.content.Intent;

import com.paar.ch9.MainActivity;
import com.paar.ch9.SearchDataSource;

import jp.androidgroup.nyartoolkit.NyARToolkitAndroidActivity;
import jp.androidgroup.nyartoolkit.R;

public class SearchActivity extends ListActivity {
	
	private static List<Document> documents = null;
	
	public static Integer[] mThumbIds = {
		R.drawable.cgs_s,
		R.drawable.marmot_s,
		R.drawable.elk_bull_s,
		R.drawable.machingunlily_s };

public static String[] mTitles = {
		"Elk",
		"Machingun Lily",
		"Marmot" 
		};
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
 
    }
    
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        return true;
    }

    Intent intent = null;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_to_cg:
            	// change to 3dcg activity
                intent = new Intent(SearchActivity.this, NyARToolkitAndroidActivity.class);
                startActivity(intent);
                break;
            case R.id.search_to_quest:
            	// change to quest activity
                intent = new Intent(SearchActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.exit:
                finish();
                break;
        }
        return true;
    }
    
    public void showResultList(View v) {
    	
    	//get search keyword from textbox
    	EditText editText = (EditText) findViewById(R.id.editText1);
    	SpannableStringBuilder query = (SpannableStringBuilder)editText.getText();
    	System.out.println(query.toString());
    	
    	
    	SearchDataSource search = new SearchDataSource(this.getResources());
    	search(search, query.toString());
		List<BindData> objects = new ArrayList<BindData>();
		for(int i = 0; i < documents.size(); i++) {
			BindData data =
				new BindData(documents.get(i).getName(), mThumbIds[i]);
			objects.add(data);
		}
		setListAdapter(new ImageAdapter(this, objects));

    }
    
    private static boolean search(SearchDataSource source, String query) {
		if (source==null) return false;
		
		String url = null;
		try {
			url = source.createRequestURL(0, 0, 0, 0, query);
			Log.v("SEARCH", "url="+url);
		} catch (NullPointerException e) {
			return false;
		}
    	
		
		try {
			documents = source.parseSearchResult(url);
		} catch (NullPointerException e) {
			return false;
		}

    	//ARData.addMarkers(markers);
    	return true;
    }
}
