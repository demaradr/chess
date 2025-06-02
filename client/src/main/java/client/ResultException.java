package client;

import com.google.gson.Gson;
import java.util.HashMap;
import java.io.InputStreamReader;
import java.util.Map;
import java.io.InputStream;

public class ResultException extends Exception {
    final private int statusCode;

    public ResultException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public String toJson() {
        return new Gson().toJson(Map.of("message", getMessage(), "status", statusCode));
    }

    public static ResultException fromJson(InputStream stream) {
        var map = new Gson().fromJson(new InputStreamReader(stream), HashMap.class);
        var status = ((Double) map.get("status")).intValue();
        String message = map.get("message").toString();
        return new ResultException(status, message);
    }

    public static ResultException fromJson(InputStream stream, int status) {
        var map = new Gson().fromJson(new InputStreamReader(stream), HashMap.class);
        String message = map.get("message").toString();
        return new ResultException(status, message);
    }

    public int statusCode() {
        return statusCode;
    }
}