package com.crofogram.messenger;

import android.content.Context;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.exteragram.messenger.preferences.BasePreferencesActivity;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;

public class CrofoVaultPreferencesActivity extends BasePreferencesActivity {

    private int headerVaultRow;
    private int vaultEnabledRow;
    private int vaultPinRow;
    private int decoyPinRow;
    private int secretTriggerRow;
    private int vaultInfoRow;
    private int divider1Row;

    private int headerAntiDeleteRow;
    private int antiDeleteRow;
    private int antiDeleteInfoRow;
    private int divider2Row;

    private int headerChatsRow;
    private int hiddenChatsRow;
    private int hiddenChatsInfoRow;

    @Override
    protected void updateRowsId() {
        super.updateRowsId();

        headerVaultRow = newRow();
        vaultEnabledRow = newRow();
        if (CrofoVaultManager.isVaultEnabled()) {
            vaultPinRow = newRow();
            decoyPinRow = newRow();
            secretTriggerRow = newRow();
        } else {
            vaultPinRow = -1;
            decoyPinRow = -1;
            secretTriggerRow = -1;
        }
        vaultInfoRow = newRow();
        divider1Row = newRow();

        headerAntiDeleteRow = newRow();
        antiDeleteRow = newRow();
        antiDeleteInfoRow = newRow();
        divider2Row = newRow();

        headerChatsRow = newRow();
        hiddenChatsRow = newRow();
        hiddenChatsInfoRow = newRow();
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        return true;
    }

    @Override
    protected String getTitle() {
        return "Yashirin Seyf va Xavfsizlik";
    }

    @Override
    public View createView(Context context) {
        View view = super.createView(context);
        actionBar.setTitle("Yashirin Seyf va Xavfsizlik");
        return view;
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == vaultEnabledRow) {
            boolean enabled = !CrofoVaultManager.isVaultEnabled();
            CrofoVaultManager.setVaultEnabled(enabled);
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(enabled);
            }
            updateRowsId();
            if (listAdapter != null) {
                listAdapter.notifyDataSetChanged();
            }
            BulletinFactory.of(this).createSimpleBulletin(R.drawable.msg_secret, enabled ? "Yashirin Seyf yoqildi" : "Yashirin Seyf o'chirildi").show();
        } else if (position == vaultPinRow) {
            promptEditDialog("Maxfiy Seyf PIN kodi", "Ilovaga kirishda Seyfni ochuvchi 4 ta raqamli PIN kod:", CrofoVaultManager.getVaultPin(), true, 4, newValue -> {
                if (newValue.length() == 4) {
                    CrofoVaultManager.setVaultPin(newValue);
                    if (listAdapter != null) {
                        listAdapter.notifyDataSetChanged();
                    }
                    BulletinFactory.of(this).createSuccessBulletin("Maxfiy PIN saqlandi!").show();
                } else {
                    BulletinFactory.of(this).createErrorBulletin("PIN 4 ta raqamdan iborat bo'lishi kerak!").show();
                }
            });
        } else if (position == decoyPinRow) {
            promptEditDialog("Chalg'ituvchi PIN (Decoy)", "Kiritilganda oddiy ko'rinish beruvchi 4 ta raqamli PIN kod:", CrofoVaultManager.getDecoyPin(), true, 4, newValue -> {
                if (newValue.length() == 4) {
                    CrofoVaultManager.setDecoyPin(newValue);
                    if (listAdapter != null) {
                        listAdapter.notifyDataSetChanged();
                    }
                    BulletinFactory.of(this).createSuccessBulletin("Chalg'ituvchi PIN saqlandi!").show();
                } else {
                    BulletinFactory.of(this).createErrorBulletin("PIN 4 ta raqamdan iborat bo'lishi kerak!").show();
                }
            });
        } else if (position == secretTriggerRow) {
            promptEditDialog("Qidiruv trigger so'zi", "Qidiruv qatoriga yozilganda Seyfni ochuvchi maxfiy so'z:", CrofoVaultManager.getSecretTrigger(), false, 20, newValue -> {
                if (!TextUtils.isEmpty(newValue)) {
                    CrofoVaultManager.setSecretTrigger(newValue);
                    if (listAdapter != null) {
                        listAdapter.notifyDataSetChanged();
                    }
                    BulletinFactory.of(this).createSuccessBulletin("Trigger so'zi saqlandi: " + newValue).show();
                }
            });
        } else if (position == antiDeleteRow) {
            boolean enabled = !CrofoVaultManager.isAntiDeleteEnabled();
            CrofoVaultManager.setAntiDeleteEnabled(enabled);
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(enabled);
            }
            BulletinFactory.of(this).createSimpleBulletin(R.drawable.msg_delete, enabled ? "Anti-Delete yoqildi" : "Anti-Delete o'chirildi").show();
        } else if (position == hiddenChatsRow) {
            if (!CrofoVaultManager.isVaultUnlocked()) {
                CrofoVaultManager.promptPin(this, "Yashirin Seyf", "Yashirilgan chatlarni ko'rish uchun PIN kodni kiriting:", () -> {
                    if (listAdapter != null) {
                        listAdapter.notifyDataSetChanged();
                    }
                    presentFragment(new CrofoVaultActivity());
                });
            } else {
                presentFragment(new CrofoVaultActivity());
            }
        }
    }

    private interface OnInputSetListener {
        void onSet(String value);
    }

    private void promptEditDialog(String title, String message, String currentValue, boolean isDigits, int maxLength, OnInputSetListener listener) {
        if (getParentActivity() == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(title);
        builder.setMessage(message);

        LinearLayout container = new LinearLayout(getParentActivity());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(AndroidUtilities.dp(24), AndroidUtilities.dp(8), AndroidUtilities.dp(24), AndroidUtilities.dp(8));

        final EditText editText = new EditText(getParentActivity());
        if (isDigits) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
        }
        if (maxLength > 0) {
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        }
        editText.setText(currentValue);
        editText.setSelection(editText.getText().length());
        editText.setGravity(Gravity.CENTER);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        container.addView(editText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        builder.setView(container);
        builder.setPositiveButton("Saqlash", (dialog, which) -> {
            String text = editText.getText().toString().trim();
            if (listener != null) {
                listener.onSet(text);
            }
        });
        builder.setNegativeButton("Bekor qilish", null);
        AlertDialog d = builder.create();
        showDialog(d);
        editText.requestFocus();
        AndroidUtilities.showKeyboard(editText);
    }

    @Override
    protected BaseListAdapter createAdapter(Context context) {
        return new VaultPrefAdapter(context);
    }

    private class VaultPrefAdapter extends BaseListAdapter {

        public VaultPrefAdapter(Context context) {
            super(context);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == divider1Row || position == divider2Row) {
                return 1; // ShadowSectionCell
            } else if (position == hiddenChatsRow) {
                return 2; // TextCell
            } else if (position == headerVaultRow || position == headerAntiDeleteRow || position == headerChatsRow) {
                return 3; // HeaderCell
            } else if (position == vaultEnabledRow || position == antiDeleteRow) {
                return 5; // TextCheckCell
            } else if (position == vaultPinRow || position == decoyPinRow || position == secretTriggerRow) {
                return 7; // TextSettingsCell
            } else if (position == vaultInfoRow || position == antiDeleteInfoRow || position == hiddenChatsInfoRow) {
                return 8; // TextInfoPrivacyCell
            }
            return 2;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean payload) {
            int type = holder.getItemViewType();
            switch (type) {
                case 3: {
                    HeaderCell cell = (HeaderCell) holder.itemView;
                    if (position == headerVaultRow) {
                        cell.setText("Yashirin Seyf va PIN kodlar");
                    } else if (position == headerAntiDeleteRow) {
                        cell.setText("Xabarlar xavfsizligi");
                    } else if (position == headerChatsRow) {
                        cell.setText("Yashirilgan chatlar");
                    }
                    break;
                }
                case 5: {
                    TextCheckCell cell = (TextCheckCell) holder.itemView;
                    if (position == vaultEnabledRow) {
                        cell.setTextAndCheck("Yashirin Seyf rejimi", CrofoVaultManager.isVaultEnabled(), true);
                    } else if (position == antiDeleteRow) {
                        cell.setTextAndCheck("O'chirilgan xabarlarni saqlash (Anti-Delete)", CrofoVaultManager.isAntiDeleteEnabled(), false);
                    }
                    break;
                }
                case 7: {
                    TextSettingsCell cell = (TextSettingsCell) holder.itemView;
                    if (position == vaultPinRow) {
                        cell.setTextAndValue("Maxfiy Seyf PIN kodi", "••••", true);
                    } else if (position == decoyPinRow) {
                        cell.setTextAndValue("Chalg'ituvchi PIN (Decoy)", "••••", true);
                    } else if (position == secretTriggerRow) {
                        cell.setTextAndValue("Qidiruv trigger so'zi", CrofoVaultManager.getSecretTrigger(), false);
                    }
                    break;
                }
                case 8: {
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == vaultInfoRow) {
                        cell.setText("Ilovaga kirishda Maxfiy PIN terilsa yashirin chatlar ochiladi. Chalg'ituvchi PIN terilsa oddiy rejimda ochilib, barcha maxfiy chatlar berkiladi. Qidiruv qatoriga trigger so'zini (" + CrofoVaultManager.getSecretTrigger() + ") yozib ham Seyfni ochish mumkin.");
                    } else if (position == antiDeleteInfoRow) {
                        cell.setText("Boshqa suhbatdoshlar o'chirib yuborgan xabarlar sizning qurilmangizda saqlanib qoladi va 🗑 belgisi bilan ko'rinadi.");
                    } else if (position == hiddenChatsInfoRow) {
                        cell.setText("Asosiy ro'yxatda chat ustiga uzoq bosib, 'Yashirish (Seyf)' ni tanlash orqali chatlarni yashirishingiz mumkin.");
                    }
                    break;
                }
                case 2: {
                    TextCell cell = (TextCell) holder.itemView;
                    if (position == hiddenChatsRow) {
                        int count = CrofoVaultManager.getHiddenDialogsCount();
                        if (CrofoVaultManager.isVaultUnlocked()) {
                            cell.setTextAndValueAndIcon("Yashiringan chatlarni ko'rish", count + " ta chat", R.drawable.msg_secret, false);
                        } else {
                            cell.setTextAndValueAndIcon("Yashiringan chatlarni ko'rish", "🔒 Qulflangan", R.drawable.msg_secret, false);
                        }
                    }
                    break;
                }
            }
        }
    }
}
