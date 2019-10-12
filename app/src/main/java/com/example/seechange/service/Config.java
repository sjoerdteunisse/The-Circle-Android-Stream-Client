package com.example.seechange.service;

public class Config {
    // Update the IP in 'rtsp/java/com.pedrop.rtsp/rtsp/rtspsender' aswell (Line 169)
    private static final String IP =  "145.49.2.169";
    //    private static final String IP =  "192.168.178.171";
    private static final String WEBPORT = ":3000";
    private static final String STREAMPORT = ":8080";
    private static final String BASIC_URL = "http://" + IP + WEBPORT;
    private static final String BASIC_STREAMURL = "rtsp://" + IP + STREAMPORT;
    public static final String URL_AUTH = BASIC_URL + "/api/auth";
    public static final String URL_REGISTER = BASIC_URL + "/api/User/Register";
    public static final String URL_GETVIEWCOUNT = BASIC_URL;
    public static final String URL_STARTSTREAM = BASIC_STREAMURL + "/live/";
    public static final String URL_NEWAVATAR = BASIC_URL + "/api/User/NewAvatar";
    public static final String URL_GETSATOSHI = BASIC_URL + "/api/User/Satoshibalance/";

    public static final String PUBLIC_KEY_SERVER = "-----BEGIN PUBLIC KEY-----\n" +
            "MIGeMA0GCSqGSIb3DQEBAQUAA4GMADCBiAKBgHQxNkTJot3CrlQWwyGXbJYpCyPH\n" +
            "VlThDNkQV0ykv5myFmPn+YOTnzWdB3vcPrXHD/Znql+skLfdpufyt2Bwn5Txhgnk\n" +
            "CDJS22WS+m/c4GRjsVgIeSBR3OQmBD028Lan6ThmSreCK8JOvXTsGgM1/bWvfCCs\n" +
            "M11F1u1mjWvskVt/AgMBAAE=\n" +
            "-----END PUBLIC KEY-----";
}



