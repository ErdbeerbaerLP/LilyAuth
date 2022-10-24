package de.erdbeerbaerlp.lilyauth.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class MD5 implements Algorithm {

    @Override
    public String hash(UUID salt, String input) {
        final MessageDigest md;
        final StringBuilder sb = new StringBuilder();
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(salt.toString().getBytes(StandardCharsets.UTF_8));
            md.update(input.getBytes());
            final byte[] bytes = md.digest();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xFF) + 256, 16).substring(1));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
