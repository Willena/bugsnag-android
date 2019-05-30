package com.bugsnag.android;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observer;
import java.util.Queue;

/**
 * Used as the entry point for native code to allow proguard to obfuscate other areas if needed
 */
public class NativeInterface {

    /**
     * Static reference used if not using Bugsnag.init()
     */
    @SuppressLint("StaticFieldLeak")
    private static Client client;

    @NonNull
    private static Client getClient() {
        if (client != null) {
            return client;
        } else {
            return Bugsnag.getClient();
        }
    }

    /**
     * Caches a client instance for responding to future events
     */
    public static void setClient(@NonNull Client client) {
        if (NativeInterface.client == client) {
            return;
        }
        NativeInterface.client = client;
        configureClientObservers(client);
    }

    /**
     * Sets up observers for the NDK client
     * @param client the client
     */
    public static void configureClientObservers(@NonNull Client client) {
        try {
            String className = "com.bugsnag.android.ndk.NativeBridge";
            Class<?> clz = Class.forName(className);
            Observer observer = (Observer) clz.newInstance();
            client.addObserver(observer);
        } catch (ClassNotFoundException exception) {
            // ignore this one, will happen if the NDK plugin is not present
            Logger.info("Bugsnag NDK integration not available");
        } catch (InstantiationException exception) {
            Logger.warn("Failed to instantiate NDK observer", exception);
        } catch (IllegalAccessException exception) {
            Logger.warn("Could not access NDK observer", exception);
        }

        // Configure NDK components
        client.sendNativeSetupNotification();
    }

    @Nullable
    public static String getContext() {
        return getClient().getContext();
    }

    public static boolean getLoggingEnabled() {
        return Logger.getEnabled();
    }

    @NonNull
    public static String getNativeReportPath() {
        return getClient().appContext.getCacheDir().getAbsolutePath() + "/bugsnag-native/";
    }

    /**
     * Retrieve user data from the static Client instance as a Map
     */
    @NonNull
    @SuppressWarnings("unused")
    public static Map<String,String> getUserData() {
        HashMap<String, String> userData = new HashMap<>();
        User user = getClient().getUser();
        userData.put("id", user.getId());
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());

        return userData;
    }

    /**
     * Retrieve app data from the static Client instance as a Map
     */
    @NonNull
    @SuppressWarnings("unused")
    public static Map<String,Object> getAppData() {
        HashMap<String,Object> data = new HashMap<>();
        AppData source = getClient().getAppData();
        data.putAll(source.getAppData());
        data.putAll(source.getAppDataMetaData());
        return data;
    }

    /**
     * Retrieve device data from the static Client instance as a Map
     */
    @NonNull
    @SuppressWarnings("unused")
    public static Map<String,Object> getDeviceData() {
        HashMap<String,Object> deviceData = new HashMap<>();
        DeviceData source = getClient().getDeviceData();
        deviceData.putAll(source.getDeviceMetaData());
        deviceData.putAll(source.getDeviceData()); // wat
        return deviceData;
    }

    /**
     * Retrieve the CPU ABI(s) for the current device
     */
    @NonNull
    public static String[] getCpuAbi() {
        return getClient().deviceData.cpuAbi;
    }

    /**
     * Retrieves global metadata from the static Client instance as a Map
     */
    @NonNull
    public static Map<String, Object> getMetaData() {
        return new HashMap<>(getClient().getMetaData().store);
    }

    /**
     * Retrieves breadcrumbs from the static Client instance as a Map
     */
    @NonNull
    public static List<Breadcrumb> getBreadcrumbs() {
        Queue<Breadcrumb> store = getClient().breadcrumbs.store;
        return new ArrayList<>(store);
    }

    /**
     * Sets the user
     *
     * @param id id
     * @param email email
     * @param name name
     */
    @SuppressWarnings("unused")
    public static void setUser(@Nullable final String id,
                               @Nullable final String email,
                               @Nullable final String name) {
        Client client = getClient();
        client.setUserId(id);
        client.setUserEmail(email);
        client.setUserName(name);
    }

    /**
     * Leave a "breadcrumb" log message
     */
    public static void leaveBreadcrumb(@NonNull final String name,
                                       @NonNull final BreadcrumbType type) {
        getClient().leaveBreadcrumb(name, type, new HashMap<String, String>());
    }

    /**
     * Leaves a breadcrumb on the static client instance
     */
    public static void leaveBreadcrumb(@NonNull String name,
                                       @NonNull String type,
                                       @NonNull Map<String, String> metadata) {
        String typeName = type.toUpperCase(Locale.US);
        Map<String, String> map = metadata == null ? new HashMap<String, String>() : metadata;
        getClient().leaveBreadcrumb(name, BreadcrumbType.valueOf(typeName), map);
    }

    /**
     * Add metadata to subsequent exception reports
     */
    public static void addToTab(@NonNull final String tab,
                                @NonNull final String key,
                                @Nullable final Object value) {
        getClient().addToTab(tab, key, value);
    }

    /**
     * Set the client report release stage
     */
    public static void setReleaseStage(@Nullable final String stage) {
        getClient().setReleaseStage(stage);
    }

    /**
     * Return the client report release stage
     */
    @Nullable
    public static String getReleaseStage() {
        return getClient().getConfig().getReleaseStage();
    }

    /**
     * Return the client session endpoint
     */
    @NonNull
    public static String getSessionEndpoint() {
        return getClient().getConfig().getSessionEndpoint();
    }

    /**
     * Return the client report endpoint
     */
    @NonNull
    public static String getEndpoint() {
        return getClient().getConfig().getEndpoint();
    }

    /**
     * Set the client session endpoint
     */
    @SuppressWarnings("deprecation")
    public static void setSessionEndpoint(@NonNull final String endpoint) {
        getClient().getConfig().setSessionEndpoint(endpoint);
    }

    /**
     * Set the client report endpoint
     */
    @SuppressWarnings("deprecation")
    public static void setEndpoint(@NonNull final String endpoint) {
        getClient().getConfig().setEndpoint(endpoint);
    }

    /**
     * Set the client report context
     */
    public static void setContext(@Nullable final String context) {
        getClient().setContext(context);
    }

    /**
     * Set the client report app version
     */
    public static void setAppVersion(@NonNull final String version) {
        getClient().setAppVersion(version);
    }

    /**
     * Set the binary arch used in the application
     */
    public static void setBinaryArch(@NonNull final String binaryArch) {
        getClient().setBinaryArch(binaryArch);
    }

    /**
     * Return the client report app version
     */
    @NonNull
    public static String getAppVersion() {
        return getClient().getConfig().getAppVersion();
    }

    /**
     * Return which release stages notify
     */
    @Nullable
    public static String[] getNotifyReleaseStages() {
        return getClient().getConfig().getNotifyReleaseStages();
    }

    /**
     * Set which release stages notify
     */
    public static void setNotifyReleaseStages(@Nullable String[] notifyReleaseStages) {
        getClient().getConfig().setNotifyReleaseStages(notifyReleaseStages);
    }

    /**
     * Update the current session with a given start time, ID, and event counts
     */
    public static void registerSession(long startedAt, @Nullable String sessionId,
                                       int unhandledCount, int handledCount) {
        Client client = getClient();
        User user = client.getUser();
        Date startDate = startedAt > 0 ? new Date(startedAt) : null;
        client.getSessionTracker().registerExistingSession(startDate, sessionId, user,
                                                           unhandledCount, handledCount);
    }

    /**
     * Deliver a report, serialized as an event JSON payload.
     *
     * @param releaseStage The release stage in which the event was captured. Used to determin
     *                     whether the report should be discarded, based on configured release
     *                     stages
     */
    @SuppressWarnings("unused")
    public static void deliverReport(@Nullable String releaseStage, @NonNull String payload) {
        Client client = getClient();
        if (releaseStage == null
            || releaseStage.length() == 0
            || client.getConfig().shouldNotifyForReleaseStage(releaseStage)) {
            client.getErrorStore().enqueueContentForDelivery(payload);
            client.getErrorStore().flushAsync();
        }
    }

    /**
     * Notifies using the Android SDK
     *
     * @param name the error name
     * @param message the error message
     * @param severity the error severity
     * @param stacktrace a stacktrace
     */
    public static void notify(@NonNull final String name,
                              @NonNull final String message,
                              @NonNull final Severity severity,
                              @NonNull final StackTraceElement[] stacktrace) {

        getClient().notify(name, message, stacktrace, new Callback() {
            @Override
            public void beforeNotify(@NonNull Report report) {
                Error error = report.getError();
                if (error != null) {
                    if (severity != null) {
                        error.setSeverity(severity);
                    }
                    error.getExceptions().setExceptionType("c");
                }
            }
        });
    }

    static boolean is32bit() {
        String[] abis = NativeInterface.getCpuAbi();

        boolean is32bit = true;
        for (String abi : abis) {
            if (abi.contains("64")) {
                is32bit = false;
                break;
            }
        }
        return is32bit;
    }
}
