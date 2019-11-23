package com.aurora.store.tokendispenser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class Server {

    private static final Logger LOG = LoggerFactory.getLogger(Server.class.getName());

    static Map<String, String> authMap = new HashMap<>();

    public static void main(String[] args) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(System.getProperty("credentials")));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] credential = line.split(" ");
                authMap.put(credential[0], credential[1]);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String host = System.getProperty("host", "0.0.0.0");
        // Google auth requests are not fast, so lets limit max simultaneous threads
        Spark.threadPool(32, 2, 5000);
        Spark.ipAddress(host);
        Spark.port(Integer.parseInt(System.getProperty("port", "8080")));

        if (System.getProperty("keystore") != null) {
            Spark.secure(System.getProperty("keystore"), System.getProperty("keystore_password"), null, null);
        }

        Spark.before((req, res) -> {
            LOG.info(req.requestMethod() + " " + req.url());
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Request-Method", "GET");
        });

        Spark.after((req, res) -> res.type("text/plain"));
        Spark.get("/", (req, res) -> "Aurora Token Dispenser");
        Spark.get("/status", (req, res) -> "Token dispenser is alive !");
        Spark.get("/token/email/:email", (req, res) -> new TokenResource().handle(req, res));
        Spark.get("/email", (req, res) -> getRandomEmail(req, res));
        Spark.notFound((req, res) -> "You are lost !");
    }

    private static Response getRandomEmail(Request request, Response response) {
        Object[] keyArray = authMap.keySet().toArray();
        Object key = keyArray[new Random().nextInt(keyArray.length)];
        String email = key.toString();
        if (email == null || email.isEmpty()) {
            String body = "Could not retrieve email from server";
            int status = 500;
            Spark.halt(status, body);
            response.status(status);
            response.body(body);
        } else {
            response.status(200);
            response.body(key.toString());
        }
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
