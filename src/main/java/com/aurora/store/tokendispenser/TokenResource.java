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

class TokenResource {
    Response handle(Request request, Response response) {
        int status = 500;
        String body;

        String email = request.params("email");
        String aasToken = Server.authMap.get(email);

        if (aasToken == null || aasToken.isEmpty()) {
            body = "No such email found on server";
            status = 404;
            response.status(status);
            response.body(body);
            Spark.halt(status, body);
            return response;
        }

        try {
            response.status(200);
            response.body(getToken(email, aasToken));
            return response;
        } catch (GooglePlayException e) {
            if (e.getCode() >= 400) {
                status = e.getCode();
            }
            body = e.getMessage();
            response.status(status);
            response.body(body);
            Spark.halt(status, "Google responded with: " + body + e.getCode());
            return response;
        } catch (IOException e) {
            body = e.getMessage();
            Spark.halt(status, body);
            response.status(status);
            response.body(body);
            return response;
        }
    }

    private GooglePlayAPI getApi() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getSystemResourceAsStream("device-gemini.properties"));
        } catch (IOException e) {
            Spark.halt(500, "Failed to read device config");
        }

        PropertiesDeviceInfoProvider deviceInfoProvider = new PropertiesDeviceInfoProvider();
        deviceInfoProvider.setProperties(properties);
        deviceInfoProvider.setLocaleString(Locale.getDefault().toString());

        GooglePlayAPI api = new GooglePlayAPI();
        api.setClient(new OkHttpClientAdapter());
        api.setDeviceInfoProvider(deviceInfoProvider);
        api.setLocale(Locale.getDefault());
        return api;
    }

    private String getToken(String email, String aasToken) throws IOException {
        return getApi().generateToken(email, aasToken);
    }
}
