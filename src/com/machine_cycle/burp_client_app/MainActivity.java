package com.machine_cycle.burp_client_app;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.res.AssetManager;
import android.util.Log;
import android.content.Intent;
import android.content.SharedPreferences;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	private static final String ZIP_ME_NOT_EXT = ".zip-me-not.mp3";
	private static final String TEMPLATE_EXT = ".in";
	private String arch = "arm";
	private String burpVersion = "*unknown*";

	private String exec(String... command) {
		StringBuffer output = new StringBuffer();
		Process process = null;
		try {
			process = new ProcessBuilder()
			.command(command)
			.redirectErrorStream(true)
			.start();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			int read;
			char[] buffer = new char[4096];
			while ((read = reader.read(buffer)) > 0) {
				output.append(buffer, 0, read);
			}
			reader.close();
			process.waitFor();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		finally {
			if (process != null)
				process.destroy();
		}
		return output.toString();
	}

	private void createFileFromTemplate(InputStream in, FileOutputStream out) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String rline;
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		OutputStreamWriter osw = new OutputStreamWriter(out); 
		Pattern p = Pattern.compile("@([^@]*)@");
		try {
			while ((rline = br.readLine()) != null) {
				int idx = 0;
				String wline = "";
				Matcher m = p.matcher(rline);
				while (m.find()) {
					String property = m.group(1);
					String value;
					try {
						value = settings.getString(property, m.group(0));
					} catch (ClassCastException e) {
						boolean bValue = settings.getBoolean(property, false);
						boolean commentOut = property.endsWith("_comment_out");
						if (bValue) {
							if (commentOut) {
								value = "";
							} else {
								value = "1";
							}
						} else {
							if (commentOut) {
								value = "#";
							} else {
								value = "0";
							}
						}
					}
					if (value.startsWith("@")) {
						Log.w(TAG, "unknown property in template: " + value);
					}
					wline = wline + rline.substring(idx, m.start(0));
					wline = wline + value;
					idx = m.end(0);
				}
				wline = wline + rline.substring(idx);
				osw.write(wline +"\n");
			}
			osw.close();
		} catch (IOException e) {
			throw new RuntimeException(e);			
		}
	}

	private void extractBinaryAsset(InputStream in, FileOutputStream out) {
		try {
			int read;
			byte[] buffer = new byte[4096];
			while ((read = in.read(buffer)) > 0) {
				out.write(buffer, 0, read);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}	

	private String getAssetTargetFileName(String fileName) {
		if (fileName.endsWith(TEMPLATE_EXT)) {
			return fileName.substring(0, fileName.length() - TEMPLATE_EXT.length());
		}
		return fileName;
	}

	public void extractAsset(String fromPrefix, String toPrefix, String fileName) {
		String tgtFileName = getAssetTargetFileName(fileName);
		boolean template = fileName.endsWith(TEMPLATE_EXT);
		AssetManager assets = getAssets();
		try {
			InputStream in;
			try {
				in = assets.open(fromPrefix + fileName);
			}
			catch (IOException e) {
				// file not found: maybe this is a large (>1MB) asset that
				// was renamed with an mp3 extension to prevent it from being compressed
				// so that it can be opened on android 2.3
				in = assets.open(fromPrefix + fileName + ZIP_ME_NOT_EXT);
			}			
			FileOutputStream out = new FileOutputStream(toPrefix + tgtFileName);
			if (template) {
				createFileFromTemplate(in, out);
			} else {
				extractBinaryAsset(in, out);
			}
			out.close();
			in.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void extractExecutable(String fromPrefix, String toPrefix, String fileName) {
		String tgtFileName = getAssetTargetFileName(fileName);
		extractAsset(fromPrefix, toPrefix, fileName);
		exec("/system/bin/chmod", "744", toPrefix + tgtFileName);
	}


	private void extractAllAssets() {
		String fromPrefix = arch + "/";
		String toPrefix = getFilesDir().getPath() + "/"; 
		extractExecutable(fromPrefix, toPrefix, "burp");
		extractExecutable(fromPrefix, toPrefix, "openssl");
		extractExecutable("", toPrefix, "burp_ca.in");
		updateConfig();

		burpVersion = exec(toPrefix + "burp", "-v");
		Log.i(TAG, "Using BURP version " + burpVersion);
	}

	private void updateConfig() {
		String toPrefix = getFilesDir().getPath() + "/"; 
		extractAsset("", toPrefix, "burp.conf.in");		
	}


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
			Log.w(TAG, "Unknown architecture. Guessing \"arm\".");
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
		
		Thread thread = new Thread(new Runnable() {
			public void run() {
				extractAllAssets();
			}
		});
		thread.start();		;
	}

	public boolean onClickView(View target) {
		switch (target.getId()) {
		case R.id.button_backup:
			Log.i(TAG, exec(getFilesDir().getPath() + "/burp", "-c", getFilesDir().getPath() + "/burp.conf", "-a", "b"));
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
			startActivityForResult(i, R.id.action_settings);
			break;

		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case R.id.action_settings:
			updateConfig();
			break;

		}
	}

}
