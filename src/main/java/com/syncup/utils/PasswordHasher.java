package com.syncup.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class PasswordHasher {
    public static String sha256(String raw){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for(byte b: out) sb.append(String.format("%02x", b));
            return "sha256:"+sb.toString();
        }catch(Exception e){
            throw new RuntimeException("Unable to hash password", e);
        }
    }
}
