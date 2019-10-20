package com.aurora.store.tokendispenser;

import spark.Request;

public class Utils {

    static long getIp(Request request) {
        String requestIp = request.headers("X-Forwarded-For");
        return ipToLong((null == requestIp || requestIp.isEmpty()) ? request.ip() : requestIp);
    }

    static long ipToLong(String address) {
        String[] split = address.split("\\.");
        if (split.length < 4) {
            return 0;
        }
        long result = 0;
        for (int i = 0; i < split.length; i++) {
            int power = 3 - i;
            result += (Integer.parseInt(split[i]) % 256 * Math.pow(256, power));
        }
        return result;
    }

    static String longToIp(long ip) {
        return ((ip >> 24) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + (ip & 0xFF);
    }

}
