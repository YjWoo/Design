package com.indigo.vincent.Util;

import android.location.Location;
import android.location.LocationManager;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LocationUtil {
    public LocationManager locationManager;

    public LocationUtil(LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    /**
     * 得到当前经纬度并开启线程去反向地理编码
     */
    public String getAddress(Location location) {
        String latitude = location.getLatitude() + "";
        String longitude = location.getLongitude() + "";
        String address = new String();
        String url = "http://api.map.baidu.com/geocoder/v2/?ak=z7khow5B5WXIGlMGTCYVXi44He9PFrGi&callback=renderReverse&location=" + latitude + "," + longitude + "&output=json&pois=0";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            String regex = ":\".+\",\"bus";
            address = response.body().string();
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(address);
            while (matcher.find()) {
                address = matcher.group(0);
            }
            if (address.length() > 8)
                address = address.substring(2, address.length() - 6);
        } catch (IOException e) {
            return null;
        }
        return address;
    }

}
