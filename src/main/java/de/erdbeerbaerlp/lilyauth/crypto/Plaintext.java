package de.erdbeerbaerlp.lilyauth.crypto;

import java.util.UUID;

public class Plaintext implements Algorithm {

    @Override
    public String hash(UUID salt, String input) {
        return input;
    }
}
