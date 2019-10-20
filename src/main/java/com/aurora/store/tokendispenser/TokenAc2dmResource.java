package com.aurora.store.tokendispenser;

import com.dragons.aurora.playstoreapiv2.GooglePlayAPI;
import com.dragons.aurora.playstoreapiv2.GooglePlayException;
import com.dragons.aurora.playstoreapiv2.PropertiesDeviceInfoProvider;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

public class TokenAc2dmResource {

    public String handle(Request request, Response response) {
        String email = request.params("email");
        String password = Server.authMap.get(email);
        int code = 500;
        String message;
        try {
            String token = getToken(email, password);
            return token;
        } catch (GooglePlayException e) {
            if (e.getCode() >= 400) {
                code = e.getCode();
            }
            message = e.getMessage();
            Spark.halt(code, "Google responded with: " + message + e.getCode());
        } catch (IOException e) {
            message = e.getMessage();
            Spark.halt(code, message);
        }
        return "";
    }

    GooglePlayAPI getApi() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getSystemResourceAsStream("device-gemini.properties"));
        } catch (IOException e) {
            Spark.halt(500, "device-gemini.properties not found");
        }

        PropertiesDeviceInfoProvider deviceInfoProvider = new PropertiesDeviceInfoProvider();
        deviceInfoProvider.setProperties(properties);
        deviceInfoProvider.setLocaleString(Locale.ENGLISH.toString());

        GooglePlayAPI api = new GooglePlayAPI();
        api.setClient(new OkHttpClientAdapter());
        api.setDeviceInfoProvider(deviceInfoProvider);
        api.setLocale(Locale.US);
        return api;
    }

    protected String getToken(String email, String password) throws IOException {
        return getApi().generateToken(email, password);
    }
}
