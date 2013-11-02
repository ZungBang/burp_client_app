package com.machine_cycle.burp_client_app;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */

public class BurpIntentService extends IntentService {
	private static final String TAG = "BurpIntentService";
	private static final String ZIP_ME_NOT_EXT = ".zip-me-not.mp3";
	private static final String TEMPLATE_EXT = ".in";
	
	private static final String ACTION_INIT = "com.machine_cycle.burp_client_app.action.INIT";	
	private static final String ACTION_BACKUP = "com.machine_cycle.burp_client_app.action.BACKUP";
	private static final String ACTION_TIMED_BACKUP = "com.machine_cycle.burp_client_app.action.TIMED_BACKUP";
	private static final String ACTION_RESTORE = "com.machine_cycle.burp_client_app.action.RESTORE";
	private static final String ACTION_LIST = "com.machine_cycle.burp_client_app.action.LIST";
	private static final String ACTION_LONG_LIST = "com.machine_cycle.burp_client_app.action.LONG_LIST";
	private static final String ACTION_VERIFY = "com.machine_cycle.burp_client_app.action.VERIFY";
	private static final String ACTION_ESTIMATE = "com.machine_cycle.burp_client_app.action.ESTIMATE";

	private static final String EXTRA_PARAM_BACKUP_NUMBER = "com.machine_cycle.burp_client_app.extra.BACKUP_NUMBER";
	private static final String EXTRA_PARAM_DIRECTORY = "com.machine_cycle.burp_client_app.extra.DIRECTORY";
	private static final String EXTRA_PARAM_REGEX = "com.machine_cycle.burp_client_app.extra.REGEX";
	
	public static final String DEFAULT_ARCH = "arm";
	public static String burpVersion = "*unknown*";
	
	private static volatile PowerManager.WakeLock wakeLock = null;

	synchronized private static PowerManager.WakeLock getLock(Context context) {
		if (wakeLock == null) {
			PowerManager mgr=
					(PowerManager)context.getSystemService(Context.POWER_SERVICE);
			wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,  context.getPackageName());
			wakeLock.setReferenceCounted(true);
		}
		return wakeLock;
	}


	public static String exec(String... command) {
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
	
	/**
	 * Starts this service to perform action Init with the given parameters. If
	 * the service is already performing a task this action will be queued.
	 * 
	 * @see IntentService
	 */
	public static void startActionInit(Context context) {
		getLock(context.getApplicationContext()).acquire();
		Intent intent = new Intent(context, BurpIntentService.class);
		intent.setAction(ACTION_INIT);
		context.startService(intent);
	}

	/**
	 * Starts this service to perform action Backup with the given parameters. If
	 * the service is already performing a task this action will be queued.
	 * 
	 * @see IntentService
	 */
	public static void startActionBackup(Context context) {
		getLock(context.getApplicationContext()).acquire();
		Intent intent = new Intent(context, BurpIntentService.class);
		intent.setAction(ACTION_BACKUP);
		context.startService(intent);
	}

	/**
	 * Starts this service to perform action Timed Backup with the given parameters. If
	 * the service is already performing a task this action will be queued.
	 * 
	 * @see IntentService
	 */
	public static void startActionTimedBackup(Context context) {
		getLock(context.getApplicationContext()).acquire();
		Intent intent = new Intent(context, BurpIntentService.class);
		intent.setAction(ACTION_TIMED_BACKUP);
		context.startService(intent);
	}

	/**
	 * Starts this service to perform action Restore with the given parameters. If
	 * the service is already performing a task this action will be queued.
	 * 
	 * @see IntentService
	 */
	public static void startActionRestore(Context context, String backup_number,
			String directory, String regex) {
		getLock(context.getApplicationContext()).acquire();
		Intent intent = new Intent(context, BurpIntentService.class);
		intent.setAction(ACTION_RESTORE);
		intent.putExtra(EXTRA_PARAM_BACKUP_NUMBER, backup_number);
		intent.putExtra(EXTRA_PARAM_DIRECTORY, directory);
		intent.putExtra(EXTRA_PARAM_REGEX, regex);
		context.startService(intent);
	}

	/**
	 * Starts this service to perform action List with the given parameters. If
	 * the service is already performing a task this action will be queued.
	 * 
	 * @see IntentService
	 */
	public static void startActionList(Context context, String backup_number,
			String directory, String regex) {
		getLock(context.getApplicationContext()).acquire();
		Intent intent = new Intent(context, BurpIntentService.class);
		intent.setAction(ACTION_LIST);
		intent.putExtra(EXTRA_PARAM_BACKUP_NUMBER, backup_number);
		intent.putExtra(EXTRA_PARAM_DIRECTORY, directory);
		intent.putExtra(EXTRA_PARAM_REGEX, regex);
		context.startService(intent);
	}

	/**
	 * Starts this service to perform action Long List with the given parameters. If
	 * the service is already performing a task this action will be queued.
	 * 
	 * @see IntentService
	 */
	public static void startActionLongList(Context context, String backup_number,
			String directory, String regex) {
		getLock(context.getApplicationContext()).acquire();
		Intent intent = new Intent(context, BurpIntentService.class);
		intent.setAction(ACTION_LONG_LIST);
		intent.putExtra(EXTRA_PARAM_BACKUP_NUMBER, backup_number);
		intent.putExtra(EXTRA_PARAM_DIRECTORY, directory);
		intent.putExtra(EXTRA_PARAM_REGEX, regex);
		context.startService(intent);
	}

	/**
	 * Starts this service to perform action Verify with the given parameters. If
	 * the service is already performing a task this action will be queued.
	 * 
	 * @see IntentService
	 */
	public static void startActionVerify(Context context) {
		getLock(context.getApplicationContext()).acquire();
		Intent intent = new Intent(context, BurpIntentService.class);
		intent.setAction(ACTION_VERIFY);
		context.startService(intent);
	}

	/**
	 * Starts this service to perform action Estimate with the given parameters. If
	 * the service is already performing a task this action will be queued.
	 * 
	 * @see IntentService
	 */
	public static void startActionEstimate(Context context) {
		getLock(context.getApplicationContext()).acquire();
		Intent intent = new Intent(context, BurpIntentService.class);
		intent.setAction(ACTION_ESTIMATE);
		context.startService(intent);
	}
	
	private static String byteArray2Hex(final byte[] hash) {
	    Formatter formatter = new Formatter();
	    for (byte b : hash) {
	        formatter.format("%02x", b);
	    }
	    String hex = formatter.toString();
	    formatter.close();
	    return hex;
	}		

	
	private static String readFileToString(String path) throws IOException {
		// based on code from 
		// http://www.journaldev.com/875/java-read-file-to-string-example
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        final String ls = System.getProperty("line.separator");
        while((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }
        //delete the last ls
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        reader.close();
        return stringBuilder.toString();
    }
	
	private static String computeChecksum(String path) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	    FileInputStream fis;
		try {
			fis = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			return null;
		}
		
		try {
			byte[] buffer = new byte[1024];	 
			int nread = 0; 
			while ((nread = fis.read(buffer)) != -1) {
				md.update(buffer, 0, nread);
			}
			fis.close();
		} catch (IOException e) {
			return null;
		}
	 
	    byte[] mdbytes = md.digest();
	    return byteArray2Hex(mdbytes);
	}
	
	private static boolean isChecksumValid(String path) {
		String hash = null;
		try {
			hash = readFileToString(path + ".sha1sum");
			Log.d(TAG, hash + " " + path + ".sha1sum");
		} catch (IOException e) {
			return false;
		}
		String actual_hash = computeChecksum(path);
		Log.d(TAG, actual_hash + " " + path);
		return hash.equals(actual_hash);
	}
	
	
	public BurpIntentService() {
		super("BurpIntentService");
		setIntentRedelivery(true);
	}

	private void createFileFromTemplate(InputStream in, String tgtPath) {
		// replace strings in input that look like this @settings_property_name@
		// with the corresponding settings property value
		// - string properties are substituted as is, 
		// - booleans are replaced with a "0"/"1"
		// - booleans whose property name ends with "_comment_out" are replaced with "#"
		//   if false or are removed
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String rline;
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		Pattern p = Pattern.compile("@([^@]*)@");
		try {
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(tgtPath)); 
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

	private void extractBinaryAsset(InputStream in, String tgtPath) {
		try {
			// check if file exists and its checksum is correct
			if (isChecksumValid(tgtPath)) {
				Log.d(TAG, "Asset already extracted -- " + tgtPath);
				return;
			}
			// extract file from assets and compute its checksum
			MessageDigest md;
			try {
				md = MessageDigest.getInstance("SHA-1");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				return;
			}
			FileOutputStream out = new FileOutputStream(tgtPath);
			int nread;
			byte[] buffer = new byte[4096];
			while ((nread = in.read(buffer)) > 0) {
				md.update(buffer, 0, nread);
				out.write(buffer, 0, nread);
			}
			out.close();
			// write checksum
		    byte[] mdbytes = md.digest();
		    String hash = byteArray2Hex(mdbytes);
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(tgtPath + ".sha1sum"));
			osw.write(hash);
			osw.close();
			// check if extraction succeeded
			if (!isChecksumValid(tgtPath))
				throw new RuntimeException("Failed to extract " + tgtPath);
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
			String tgtPath = toPrefix + tgtFileName;
			if (template) {
				createFileFromTemplate(in, tgtPath);
			} else {
				extractBinaryAsset(in, tgtPath);
			}
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


	private void updateConfig() {
		String toPrefix = getFilesDir().getPath() + "/"; 
		extractAsset("", toPrefix, "burp.conf.in");		
	}
	
	private void extractAllAssets() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String arch = settings.getString("arch", DEFAULT_ARCH);
		String fromPrefix = arch + "/";
		String toPrefix = getFilesDir().getPath() + "/"; 
		extractExecutable(fromPrefix, toPrefix, "burp");
		extractExecutable(fromPrefix, toPrefix, "openssl");
		extractExecutable("", toPrefix, "burp_ca.in");
		updateConfig();

		burpVersion = exec(toPrefix + "burp", "-v");
		Log.i(TAG, "Using BURP version " + burpVersion);
	}

	String burp(String... args) {
	    extractAllAssets();
		String filesPath = getFilesDir().getPath();
		List<String> command = new ArrayList<String>(Arrays.asList(filesPath + "/burp", "-c", filesPath + "/burp.conf"));
	    command.addAll(Arrays.asList(args));
	    String output = exec(command.toArray(new String[0]));
	    Log.d(TAG, output);
	    return output;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		PowerManager.WakeLock lock = getLock(this.getApplicationContext());
		if (!lock.isHeld() || (flags & START_FLAG_REDELIVERY) != 0) {
			lock.acquire();
		}
		super.onStartCommand(intent, flags, startId);
		return START_REDELIVER_INTENT;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			if (intent != null) {
				final String action = intent.getAction();
				if (ACTION_INIT.equals(action)) {
					handleActionInit();
				} else if (ACTION_BACKUP.equals(action)) {
					handleActionBackup();
				} else if (ACTION_TIMED_BACKUP.equals(action)) {
					handleActionTimedBackup();
				} else if (ACTION_RESTORE.equals(action)) {
					final String backup_number = intent.getStringExtra(EXTRA_PARAM_BACKUP_NUMBER);
					final String directory = intent.getStringExtra(EXTRA_PARAM_DIRECTORY);
					final String regex = intent.getStringExtra(EXTRA_PARAM_REGEX);
					handleActionRestore(backup_number, directory, regex);
				} else if (ACTION_LIST.equals(action)) {
					final String backup_number = intent.getStringExtra(EXTRA_PARAM_BACKUP_NUMBER);
					final String directory = intent.getStringExtra(EXTRA_PARAM_DIRECTORY);
					final String regex = intent.getStringExtra(EXTRA_PARAM_REGEX);
					handleActionList(backup_number, directory, regex);
				} else if (ACTION_LONG_LIST.equals(action)) {
					final String backup_number = intent.getStringExtra(EXTRA_PARAM_BACKUP_NUMBER);
					final String directory = intent.getStringExtra(EXTRA_PARAM_DIRECTORY);
					final String regex = intent.getStringExtra(EXTRA_PARAM_REGEX);
					handleActionLongList(backup_number, directory, regex);
				} else if (ACTION_VERIFY.equals(action)) {
					handleActionVerify();
				} else if (ACTION_ESTIMATE.equals(action)) {
					handleActionEstimate();
				}
			}
		}
		finally {
			PowerManager.WakeLock lock = getLock(this.getApplicationContext());		        
			if (lock.isHeld()) {
				lock.release();
			}		    
		}
	}


	/**
	 * Handle action Init in the provided background thread with the provided
	 * parameters.
	 */
	private void handleActionInit() {
		extractAllAssets();
	}

	/**
	 * Handle action Backup in the provided background thread with the provided
	 * parameters.
	 */
	private void handleActionBackup() {
		burp("-a", "b");
	}

	/**
	 * Handle action Timed Backup in the provided background thread with the provided
	 * parameters.
	 */
	private void handleActionTimedBackup() {
		burp("-a", "t");
	}

	/**
	 * Handle action Restore in the provided background thread with the provided
	 * parameters.
	 */
	private void handleActionRestore(String backup_number, String directory, String regex) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * Handle action List in the provided background thread with the provided
	 * parameters.
	 */
	private void handleActionList(String backup_number, String directory, String regex) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * Handle action Long List in the provided background thread with the provided
	 * parameters.
	 */
	private void handleActionLongList(String backup_number, String directory, String regex) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * Handle action Verify in the provided background thread with the provided
	 * parameters.
	 */
	private void handleActionVerify() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * Handle action Estimate in the provided background thread with the provided
	 * parameters.
	 */
	private void handleActionEstimate() {
		burp("-a", "e");
	}

}
