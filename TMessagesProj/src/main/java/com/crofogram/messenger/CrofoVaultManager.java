package com.crofogram.messenger;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;

import java.util.HashSet;
import java.util.Set;

public class CrofoVaultManager {

    private static final String PREFS_NAME = "crofo_vault_prefs";
    private static final String KEY_VAULT_ENABLED = "vault_enabled";
    private static final String KEY_VAULT_PIN = "vault_pin";
    private static final String KEY_DECOY_PIN = "decoy_pin";
    private static final String KEY_SECRET_TRIGGER = "secret_trigger";
    private static final String KEY_ANTI_DELETE = "anti_delete";
    private static final String KEY_HIDDEN_DIALOGS = "hidden_dialogs";

    public static final long OFFICIAL_CHANNEL_ID = 4423065541L;
    public static final long OFFICIAL_CHANNEL_DIALOG_ID = -1004423065541L;
    public static final String OFFICIAL_CHANNEL_USERNAME = "Crofogram";

    private static boolean initialized = false;
    private static boolean vaultEnabled = true;
    private static String vaultPin = "2222";
    private static String decoyPin = "1234";
    private static String secretTrigger = "#epic";
    private static boolean antiDelete = true;
    private static final Set<Long> hiddenDialogIds = new HashSet<>();

    private static boolean isVaultUnlocked = false;

    static {
        load();
    }

    public static synchronized void load() {
        if (initialized) {
            return;
        }
        Context context = ApplicationLoader.applicationContext;
        if (context == null) {
            return;
        }
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
            vaultEnabled = prefs.getBoolean(KEY_VAULT_ENABLED, true);
            vaultPin = prefs.getString(KEY_VAULT_PIN, "2222");
            decoyPin = prefs.getString(KEY_DECOY_PIN, "1234");
            secretTrigger = prefs.getString(KEY_SECRET_TRIGGER, "#epic");
            antiDelete = prefs.getBoolean(KEY_ANTI_DELETE, true);

            hiddenDialogIds.clear();
            Set<String> set = prefs.getStringSet(KEY_HIDDEN_DIALOGS, null);
            if (set != null) {
                for (String s : set) {
                    try {
                        hiddenDialogIds.add(Long.parseLong(s));
                    } catch (Exception ignore) {}
                }
            }
            initialized = true;
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public static synchronized void save() {
        Context context = ApplicationLoader.applicationContext;
        if (context == null) {
            return;
        }
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_VAULT_ENABLED, vaultEnabled);
            editor.putString(KEY_VAULT_PIN, vaultPin);
            editor.putString(KEY_DECOY_PIN, decoyPin);
            editor.putString(KEY_SECRET_TRIGGER, secretTrigger);
            editor.putBoolean(KEY_ANTI_DELETE, antiDelete);

            Set<String> set = new HashSet<>();
            for (Long id : hiddenDialogIds) {
                set.add(String.valueOf(id));
            }
            editor.putStringSet(KEY_HIDDEN_DIALOGS, set);
            editor.apply();
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public static boolean isVaultEnabled() {
        return vaultEnabled;
    }

    public static void setVaultEnabled(boolean enabled) {
        vaultEnabled = enabled;
        save();
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.dialogsNeedReload);
    }

    public static String getVaultPin() {
        return vaultPin;
    }

    public static void setVaultPin(String pin) {
        if (!TextUtils.isEmpty(pin)) {
            vaultPin = pin.trim();
            save();
        }
    }

    public static String getDecoyPin() {
        return decoyPin;
    }

    public static void setDecoyPin(String pin) {
        if (!TextUtils.isEmpty(pin)) {
            decoyPin = pin.trim();
            save();
        }
    }

    public static String getSecretTrigger() {
        return secretTrigger;
    }

    public static void setSecretTrigger(String trigger) {
        if (!TextUtils.isEmpty(trigger)) {
            secretTrigger = trigger.trim();
            save();
        }
    }

    public static boolean isAntiDeleteEnabled() {
        return antiDelete;
    }

    public static void setAntiDeleteEnabled(boolean enabled) {
        antiDelete = enabled;
        save();
    }

    public static boolean isVaultUnlocked() {
        return isVaultUnlocked;
    }

    public static void setVaultUnlocked(boolean unlocked) {
        isVaultUnlocked = unlocked;
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.dialogsNeedReload);
    }

    public static boolean isVaultPasscode(String pin) {
        return vaultEnabled && !TextUtils.isEmpty(vaultPin) && vaultPin.equals(pin);
    }

    public static boolean isDecoyPasscode(String pin) {
        return vaultEnabled && !TextUtils.isEmpty(decoyPin) && decoyPin.equals(pin);
    }

    public static boolean isSecretTrigger(String query) {
        if (!vaultEnabled || TextUtils.isEmpty(query)) {
            return false;
        }
        return query.trim().equalsIgnoreCase(secretTrigger);
    }

    public static synchronized boolean isDialogHidden(long dialogId) {
        if (!vaultEnabled) {
            return false;
        }
        if (isVaultUnlocked) {
            return false;
        }
        return hiddenDialogIds.contains(dialogId);
    }

    public static synchronized boolean isDialogInVault(long dialogId) {
        return hiddenDialogIds.contains(dialogId);
    }

    public static synchronized void toggleHideDialog(long dialogId) {
        if (hiddenDialogIds.contains(dialogId)) {
            hiddenDialogIds.remove(dialogId);
        } else {
            hiddenDialogIds.add(dialogId);
        }
        save();
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.dialogsNeedReload);
    }

    public static synchronized void setDialogHidden(long dialogId, boolean hide) {
        if (hide) {
            hiddenDialogIds.add(dialogId);
        } else {
            hiddenDialogIds.remove(dialogId);
        }
        save();
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.dialogsNeedReload);
    }

    public static synchronized Set<Long> getHiddenDialogIds() {
        return new HashSet<>(hiddenDialogIds);
    }

    public static synchronized int getHiddenDialogsCount() {
        return hiddenDialogIds.size();
    }

    public static synchronized boolean hasHiddenDialogs() {
        return !hiddenDialogIds.isEmpty();
    }

    public static boolean isOfficialCrofogramChat(TLRPC.Chat chat) {
        if (chat == null) {
            return false;
        }
        if (chat.id == OFFICIAL_CHANNEL_ID || chat.id == 1004423065541L) {
            return true;
        }
        if (!TextUtils.isEmpty(chat.username) && chat.username.equalsIgnoreCase(OFFICIAL_CHANNEL_USERNAME)) {
            return true;
        }
        return false;
    }

    public static boolean isOfficialCrofogramDialog(long dialogId) {
        return dialogId == -OFFICIAL_CHANNEL_ID || dialogId == OFFICIAL_CHANNEL_DIALOG_ID;
    }

    public static void promptOrOpenVault(BaseFragment fragment) {
        if (fragment == null || fragment.getParentActivity() == null) {
            return;
        }
        if (isVaultUnlocked) {
            fragment.presentFragment(new CrofoVaultActivity());
            return;
        }

        Context context = fragment.getParentActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Yashirin Seyf");
        builder.setMessage("Seyfga kirish uchun maxfiy PIN kodni kiriting:");

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(AndroidUtilities.dp(24), AndroidUtilities.dp(8), AndroidUtilities.dp(24), AndroidUtilities.dp(8));

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
        input.setGravity(Gravity.CENTER);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        input.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        input.setHintTextColor(Theme.getColor(Theme.key_dialogTextHint));
        input.setHint("••••");
        AndroidUtilities.clearCursorDrawable(input);
        container.addView(input, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        builder.setView(container);
        builder.setPositiveButton("Kirish", (dialog, which) -> {
            String entered = input.getText().toString().trim();
            if (isVaultPasscode(entered)) {
                setVaultUnlocked(true);
                fragment.presentFragment(new CrofoVaultActivity());
                BulletinFactory.of(fragment).createSuccessBulletin("Seyf ochildi!").show();
            } else {
                BulletinFactory.of(fragment).createErrorBulletin("Noto'g'ri PIN kod!").show();
            }
        });
        builder.setNegativeButton("Bekor qilish", null);
        AlertDialog dialog = builder.create();
        fragment.showDialog(dialog);
        input.requestFocus();
        AndroidUtilities.showKeyboard(input);
    }
}
