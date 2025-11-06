package com.bms.account.utility;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PinEncoder {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    // Encode the plain 4-digit PIN
    public String encode(String pin) {
        return encoder.encode(pin);
    }

    // Verify raw PIN with encoded one
    public boolean matches(String rawPin, String encodedPin) {
        return encoder.matches(rawPin, encodedPin);
    }
}
