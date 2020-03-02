package com.perflyst.twire.tasks;

import android.os.AsyncTask;

import com.perflyst.twire.service.MySSLSocketFactory;
import com.perflyst.twire.service.Service;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Sebastian Rask on 18-04-2016.
 */
public class FollowTask extends AsyncTask<String, Void, Boolean> {
    private String LOG_TAG = getClass().getSimpleName();
    private FollowResult callback;

    public FollowTask(FollowResult callback) {
        this.callback = callback;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        URL url;
        try {
            url = new URL(params[0]);
            HttpsURLConnection httpCon = (HttpsURLConnection) url.openConnection();
            httpCon.setSSLSocketFactory(new MySSLSocketFactory(httpCon.getSSLSocketFactory()));

            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("PUT");
            httpCon.setRequestProperty("Client-ID", Service.getApplicationClientID());
            httpCon.setRequestProperty("Accept", "application/vnd.twitchtv.v5+json");
            OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
            out.write("Resource content");
            out.close();
            int response = httpCon.getResponseCode();

            int FOLLOW_UNSUCCESFUL = 422;
            return response != FOLLOW_UNSUCCESFUL;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        callback.onTaskDone(aBoolean);
    }

    public interface FollowResult {
        void onTaskDone(Boolean result);
    }
}
