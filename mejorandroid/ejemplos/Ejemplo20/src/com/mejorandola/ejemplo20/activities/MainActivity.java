package com.mejorandola.ejemplo20.activities;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

import com.google.gson.Gson;
import com.mejorandola.ejemplo20.R;
import com.mejorandola.ejemplo20.data.App;
import com.mejorandola.ejemplo20.fragments.MainFragment;
import com.mejorandola.ejemplo20.fragments.RoomListFragment;
import com.mejorandola.ejemplo20.fragments.TermsFragment;
import com.mejorandola.ejemplo20.fragments.dialogs.SelectMapTypeDialog;
import com.mejorandola.ejemplo20.models.Room;

import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends FragmentActivity implements  ListView.OnItemClickListener,
															   SelectMapTypeDialog.DialogListener {
	private Gson gson;
	private ListView drawer_list;
	private DrawerLayout drawer_layout;
	private ActionBarDrawerToggle drawer_toggle;
	private PullToRefreshAttacher pull_to_refresh_attacher;
	private final static String FILE_NAME = "favorites.txt";
	private final static String MAIN_FRAGMENT_TAG = "main";
	private final static String LIST_FRAGMENT_TAG = "list";
	private final static String TERMS_FRAGMENT_TAG = "terms";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		gson = new Gson();
		pull_to_refresh_attacher = PullToRefreshAttacher.get(this);

		drawer_list = (ListView) findViewById(R.id.left_drawer);
        drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ArrayAdapter<String> drawer_adapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, getResources().getStringArray(R.array.array_drawer_options));
        
        drawer_list.setAdapter(drawer_adapter);
        drawer_list.setOnItemClickListener(this);
        drawer_toggle = new ActionBarDrawerToggle(this, drawer_layout, R.drawable.ic_drawer, 
        										  R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }
        };
        
        drawer_layout.setDrawerListener(drawer_toggle);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);  

        selectItem(0);
        readFromInternalStorage();
	}
 	
	@Override
	public void onStop() {
		super.onStop();
		writeToInternalStorage();		
	}	
	
	public void writeToInternalStorage() {
		App app = (App)getApplicationContext();
		String json = gson.toJson(app.getFavoriteRooms());
		android.util.Log.i("ToFile",json);
		FileOutputStream fos;
		try {
			fos = openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
			fos.write(json.getBytes());
			fos.close();					
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public void readFromInternalStorage() {
        StringBuilder sb = new StringBuilder();
        
        try{
        	FileInputStream fis = openFileInput(FILE_NAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();            
        } catch(Exception ex){
            ex.printStackTrace();
        }
        String result = sb.toString();
        if (!result.equals("")) {
        	ArrayList<Room> fav = new ArrayList<Room>(Arrays.asList(gson.fromJson(result, Room[].class)));
        	android.util.Log.i("FromFile",fav.toString());
        }
	}
	
	
	public PullToRefreshAttacher getPullToRefreshAttacher() {
		return pull_to_refresh_attacher;
	}

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawer_toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawer_toggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawer_toggle.onOptionsItemSelected(item)) {
          return true;
        }

        return super.onOptionsItemSelected(item);
    }	
    
	private void selectItem(int position) {
		String tag;
		Fragment frag;
		
		if (getActionBar().getNavigationMode() == ActionBar.NAVIGATION_MODE_TABS) {
			SharedPreferences settings = getPreferences(0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putInt("tabindex", getActionBar().getSelectedNavigationIndex());
			editor.commit();	
		}
		
		if (position == 0) {
			tag = MAIN_FRAGMENT_TAG;
			frag = new MainFragment();
		} else if (position == 1) {
			tag = LIST_FRAGMENT_TAG;
			Bundle args = new Bundle();
			args.putInt(RoomListFragment.LIST_TYPE, RoomListFragment.FAVORITE_LIST);
			frag = new RoomListFragment();
			frag.setArguments(args);
			
			getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		} else {
			tag = TERMS_FRAGMENT_TAG;
			frag = new TermsFragment();
		} 

	    FragmentManager fragmentManager = getSupportFragmentManager();
	    fragmentManager.beginTransaction()
	                   .replace(R.id.main_content, frag, tag)
	                   .commit();
	    drawer_list.setItemChecked(position, true);	    
	    setTitle(drawer_list.getItemAtPosition(position).toString());
	    drawer_layout.closeDrawer(drawer_list);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		selectItem(position);		
	}

	@Override
	public void onSelectMapTypeDialogSelected(int type) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		MainFragment frag = (MainFragment) fragmentManager.findFragmentByTag(MAIN_FRAGMENT_TAG);
		frag.onSelectMapTypeDialogSelected(type);
	}

	@Override
	public int getCurrentMapType() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		MainFragment frag = (MainFragment) fragmentManager.findFragmentByTag(MAIN_FRAGMENT_TAG);
		return frag.getCurrentMapType();
	}	
}