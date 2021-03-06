package com.yetwish.libs.distance.altbeacon;

import com.yetwish.libs.BuildConfig;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;




/**
 * Created by dyoung on 9/12/14.
 */
@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class ModelSpecificDistanceUpdater extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "ModelSpecificDistanceUpdater";
    private Context mContext;
    private DistanceConfigFetcher mDistanceConfigFetcher;
    private CompletionHandler mCompletionHandler;
    private static final String VERSION_NAME = "1.0";
    
    @Override
    protected Void doInBackground(Void... params) {
        mDistanceConfigFetcher.request();
        if (mCompletionHandler != null) {
            mCompletionHandler.onComplete(mDistanceConfigFetcher.getResponseString(), mDistanceConfigFetcher.getException(), mDistanceConfigFetcher.getResponseCode());
        }
        return null;
    }

    protected void onPostExecute() {
    }

    public ModelSpecificDistanceUpdater(Context context, String urlString, CompletionHandler completionHandler) {
        mContext = context;
        mDistanceConfigFetcher = new DistanceConfigFetcher(urlString, getUserAgentString());
        mCompletionHandler = completionHandler;
    }

    private String getUserAgentString() {
        return "Android Beacon Library;"+getVersion()+";"+getPackage()+";"+getInstallId()+";"+getModel();
    }
    private String getPackage() {
        return mContext.getPackageName();
    }
    private String getModel() {
        return AndroidModel.forThisDevice().toString();
    }
    private String getInstallId() {
        return Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
    private String getVersion() {
        return VERSION_NAME;
    }

    interface CompletionHandler {
        public void onComplete(String body, Exception exception, int code);
    }

}
