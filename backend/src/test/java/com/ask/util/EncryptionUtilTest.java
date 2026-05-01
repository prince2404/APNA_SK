package com.ask.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class EncryptionUtilTest {

    @Test
    void encryptUsesRandomIvAndDecryptsBackToPlainText() {
        EncryptionUtil encryptionUtil = new EncryptionUtil("CHANGE_THIS_32_CHAR_AES_KEY_0123");

        String plainText = "1234567890123456";
        String firstEncrypted = encryptionUtil.encrypt(plainText);
        String secondEncrypted = encryptionUtil.encrypt(plainText);

        assertNotEquals(firstEncrypted, secondEncrypted);
        assertEquals(plainText, encryptionUtil.decrypt(firstEncrypted));
        assertEquals(plainText, encryptionUtil.decrypt(secondEncrypted));
    }
}
