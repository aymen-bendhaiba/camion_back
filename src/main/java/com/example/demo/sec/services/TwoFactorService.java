package com.example.demo.sec.services;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class TwoFactorService {
    
    private static final int DIGITS = 6;
    private static final int PERIOD = 30;
    
    /**
     * Génère une nouvelle clé secrète pour un utilisateur
     */
    public String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return encodeBase32(bytes).substring(0, 32);
    }
    
    /**
     * Encode des bytes en Base32
     */
    private String encodeBase32(byte[] input) {
        String base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        StringBuilder result = new StringBuilder();
        
        int bits = 0;
        int value = 0;
        
        for (byte b : input) {
            value = (value << 8) | (b & 0xFF);
            bits += 8;
            
            while (bits >= 5) {
                result.append(base32Chars.charAt((value >>> (bits - 5)) & 0x1F));
                bits -= 5;
            }
        }
        
        if (bits > 0) {
            result.append(base32Chars.charAt((value << (5 - bits)) & 0x1F));
        }
        
        return result.toString();
    }
    
    /**
     * Génère l'URL QR code pour Google Authenticator
     */
    public String generateQRCodeUrl(String username, String secretKey) {
        return "otpauth://totp/TransportPro:" + username + "?secret=" + secretKey + "&issuer=TransportPro";
    }
    
    /**
     * Vérifie si le code TOTP est valide
     */
    public boolean verifyCode(String secretKey, int code) {
        return verifyCodeWithTolerance(secretKey, code, 0);
    }
    
    /**
     * Vérifie si le code TOTP est valide avec une tolérance de 1 période
     */
    public boolean verifyCodeWithTolerance(String secretKey, int code) {
        return verifyCodeWithTolerance(secretKey, code, 1);
    }
    
    /**
     * Vérifie si le code TOTP est valide avec une tolérance spécifiée
     */
    public boolean verifyCodeWithTolerance(String secretKey, int code, int tolerance) {
        long currentTime = System.currentTimeMillis() / 1000;
        
        for (int i = -tolerance; i <= tolerance; i++) {
            long time = currentTime + (i * PERIOD);
            int generatedCode = generateTOTP(secretKey, time);
            System.out.println("Generated code: " + generatedCode + " for time: " + time);
            if (generatedCode == code) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Génère le code TOTP actuel pour une clé donnée (pour debug)
     */
    public int getCurrentTOTP(String secretKey) {
        long currentTime = System.currentTimeMillis() / 1000;
        return generateTOTP(secretKey, currentTime);
    }
    
    /**
     * Génère un code TOTP pour un temps donné
     */
    private int generateTOTP(String secretKey, long time) {
        try {
            // Décoder la clé Base32 (format standard pour TOTP)
            byte[] key = decodeBase32(secretKey);
            byte[] timeBytes = ByteBuffer.allocate(8).putLong(time / PERIOD).array();
            
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec keySpec = new SecretKeySpec(key, "HmacSHA1");
            mac.init(keySpec);
            
            byte[] hash = mac.doFinal(timeBytes);
            int offset = hash[hash.length - 1] & 0xf;
            
            int binary = ((hash[offset] & 0x7f) << 24) |
                        ((hash[offset + 1] & 0xff) << 16) |
                        ((hash[offset + 2] & 0xff) << 8) |
                        (hash[offset + 3] & 0xff);
            
            return binary % (int) Math.pow(10, DIGITS);
            
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Décode une chaîne Base32
     */
    private byte[] decodeBase32(String input) {
        String base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        input = input.toUpperCase().replaceAll("[^A-Z2-7]", "");
        
        int bits = 0;
        int value = 0;
        int index = 0;
        byte[] result = new byte[input.length() * 5 / 8];
        
        for (char c : input.toCharArray()) {
            value = (value << 5) | base32Chars.indexOf(c);
            bits += 5;
            
            if (bits >= 8) {
                result[index++] = (byte) ((value >>> (bits - 8)) & 0xFF);
                bits -= 8;
            }
        }
        
        return result;
    }
} 