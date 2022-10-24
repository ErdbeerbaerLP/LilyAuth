package de.erdbeerbaerlp.lilyauth.crypto;

import java.util.UUID;

public interface Algorithm {

    String hash(UUID salt, String input);
}
