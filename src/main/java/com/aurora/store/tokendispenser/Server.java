package com.aurora.store.tokendispenser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class Server {

    public static final Logger LOG = LoggerFactory.getLogger(Server.class.getName());

    static Map<String, String> authMap = new HashMap<>();

    static {
        authMap.put("username@gmail.com", "password");
    }

    public static void main(String[] args) {
        String host = "0.0.0.0";
        // Google auth requests are not fast, so lets limit max simultaneous threads
        Spark.threadPool(32, 2, 5000);
        Spark.ipAddress(host);
        Spark.port(8443/*getHerokuAssignedPort()*/);
        Spark.secure("your_keystore", "your_keystore_password", null, null);

        Spark.before((req, res) -> {
            LOG.info(req.requestMethod() + " " + req.url());
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Request-Method", "GET");
        });

        Spark.after((req, res) -> res.type("text/plain"));
        Spark.get("/", (req, res) -> "Aurora Token Dispenser");
        Spark.get("/status", (req, res) -> "Token dispenser is alive !");
        Spark.get("/token/email/:email", (req, res) -> new TokenResource().handle(req, res));
        Spark.get("/token-ac2dm/email/:email", (req, res) -> new TokenAc2dmResource().handle(req, res));
        Spark.get("/email", (req, res) -> getRandomEmail(req, res));
        Spark.notFound((req, res) -> "You are lost !");
    }

    private static Response getRandomEmail(Request request, Response response) {
        Object[] keyArray = authMap.keySet().toArray();
        Object key = keyArray[new Random().nextInt(keyArray.length)];
        response.body(key.toString());
        return response;
    }

    private static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567;
    }
}
