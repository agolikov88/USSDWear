package com.agolikov.wearbalance.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.Settings;

/**
 * Created by agolikov on 04/10/14.
 */
public class BalanceCheckUtils {

    public static final String SHARED_PREFS_NAME = "balance_checker_preferences";
    public static final String USSD_NUMBER_PREFERENCE = "ussd_number_preference";

    private static final String USSD_READER_SERVICE_RUNNING_PREFERENCE = "ussd_reader_service_running_preference";
    private static final String FIRST_LAUNCH_PREFERENCE = "first_launch_preference";
    private static final String BALANCE_CALLER_PREFERENCE = "balance_caller_preference";
    private static final String SCREEN_OFF_DEFAULT_PREFERENCE = "screen_off_default_preference";

    private static final int MINIMUM_SCREEN_OFF_TIMEOUT = 1000;
    private static final int DEFAULT_SCREEN_OFF_TIMEOUT = 120000;

    public static final String BALANCE_CHECK_INTENT_ACTION = "balance_check_action";
    public static final String USSD_REQUEST_TEST_INTENT_ACTION = "ussd_test_action";
    public static final String BALANCE_STRING_INTENT_EXTRA = "balance_string_extra";

    public static final String BALANCE_CHECKER_WEAR_PATH = "/balance-checker-wear";

    public static final String DEFAULT_USSD_REQUEST = "*100#";

    public static void checkBalance(Context context, String ussdCode) {
        setBalanceCallerFlag(context, true);
        String callString = ussdCode;
        callString = callString.substring(0, callString.length() - 1);
        callString = callString + Uri.encode("#");
        Intent checkBalanceIntent = new Intent(android.content.Intent.ACTION_CALL, Uri.parse("tel:" + callString));
        context.startActivity(checkBalanceIntent);
    }

    public static void setBalanceCallerFlag(Context context, boolean calledByApp) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(BALANCE_CALLER_PREFERENCE, calledByApp);
        editor.commit();
    }

    public static boolean ifCalledByApp(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        return prefs.getBoolean(BALANCE_CALLER_PREFERENCE, false);
    }

    public static void setUssdReaderServiceRunningFlag(Context context, boolean serviceRunning) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(USSD_READER_SERVICE_RUNNING_PREFERENCE, serviceRunning);
        editor.commit();
    }

    public static boolean ifUssdServiceRunning(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        return prefs.getBoolean(USSD_READER_SERVICE_RUNNING_PREFERENCE, false);
    }

    public static void setFirstLaunchFlag(Context context, boolean firstLaunch) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(FIRST_LAUNCH_PREFERENCE, firstLaunch);
        editor.commit();
    }

    public static boolean ifFirstLaunch(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        return prefs.getBoolean(FIRST_LAUNCH_PREFERENCE, true);
    }

    public static void setScreenOffTimeoutToMinimum(Context context) {
        //save previous value
        try {
            int currentScreenOffTimeoutSetting = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
            if(currentScreenOffTimeoutSetting != MINIMUM_SCREEN_OFF_TIMEOUT) {
                SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                editor.putInt(SCREEN_OFF_DEFAULT_PREFERENCE, currentScreenOffTimeoutSetting);
                editor.commit();
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, MINIMUM_SCREEN_OFF_TIMEOUT);    //1 ms
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void restoreScreenOffTimeoutToPrevious(Context context) {
        //read previous value
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, prefs.getInt(SCREEN_OFF_DEFAULT_PREFERENCE, DEFAULT_SCREEN_OFF_TIMEOUT));
    }

}
