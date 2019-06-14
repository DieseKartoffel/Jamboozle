package com.example.kartoffel.playlisttogether;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class APIcommunication {

    //callback via method call in browser activity
    public static void updateServers(ServerbrowsingActivity browser) {
        Log.d("API", "Looking for Servers");
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("key1", "value1");
        data.put("key2", "value2");
        AsyncHttpPost asyncHttpPost = new AsyncHttpPost(data);
        asyncHttpPost.setListener(new AsyncHttpPost.Listener(){
            @Override
            public void onResult(String result) {
                Log.d("API", "UpdateServer Result" + result);
            }
        });
        asyncHttpPost.execute("http://jamboozle.tk:8888/serverlist");
    }

    public static class AsyncHttpPost extends AsyncTask<String, String, String> {
        interface Listener {
            void onResult(String result);
        }
        private Listener mListener;
        private HashMap<String, String> mData = null;// post data

        /**
         * constructor
         */
        public AsyncHttpPost(HashMap<String, String> data) {
            mData = data;
        }

        public void setListener(Listener listener) {
            mListener = listener;
        }

        @Override
        protected String doInBackground(String... params) {
            byte[] result = null;
            String str = "";
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(params[0]);// in this case, params[0] is URL
            try {
                // set up post data
                ArrayList<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
                Iterator<String> it = mData.keySet().iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    nameValuePair.add(new BasicNameValuePair(key, mData.get(key)));
                }

                post.setEntity(new UrlEncodedFormEntity(nameValuePair, "UTF-8"));
                HttpResponse response = client.execute(post);
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpURLConnection.HTTP_OK){
                    result = EntityUtils.toByteArray(response.getEntity());
                    str = new String(result, "UTF-8");
                }
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
            }
            return str;
        }

        /**
         * on getting result
         */
        @Override
        protected void onPostExecute(String result) {
            if (mListener != null) {
                mListener.onResult(result);
            }
        }
    }
}


