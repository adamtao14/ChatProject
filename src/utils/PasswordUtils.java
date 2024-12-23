package utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import utils.PasswordUtils;

public class PasswordUtils {
    public static class HashResult {
        private final String hash;
        private final String salt;

        public HashResult(String hash, String salt) {
            this.hash = hash;
            this.salt = salt;
        }

        public String getHash() { return hash; }
        public String getSalt() { return salt; }
    }

    public static HashResult hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            
            String hashString = Base64.getEncoder().encodeToString(hash);
            String saltString = Base64.getEncoder().encodeToString(salt);
            
            return new HashResult(hashString, saltString);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static boolean verifyPassword(String password, String storedHash, String storedSalt) {
        try {
            byte[] salt = Base64.getDecoder().decode(storedSalt);
            
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            
            String newHash = Base64.getEncoder().encodeToString(hash);
            return newHash.equals(storedHash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error verifying password", e);
        }
    }
}