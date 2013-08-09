package com.machine_cycle.burp_client_app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */

public class BurpIntentService extends IntentService {
	private static final String TAG = "BurpIntentService";
	
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
	
	String burp(String filesPath, String... args) {
		List<String> command = new ArrayList<String>(Arrays.asList(filesPath + "/burp", "-c", filesPath + "/burp.conf"));
	    command.addAll(Arrays.asList(args));
	    String output = exec(command.toArray(new String[0]));
	    Log.d(TAG, output);
	    return output;
	}

	/**
	 * Starts this service to perform action Backup with the given parameters. If
	 * the service is already performing a task this action will be queued.
	 * 
	 * @see IntentService
	 */
	public static void startActionBackup(Context context) {
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
	// TODO: Customize helper method
	public static void startActionTimedBackup(Context context) {
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
	// TODO: Customize helper method
	public static void startActionRestore(Context context, String backup_number,
			String directory, String regex) {
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
	// TODO: Customize helper method
	public static void startActionList(Context context, String backup_number,
			String directory, String regex) {
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
	// TODO: Customize helper method
	public static void startActionLongList(Context context, String backup_number,
			String directory, String regex) {
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
	// TODO: Customize helper method
	public static void startActionVerify(Context context) {
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
	// TODO: Customize helper method
	public static void startActionEstimate(Context context) {
		Intent intent = new Intent(context, BurpIntentService.class);
		intent.setAction(ACTION_ESTIMATE);
		context.startService(intent);
	}

	public BurpIntentService() {
		super("BurpIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			final String action = intent.getAction();
			if (ACTION_BACKUP.equals(action)) {
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

	/**
	 * Handle action Backup in the provided background thread with the provided
	 * parameters.
	 */
	private void handleActionBackup() {
		burp(getFilesDir().getPath(), "-a", "b");
	}

	/**
	 * Handle action Timed Backup in the provided background thread with the provided
	 * parameters.
	 */
	private void handleActionTimedBackup() {
		burp(getFilesDir().getPath(), "-a", "t");
	}

	/**
	 * Handle action Restore in the provided background thread with the provided
	 * parameters.
	 */
	private void handleActionRestore(String backup_number, String directory, String regex) {
		// TODO: Handle action Restore
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * Handle action List in the provided background thread with the provided
	 * parameters.
	 */
	private void handleActionList(String backup_number, String directory, String regex) {
		// TODO: Handle action List
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * Handle action Long List in the provided background thread with the provided
	 * parameters.
	 */
	private void handleActionLongList(String backup_number, String directory, String regex) {
		// TODO: Handle action Long List
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * Handle action Verify in the provided background thread with the provided
	 * parameters.
	 */
	private void handleActionVerify() {
		// TODO: Handle action Verify
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * Handle action Estimate in the provided background thread with the provided
	 * parameters.
	 */
	private void handleActionEstimate() {
		burp(getFilesDir().getPath(), "-a", "e");
	}

}
