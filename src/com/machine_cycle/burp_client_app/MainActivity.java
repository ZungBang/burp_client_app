package com.machine_cycle.burp_client_app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	private String arch = BurpIntentService.DEFAULT_ARCH;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// detect architecture
		Log.d(TAG, "Build.CPU_ABI=" + Build.CPU_ABI);
		if (Build.CPU_ABI.startsWith("arm")) {
			arch = "arm";
		} 
		else if (Build.CPU_ABI.startsWith("mips")) {
			arch = "mips";
		}
		else if (Build.CPU_ABI.startsWith("x86")) {
			arch = "x86";
		}
		else {
			Log.w(TAG, "Unknown architecture. Guessing \"" + arch + "\".");
		}
		Log.d(TAG, "Selected architecture: \"" + arch + "\"");

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (!settings.getBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, false)) {
			PreferenceManager.setDefaultValues(this, R.xml.pref_general, true);
			PreferenceManager.setDefaultValues(this, R.xml.pref_connection, true);
			PreferenceManager.setDefaultValues(this, R.xml.pref_backup, true);
			PreferenceManager.setDefaultValues(this, R.xml.pref_restore, true);
			PreferenceManager.setDefaultValues(this, R.xml.pref_advanced, true);
		}
		
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("arch", arch);
		editor.putString("appdir", getFilesDir().getPath());
		editor.putString("external_storage", Environment.getExternalStorageDirectory().getPath());
		editor.putString("includes", "include=" + getFilesDir().getPath());
		editor.putString("excludes", "");		
		editor.commit();
		
		BurpIntentService.startActionInit(this);
	}

	public boolean onClickView(View target) {
		switch (target.getId()) {
		case R.id.button_backup:
			BurpIntentService.startActionBackup(target.getContext());
			break;
		}
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// This method is called once the menu is selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// We have only one menu option
		case R.id.action_settings:
			// Launch Preference activity
			Intent i = new Intent(MainActivity.this, SettingsActivity.class);
			startActivity(i);
			break;

		}
		return true;
	}

}
