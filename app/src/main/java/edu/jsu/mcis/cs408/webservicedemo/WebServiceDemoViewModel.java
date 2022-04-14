package edu.jsu.mcis.cs408.webservicedemo;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;

public class WebServiceDemoViewModel extends ViewModel {

    private static final String TAG = "WebServiceDemoViewModel";

    private static final String GET_URL = "http://ec2-3-143-211-101.us-east-2.compute.amazonaws.com/CS408_SimpleChat/Chat";
    private static final String POST_URL = "http://ec2-3-143-211-101.us-east-2.compute.amazonaws.com/CS408_SimpleChat/Chat";

    private MutableLiveData<JSONObject> jsonData;
    private String message;

    private final ExecutorService requestThreadExecutor;
    private final Runnable httpGetRequestThread, httpPostRequestThread, httpDeleteRequestThread;
    private Future<?> pending;

    public WebServiceDemoViewModel() {

        requestThreadExecutor = Executors.newSingleThreadExecutor();

        httpGetRequestThread = new Runnable() {

            @Override
            public void run() {

                /* If a previous request is still pending, cancel it */

                if (pending != null) { pending.cancel(true); }

                /* Begin new request now, but don't wait for it */

                try {
                    pending = requestThreadExecutor.submit(new HTTPRequestTask("GET", GET_URL));
                }
                catch (Exception e) { Log.e(TAG, " Exception: ", e); }

            }

        };

        httpDeleteRequestThread = new Runnable() {

            @Override
            public void run() {

                /* If a previous request is still pending, cancel it */

                if (pending != null) { pending.cancel(true); }

                /* Begin new request now, but don't wait for it */

                try {
                    pending = requestThreadExecutor.submit(new HTTPRequestTask("DELETE", GET_URL));
                }
                catch (Exception e) { Log.e(TAG, " Exception: ", e); }

            }

        };



        httpPostRequestThread = new Runnable() {

            @Override
            public void run() {

                /* If a previous request is still pending, cancel it */

                if (pending != null) { pending.cancel(true); }

                /* Begin new request now, but don't wait for it */

                try {
                    pending = requestThreadExecutor.submit(new HTTPRequestTask("POST", POST_URL));
                }
                catch (Exception e) { Log.e(TAG, " Exception: ", e); }

            }

        };

    }

    public void setMessage(String msg){
        this.message = msg;
    }

    // Start GET Request (called from Activity)

    public void sendGetRequest() {
        httpGetRequestThread.run();
    }

    // Start POST Request (called from Activity)

    public void sendPostRequest() {
        httpPostRequestThread.run();
    }

    public void sendDeleteRequest(){httpDeleteRequestThread.run();}

    // Setter / Getter Methods for JSON LiveData

    private void setJsonData(JSONObject json) {
        this.getJsonData().postValue(json);
    }

    public MutableLiveData<JSONObject> getJsonData() {
        if (jsonData == null) {
            jsonData = new MutableLiveData<>();
        }
        return jsonData;
    }

    // Private Class for HTTP Request Threads

    private class HTTPRequestTask implements Runnable {

        private static final String TAG = "HTTPRequestTask";
        private final String method, urlString;

        HTTPRequestTask(String method, String urlString) {
            this.method = method;
            this.urlString = urlString;
        }




        @Override
        public void run() {
            JSONObject results = doRequest(urlString);
            setJsonData(results);
        }

        /* Create and Send Request */

        private JSONObject doRequest(String urlString) {

            StringBuilder r = new StringBuilder();
            String line;

            HttpURLConnection conn = null;
            JSONObject results = null;

            /* Log Request Data */

            try {

                /* Check if task has been interrupted */

                if (Thread.interrupted())
                    throw new InterruptedException();

                /* Create Request */

                URL url = new URL(urlString);

                conn = (HttpURLConnection)url.openConnection();

                conn.setReadTimeout(10000);    /* ten seconds */
                conn.setConnectTimeout(15000); /* fifteen seconds */
                conn.setRequestMethod(method);

                conn.setDoInput(true);

                /* Add Request Parameters (if any) */

                if (method.equals("POST") ) {

                    conn.setRequestProperty("Content-Type", "application/json; utf-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);

                    JSONObject a = new JSONObject();

                    // Write parameters to request body

                    a.put("name", "Sylveon");
//                    String message =
                    // get message somehow
                    a.put("message", message);
                    try(OutputStream os = conn.getOutputStream()) {
                        byte[] input = a.toString().getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }
                    Log.i("JSON", "closed out");

                }

                /* Send Request */

                conn.connect();

                /* Check if task has been interrupted */

                if (Thread.interrupted())
                    throw new InterruptedException();

                /* Get Reader for Results */

                int code = conn.getResponseCode();

                if (code == HttpsURLConnection.HTTP_OK || code == HttpsURLConnection.HTTP_CREATED) {

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    /* Read Response Into StringBuilder */

                    line = reader.readLine();
                    JSONObject json = new JSONObject(line);
                    Log.i("JSON", line);
                    r.append(line);

                }

                /* Check if task has been interrupted */

                if (Thread.interrupted())
                    throw new InterruptedException();

                /* Parse Response as JSON */

                results = new JSONObject(r.toString());

            }
            catch (Exception e) {
                Log.e(TAG, " Exception: ", e);
            }
            finally {
                if (conn != null) { conn.disconnect(); }
            }

            /* Finished; Log and Return Results */

            Log.d(TAG, " JSON: " + r.toString());

            return results;

        }
        private JSONObject doRequest(String urlString, String message) {

            StringBuilder r = new StringBuilder();
            String line;

            HttpURLConnection conn = null;
            JSONObject results = null;

            /* Log Request Data */

            try {

                /* Check if task has been interrupted */

                if (Thread.interrupted())
                    throw new InterruptedException();

                /* Create Request */

                URL url = new URL(urlString);

                conn = (HttpURLConnection)url.openConnection();

                conn.setReadTimeout(10000);    /* ten seconds */
                conn.setConnectTimeout(15000); /* fifteen seconds */
                conn.setRequestMethod(method);

                conn.setDoInput(true);

                /* Add Request Parameters (if any) */

                if (method.equals("POST") ) {

                    conn.setRequestProperty("Content-Type", "application/json; utf-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);

                    JSONObject a = new JSONObject();

                    // Write parameters to request body

                    a.put("name", "Sylveon");
//                    String message =
                    a.put("message", message);
                    try(OutputStream os = conn.getOutputStream()) {
                        byte[] input = a.toString().getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }
                    Log.i("JSON", "closed out");

                }

                /* Send Request */

                conn.connect();

                /* Check if task has been interrupted */

                if (Thread.interrupted())
                    throw new InterruptedException();

                /* Get Reader for Results */

                int code = conn.getResponseCode();

                if (code == HttpsURLConnection.HTTP_OK || code == HttpsURLConnection.HTTP_CREATED) {

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    /* Read Response Into StringBuilder */

                    do {
                        line = reader.readLine();
                        if (line != null)
                            r.append(line);
                    }
                    while (line != null);

                }

                /* Check if task has been interrupted */

                if (Thread.interrupted())
                    throw new InterruptedException();

                /* Parse Response as JSON */

                results = new JSONObject(r.toString());

            }
            catch (Exception e) {
                Log.e(TAG, " Exception: ", e);
            }
            finally {
                if (conn != null) { conn.disconnect(); }
            }

            /* Finished; Log and Return Results */

            Log.d(TAG, " JSON: " + r.toString());

            return results;

        }


    }


}