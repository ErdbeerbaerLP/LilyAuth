package de.erdbeerbaerlp.lilyauth.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class SHA512 implements Algorithm {
    @Override
    public String hash(UUID salt, String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt.toString().getBytes(StandardCharsets.UTF_8));
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16)
                        .substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
