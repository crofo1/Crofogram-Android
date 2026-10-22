package com.exteragram.messenger.boost.encryption;

import org.telegram.messenger.MessageObject;
import androidx.core.util.Consumer;

public class EncryptionHelper {
    public static String[] names = new String[0];

    public static boolean isEncrypted(String message) {
        return false;
    }

    public static boolean isEncrypted(MessageObject obj, Object o) {
        return false;
    }

    public static MessageObject decryptMessage(MessageObject obj) {
        return obj;
    }

    public static MessageObject decryptMessage(MessageObject obj, MessageObject.GroupedMessages group) {
        return obj;
    }

    public static String encryptMessage(String text, long dialog_id, BaseEncryptor encryptor) {
        return text;
    }

    public static void encryptMessage(String text, long dialog_id, int type, Consumer<String> callback) {
        if (callback != null) {
            callback.accept(text);
        }
    }

    public static BaseEncryptor getEncryptorBy(long dialog_id) {
        return new BaseEncryptor();
    }

    public static int getEncryptorTypeFor(long dialog_id) {
        return 0;
    }

    public static void setEncryptorTypeFor(long dialog_id, int type) {
    }
}
