package com.aurora.store.tokendispenser;

import com.dragons.aurora.playstoreapiv2.GooglePlayAPI;
import com.dragons.aurora.playstoreapiv2.GooglePlayException;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.io.IOException;

class TokenResourceWithGSF extends TokenResource {
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
            GooglePlayAPI api = getApi();
            String token = api.generateToken(email, aasToken);
            String gsfId = api.generateGsfId();
            response.body(token + ":" + gsfId);
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
}
