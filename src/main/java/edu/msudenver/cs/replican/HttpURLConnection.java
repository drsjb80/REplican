package edu.msudenver.cs.replican;

import lombok.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

class HttpURLConnection implements HttpConnection {
    private final URLConnection urlConnection;

    HttpURLConnection(@NonNull URLConnection urlConnection) {
        this.urlConnection = urlConnection;
    }

    @Override
    public void connect() throws IOException {
        urlConnection.connect();
    }

    @Override
    public int getResponseCode() throws IOException {
        if (urlConnection instanceof java.net.HttpURLConnection) {
            return ((java.net.HttpURLConnection) urlConnection).getResponseCode();
        }
        return 200;
    }

    @Override
    public String getResponseMessage() throws IOException {
        if (urlConnection instanceof java.net.HttpURLConnection) {
            return ((java.net.HttpURLConnection) urlConnection).getResponseMessage();
        }
        return "";
    }

    @Override
    public String getHeaderField(String name) {
        return urlConnection.getHeaderField(name);
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        return urlConnection.getHeaderFields();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return urlConnection.getInputStream();
    }

    @Override
    public long getLastModified() {
        return urlConnection.getLastModified();
    }

    @Override
    public void setRequestProperty(String key, String value) {
        urlConnection.setRequestProperty(key, value);
    }

    @Override
    public void disconnect() {
        if (urlConnection instanceof java.net.HttpURLConnection) {
            ((java.net.HttpURLConnection) urlConnection).disconnect();
        }
    }
}
