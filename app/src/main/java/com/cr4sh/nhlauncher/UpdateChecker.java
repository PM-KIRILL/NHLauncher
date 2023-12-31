package com.cr4sh.nhlauncher;

import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateChecker {

    private static final String TAG = "UpdateChecker";
    private static final String GITHUB_API_URL = "https://api.github.com/repos/cr4sh-me/NHLauncher/releases/latest";
    MainActivity mainActivity;

    public UpdateChecker(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void checkUpdateAsync(UpdateCheckListener listener) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(() -> {
            try {
                String latestVersion = getLatestAppVersion();
                UpdateCheckResult updateResult = compareVersions(latestVersion);
                listener.onUpdateCheckCompleted(updateResult);
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error checking for updates", e);
                listener.onUpdateCheckCompleted(new UpdateCheckResult(false, e.getMessage()));
            }
        });

        executor.shutdown();

        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                future.get(); // Wait for the task to complete before calling onUpdateCheckCompleted
            } catch (Exception e) {
                Log.e(TAG, "Error getting update result", e);
                listener.onUpdateCheckCompleted(new UpdateCheckResult(false, e.getMessage()));
            }
        });
    }


    private String getLatestAppVersion() throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(GITHUB_API_URL).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                assert response.body() != null;
                String responseBody = response.body().string();
                JSONObject json = new JSONObject(responseBody);
                return json.getString("tag_name");
            } else {

                int responseCode = response.code();
                if (responseCode == 403) {
                    // Handle 403 error (Forbidden)
                    Log.e(TAG, "Too many requests! Error 403");
                    throw new IOException("API limit exceed. Try again later!");
                } else {
                    // Handle other HTTP errors
                    Log.e(TAG, "Failed to retrieve app version. HTTP Code: " + responseCode);
                    throw new IOException("Failed to retrieve app version. HTTP Code: " + responseCode);
                }
            }
        } catch (UnknownHostException | SocketTimeoutException e) {
            Log.e(TAG, "Error checking for updates", e);
            throw new IOException(mainActivity.getResources().getString(R.string.check_internet_connection));
        }
    }

    private UpdateCheckResult compareVersions(String latestVersion) {
        String installedVersion = getInstalledVersion();

        if (installedVersion == null) {
            return new UpdateCheckResult(false, mainActivity.getResources().getString(R.string.something_fucked_up));
        }

        Log.d(TAG, "cURRENT: " + installedVersion);

        String[] installedParts = installedVersion.split("\\.");
        String[] latestParts = latestVersion.split("\\.");

        for (int i = 0; i < installedParts.length && i < latestParts.length; i++) {
            int installedNumber = Integer.parseInt(installedParts[i]);
            int latestNumber = Integer.parseInt(latestParts[i]);

            Log.d(TAG, "after: " + installedVersion);

            if (installedNumber < latestNumber) {
                Log.d(TAG, latestVersion + " < " + installedVersion);
                return new UpdateCheckResult(true, mainActivity.getResources().getString(R.string.update_avaiable) +
                        "\n" + mainActivity.getResources().getString(R.string.current_app_version) + installedVersion +
                        "\n" + mainActivity.getResources().getString(R.string.new_app_version) + latestVersion);
            } else if (installedNumber > latestNumber) {
                Log.d(TAG, latestVersion + " > " + installedVersion);
                return new UpdateCheckResult(false, mainActivity.getResources().getString(R.string.giga_chad));
            }
        }

        Log.d(TAG, latestVersion + " == " + installedVersion);
        return new UpdateCheckResult(false, mainActivity.getResources().getString(R.string.already_updated));
    }

    private String getInstalledVersion() {
        try {
            PackageInfo packageInfo = mainActivity.getPackageManager().getPackageInfo(mainActivity.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            Log.e(TAG, "Error getting installed version", e);
            return null;
        }
    }

    public interface UpdateCheckListener {
        void onUpdateCheckCompleted(UpdateCheckResult updateResult);
    }

    public record UpdateCheckResult(boolean isUpdateAvailable, String message) {
    }
}
