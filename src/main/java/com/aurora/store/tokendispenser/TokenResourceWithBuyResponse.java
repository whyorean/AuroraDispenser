package com.aurora.store.tokendispenser;

import com.dragons.aurora.playstoreapiv2.BuyResponse;
import com.dragons.aurora.playstoreapiv2.DeliveryResponse;
import com.dragons.aurora.playstoreapiv2.GooglePlayAPI;
import com.dragons.aurora.playstoreapiv2.GooglePlayException;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.io.IOException;

class TokenResourceWithBuyResponse extends TokenResource {
    Response handle(Request request, Response response) {
        int status = 500;
        String body;

        String email = Server.getRandomEmail();
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
            String packageName = request.params("package");
            String code = request.params("code");
            String offer = request.params("offer");

            int versionCode = Integer.parseInt(code);
            int offerCode = Integer.parseInt(offer);

            api.setGsfId(gsfId);
            api.setToken(token);

            BuyResponse buyResponse = api.purchase(packageName, versionCode, offerCode);
            String downloadToken = buyResponse.getDownloadToken();

            if (downloadToken.isEmpty()) {
                Spark.halt(status, "Could not retrieve download token for requested package");
            }

            DeliveryResponse deliveryResponse = api.delivery(packageName, 0, versionCode, offerCode, downloadToken);
            String downloadUrl = deliveryResponse.getAppDeliveryData().getDownloadUrl();

            if (downloadUrl.isEmpty()) {
                Spark.halt(status, "Could not retrieve download url for requested package");
            }

            response.body(downloadUrl);
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
