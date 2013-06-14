package com.machine_cycle.burp_client_app;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.content.res.AssetManager;
import android.util.Log;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	private String arch = "arm";
	private String burpVersion = "*unknown*"; 
	
	// executes command
    private String exec(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();
            process.waitFor();
            return output.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

	private void extractAsset(String fromPrefix, String toPrefix, String fileName) {
		AssetManager assets = getAssets();
		try {
			InputStream in = assets.open(fromPrefix + fileName);
			FileOutputStream out = new FileOutputStream(toPrefix + fileName);
			int read;
			byte[] buffer = new byte[4096];
			while ((read = in.read(buffer)) > 0) {
				out.write(buffer, 0, read);
			}
			out.close();
			in.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void chmodExecutable(String filePath) {
		exec("/system/bin/chmod 744 " + filePath);
	}
	
	private void extractExecutable(String fromPrefix, String toPrefix, String fileName) {
		extractAsset(fromPrefix, toPrefix, fileName);
		chmodExecutable(toPrefix + fileName);
	}
	
	private void extractAllAssets() {
		String fromPrefix = arch + "/";
		String toPrefix = getFilesDir().getPath() + "/"; 
		extractExecutable(fromPrefix, toPrefix, "burp");
		extractExecutable(fromPrefix, toPrefix, "openssl");
		extractExecutable("", toPrefix, "burp_ca");
		
		burpVersion = exec(toPrefix + "burp -v");
		Log.i(TAG, "Using BURP version " + burpVersion);
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
		
		Thread thread = new Thread(new Runnable() {
			public void run() {
				extractAllAssets();
			}
		});
		thread.start();		;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
