package com.example.seechange.service;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
public class Encoder {
    public static byte[] decodeToByteArray(String hex) throws DecoderException {
        byte[] bytes = Hex.decodeHex(hex.toCharArray());
        return bytes;
    }
    public static String encodeToHex(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
    public static String hexToString(String hex) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hex.length(); i+=2) {
            String str = hex.substring(i, i+2);
            output.append((char)Integer.parseInt(str, 16));
        }
        return output.toString().trim().replace("\"", "");
    }
}