package utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class AsymmetricEncryptionUtil {

    // Generate a new RSA KeyPair (Public and Private Key)
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048); // Key size: 2048 bits
        return keyGen.generateKeyPair();
    }

    // Encrypt a message with a Public Key
    public static String encrypt(String message, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes); // Encode to Base64 for storage
    }

    // Decrypt a message with a Private Key
    public static String decrypt(String encryptedMessage, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedMessage); // Decode Base64 string
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }
    
    public static void savePrivateKey(String username, PrivateKey privateKey) throws IOException {
        // Directory where private keys will be saved
        String keysDirectory = "user_keys";
        File directory = new File(keysDirectory);
        if (!directory.exists()) {
            directory.mkdirs(); // Create the directory if it doesn't exist
        }

        // File path for the private key
        String privateKeyFilePath = keysDirectory + File.separator + username + "_private_key.pem";

        // Convert the private key to PEM format
        String encodedKey = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        String pemFormattedKey = "-----BEGIN PRIVATE KEY-----\n"
                + formatKey(encodedKey)
                + "-----END PRIVATE KEY-----";

        // Write the private key to the file
        try (FileWriter writer = new FileWriter(privateKeyFilePath)) {
            writer.write(pemFormattedKey);
        }
    }

    private static String formatKey(String encodedKey) {
        StringBuilder formattedKey = new StringBuilder();
        int index = 0;
        while (index < encodedKey.length()) {
            int endIndex = Math.min(index + 64, encodedKey.length());
            formattedKey.append(encodedKey, index, endIndex).append("\n");
            index = endIndex;
        }
        return formattedKey.toString();
    }
    
    public static PrivateKey readPrivateKey(String username) throws Exception {
        // Directory where private keys are stored
        String keysDirectory = "user_keys";
        String privateKeyFilePath = keysDirectory + File.separator + username + "_private_key.pem";

        // Read the content of the PEM file
        String pemContent = new String(Files.readAllBytes(Paths.get(privateKeyFilePath)));

        // Remove PEM headers and footers
        String encodedKey = pemContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", ""); // Remove all whitespace and newlines

        // Decode the Base64-encoded key
        byte[] keyBytes = Base64.getDecoder().decode(encodedKey);

        // Convert the bytes into a PrivateKey object
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }
    
    public static String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
    
    public static PublicKey stringToPublicKey(String keyStr) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(keyStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }
    

    //TODO
    /*
     * Create table with both encrypted symmetric keys
     */
    
}
