package com.example.kartoffel.playlisttogether;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLInfo {


    public static void main(String[] args) {
        String r = spotifyInfo("https://open.spotify.com/track/3Qh4Inf43LSK22wdfmLC2r?si=EyCFfmGzQZ-etTFAfTYh2w");
        System.out.println(r);
        r = spotifyInfo("https://open.spotify.com/track/6kaPNiBCzWGzY2QoZl4PtR?si=gOFFAYEQSNu3-k80lIBf1w");
        System.out.println(r);
        r = spotifyInfo("https://open.spotify.com/track/6axRpFl0s82PfwsNBASrw0?si=1RwgsvU2RNWpP2IJuoolkg");
        System.out.println(r);
        r = spotifyInfo("https://open.spotify.com/track/25kGNSv0yGXZKaC79xzrvg?si=iIIlEPcTTZCvzV4F-NwThg");
        System.out.println(r);
        r = spotifyInfo("https://open.spotify.com/track/6I9VzXrHxO9rA9A5euc8Ak?si=S20GwnVhSPa0HZQWW2rS0A");
        System.out.println(r);
        r = spotifyInfo("https://open.spotify.com/user/31cndeodmoctx4kwffbhjl5gldxq/playlist/5NPTyWdGY6SvXo8VOP9uHr?si=vk0ZTIFbQb-xOPSyBo5XZg");
        System.out.println(r);
    }

    public static String spotifyInfo(String songUrl) {

        URL url;

        try

        {

            Log.d("URLINFO", "Attempting to get Trackname for " + songUrl);
            HttpResponse response;
            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet(songUrl);
                response = client.execute(request);
            } catch (IllegalArgumentException iae) {
                Log.d("URLINFO", "Track URL invalid");
                return null;
            }


        String html = "Error";
        InputStream in = response.getEntity().getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder str = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            str.append(line);
        }
        in.close();
        html = str.toString();


        // get URL content
        /**
         url = new URL(songUrl);
         HttpURLConnection conn = (HttpURLConnection) url.openConnection();

         // open the stream and put it into BufferedReader
         BufferedReader br = new BufferedReader(
         new InputStreamReader(conn.getInputStream()));

         String inputLine;

         //use FileWriter to write file
         StringWriter sw = new StringWriter();

         while ((inputLine = br.readLine()) != null) {
         sw.write(inputLine);
         }



         String html = sw.toString();
         sw.close();
         */

        Log.d("URLInfo", "Full HTML " + html);

        List<String> allMatches = new ArrayList<String>();
        Matcher m = Pattern.compile("<title>(.*?)</title>")
                .matcher(html);
        while (m.find()) {
            allMatches.add(m.group());
        }


        String title;
        if (allMatches.size() > 0) {
            Log.d("URLInfo", "Fetched HTML title for Spotify URL \n " + allMatches.get(0));
            //<title>Sticks &amp; Stones, a song by Ramson Badbonez on Spotify</title>
            title = allMatches.get(0);
            title = title.replaceAll("<title>", "");
            title = title.replaceAll(", a song", "");
            title = title.replaceAll(" on Spotify</title>", "");

            Log.d("URLInfo", title);
            return title;
        }

    } catch(
    MalformedURLException e)

    {
        e.printStackTrace();
    } catch(
    IOException e)

    {
        e.printStackTrace();
    }
        Log.d("URLInfo","Error!");
        return"Error in URL Info";
}

    public static String spotifyTrackURI(String spotifyTrackUrl) {
        //https://open.spotify.com/track/7lLbnqVEA5vvgYEmyvBy2r?si=E1PqIwweRsSUlgKVCSG8jw


        List<String> allMatches = new ArrayList<String>();
        Matcher m = Pattern.compile("/track/(.*?)\\?si=")
                .matcher(spotifyTrackUrl);
        while (m.find()) {
            allMatches.add(m.group());
        }
        if (allMatches.size() > 0) {
            ///track/13hQj3kA6hLlKR9fBK9wCq?si=
            String uri = allMatches.get(0);
            uri = uri.replaceAll("/track/", "");
            uri = uri.replaceAll("\\?si=", "");
            Log.d("URIInfo", "Spotify URI " + uri);
            return uri;
        }
        return "Error in URI Parsing";


    }
}
