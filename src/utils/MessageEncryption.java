package utils;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class MessageEncryption {
	// Encrypt a message using a symmetric key
    public static String encryptMessage(String message, SecretKey key, IvParameterSpec iv) throws Exception {
        // Generate an IV (Initialization Vector)
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);

        // Encrypt the message
        byte[] encryptedBytes = cipher.doFinal(message.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Decrypt a message using a symmetric key
    public static String decryptMessage(String encryptedMessage, SecretKey key, IvParameterSpec iv) throws Exception {
        // Use the same IV as used during encryption
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, iv);

        // Decrypt the message
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
        return new String(decryptedBytes, "UTF-8");
    }
    
    public static IvParameterSpec generateIv() {
    	return new IvParameterSpec(new byte[16]);
    }
    // Convert IvParameterSpec to a Base64-encoded string
    public static String ivToString(IvParameterSpec ivSpec) {
        return Base64.getEncoder().encodeToString(ivSpec.getIV());
    }

    // Convert a Base64-encoded string back to IvParameterSpec
    public static IvParameterSpec stringToIV(String ivString) {
        byte[] ivBytes = Base64.getDecoder().decode(ivString);
        return new IvParameterSpec(ivBytes);
    }
}
