package mobi.pushapps.plugins;

import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

import mobi.pushapps.PushApps;
import mobi.pushapps.gcm.PAGcmListener;
import mobi.pushapps.models.PANotification;
import mobi.pushapps.tags.PATag;
import mobi.pushapps.tags.PATagsListener;
import mobi.pushapps.utils.PALogger;

import com.outbrain.OBSDK.Outbrain;
import com.outbrain.OBSDK.OutbrainException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PushAppsPlugin extends CordovaPlugin {

    public static final String TAG = "PushAppsPlugin";

    public static final String ACTION_ON_DEVICE_READY = "onDeviceReady";
    public static final String ACTION_REGISTER = "registerToPushNotifications";
    public static final String ACTION_GET_DEVICE_ID = "getDeviceId";
    public static final String ACTION_ENABLE_PUSH = "enablePush";
    public static final String ACTION_ENABLE_SOUND = "enableSound";
    public static final String ACTION_ENABLE_VIBRATE = "enableVibrate";
    public static final String ACTION_IS_PUSH_ENABLED = "isPushEnabled";
    public static final String ACTION_IS_SOUND_ENABLED = "isSoundEnabled";
    public static final String ACTION_IS_VIBRATE_ENABLED = "isVibrateEnabled";
    public static final String ACTION_TRACK_URL_VIEW = "trackUrlView";
    public static final String ACTION_GET_SDK_VERSION = "getSdkVersion";
    public static final String ACTION_ADD_TAG = "addTag";
    public static final String ACTION_REMOVE_TAG = "removeTag";
    public static final String ACTION_GET_TAGS = "getTags";

    private static final String PREFS_NAME = "pushappsdata";

    private Context getApplicationContext() {
        return this.cordova.getActivity().getApplicationContext();
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) {

        try {
            if (ACTION_ON_DEVICE_READY.equals(action)) {
                return internalOnDeviceReady(data, callbackContext);
            }
            if (ACTION_REGISTER.equals(action)) {
                return internalRegister(callbackContext);
            }
            if (ACTION_GET_DEVICE_ID.equals(action)) {
                return internalDeviceId(callbackContext);
            }
            if (ACTION_ENABLE_PUSH.equals(action)) {
                return internalEnablePush(data);
            }
            if (ACTION_ENABLE_SOUND.equals(action)) {
                return internalEnableSound(data);
            }
            if (ACTION_ENABLE_VIBRATE.equals(action)) {
                return internalEnableVibrate(data);
            }
            if (ACTION_IS_PUSH_ENABLED.equals(action)) {
                return internalIsPushEnabled(callbackContext);
            }
            if (ACTION_IS_SOUND_ENABLED.equals(action)) {
                return internalIsSoundEnabled(callbackContext);
            }
            if (ACTION_IS_VIBRATE_ENABLED.equals(action)) {
                return internalIsVibrateEnabled(callbackContext);
            }
            if (ACTION_TRACK_URL_VIEW.equals(action)) {
                return internalTrackUrlView(callbackContext, data);
            }
            if (ACTION_GET_SDK_VERSION.equals(action)) {
                return internalGetSdkVersion(callbackContext);
            }
            if (ACTION_ADD_TAG.equals(action)) {
                return internalAddTag(data);
            }
            if (ACTION_REMOVE_TAG.equals(action)) {
                return internalRemoveTag(data);
            }
            if (ACTION_GET_TAGS.equals(action)) {
                return internalGetTags(callbackContext);
            }
            callbackContext.error("Invalid action");
            return false;
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
            return false;
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        checkIntentExtras(intent);
    }

    private void checkIntentExtras(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            boolean containsPushApps = PANotification.bundleContainsPANotification(bundle);
            if (containsPushApps) {
                String notificationId = intent.getExtras().getString("n_id");

                SharedPreferences appSharedPrefs = getApplicationContext().getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
                Editor prefsEditor = appSharedPrefs.edit();
                String lastPushRead = appSharedPrefs.getString(
                        "LastPushMessageRead", "");

                if (!lastPushRead.equals(notificationId)) {
                    prefsEditor
                            .putString("LastPushMessageRead", notificationId);
                    prefsEditor.commit();
                    Bundle params = intent.getExtras();
                    internalOnMessage(getJSONStringFromBundle(params));
                }

            }
        }

    }

    private void internalOnMessage(JSONObject message) {
        String newString = message.toString();
        final String jsStatement = String.format("PushNotification.messageClicked('%s');", newString);
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:" + jsStatement);
            }
        });
    }

    // Utility function. convert bundle into JSONObject
    private static JSONObject getJSONStringFromBundle(Bundle bundle) {
        JSONObject json = new JSONObject();
        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            try {
                if (key.startsWith("google.")) {
                    continue;
                } else if (key.equals("pa")) {
                    continue;
                } else if (key.equals("PAArticle")) {
                    continue;
                } else {
                    json.put(key, bundle.get(key));
                }
            } catch(JSONException e) {
                return new JSONObject();
            }
        }
        return json;
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // Internal methods
    private boolean internalOnDeviceReady(JSONArray data, CallbackContext callbackContext) {
        PALogger.setLogsEnabled(true);
        try {
            String obKey = data.getString(1);
            if (obKey != null) {
                boolean testMode = false;
                try {
                    testMode = data.getBoolean(2)
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Outbrain.register(cordova.getActivity().getApplicationContext(), obKey);
                Outbrain.setTestMode(testMode);
            }
        }
        catch (OutbrainException ex) {
            // handle exception
        } catch (JSONException e) {
            e.printStackTrace();
        }
        checkIntentExtras(cordova.getActivity().getIntent());
        return true;
    }

    private boolean internalRegister(final CallbackContext callbackContext) {
        PushApps.setGcmListener(getApplicationContext(), new PAGcmListener() {
            @Override
            public void onGcmRegistrationFinished(String token, String errorMessage) {
                if (errorMessage != null) {
                    callbackContext.error(errorMessage);
                } else {
                    callbackContext.success(token);
                }
            }

            @Override
            public void onGcmUnRegistrationFinished(String errorMessage) {
                callbackContext.error(errorMessage);
            }
        });
        PushApps.register(getApplicationContext());
        checkIntentExtras(this.cordova.getActivity().getIntent());
        return true;
    }

    private boolean internalDeviceId(CallbackContext callbackContext) {
        String result = PushApps.getDeviceId(getApplicationContext());
        callbackContext.success(result);
        return true;
    }

    private boolean internalEnablePush(JSONArray data) {
        boolean enabled = false;
        try {
            enabled = data.getBoolean(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        PushApps.enablePush(getApplicationContext(), enabled);
        return true;
    }

    private boolean internalEnableSound(JSONArray data) {
        boolean enabled = false;
        try {
            enabled = data.getBoolean(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        PushApps.enableSound(getApplicationContext(), enabled);
        return true;
    }

    private boolean internalEnableVibrate(JSONArray data) {
        boolean enabled = false;
        try {
            enabled = data.getBoolean(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        PushApps.enableVibrate(getApplicationContext(), enabled);
        return true;
    }

    private boolean internalIsPushEnabled(CallbackContext callbackContext) {
        String result = String.valueOf(PushApps.isPushEnabled(getApplicationContext()));
        callbackContext.success(result);
        return true;
    }

    private boolean internalIsSoundEnabled(CallbackContext callbackContext) {
        String result = String.valueOf(PushApps.isSoundEnabled(getApplicationContext()));
        callbackContext.success(result);
        return true;
    }

    private boolean internalIsVibrateEnabled(CallbackContext callbackContext) {
        String result = String.valueOf(PushApps.isVibrateEnabled(getApplicationContext()));
        callbackContext.success(result);
        return true;
    }

    private boolean internalTrackUrlView(CallbackContext callbackContext, JSONArray data) {
        String articleUrl;
        try {
            articleUrl = data.getString(0);
        } catch (JSONException e) {
            articleUrl = null;
            e.printStackTrace();
        }
        if (articleUrl != null && articleUrl.length() > 0) {
            PushApps.trackUrlView(getApplicationContext(), articleUrl);
            callbackContext.success();
        } else {
            callbackContext.error("URL can't be empty.");
        }
        return true;
    }

    private boolean internalGetSdkVersion(CallbackContext callbackContext) {
        String result = PushApps.getSdkVersion();
        callbackContext.success(result);
        return true;
    }

    private boolean internalAddTag(JSONArray data) {
        try {
            String name = data.getString(0);
            String value = data.getString(1);
            boolean boolValue = Boolean.parseBoolean(value);

            PATag tag = new PATag(name, boolValue);
            PushApps.addTag(getApplicationContext(), tag);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean internalRemoveTag(JSONArray data) {
        try {
            String name = data.getString(0);
            PushApps.removeTag(getApplicationContext(), name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean internalGetTags(final CallbackContext callbackContext) {
        PushApps.getTags(getApplicationContext(), new PATagsListener() {
            @Override
            public void onTagsReceived(List<PATag> list) {
                JSONArray array = new JSONArray();
                for (int i = 0; i < list.size(); i++) {
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("name", list.get(i).getName());
                        obj.put("value", list.get(i).getValue());
                        array.put(obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                callbackContext.success(array);
            }
        });
        return true;
    }
}