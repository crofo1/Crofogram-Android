package com.crofogram.messenger;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.ChatActivity;
import org.telegram.messenger.MessagesStorage;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CrofoVaultActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private RecyclerListView listView;
    private VaultAdapter adapter;
    private EmptyTextProgressView emptyView;
    private final List<Long> hiddenList = new ArrayList<>();

    private static final int ITEM_LOCK = 1;
    private static final int ITEM_SETTINGS = 2;
    private static final int ITEM_ADD = 3;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        if (!CrofoVaultManager.isVaultUnlocked()) {
            return false;
        }
        getNotificationCenter().addObserver(this, NotificationCenter.dialogsNeedReload);
        getNotificationCenter().addObserver(this, NotificationCenter.updateInterfaces);
        loadHiddenDialogs();
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        getNotificationCenter().removeObserver(this, NotificationCenter.dialogsNeedReload);
        getNotificationCenter().removeObserver(this, NotificationCenter.updateInterfaces);
    }

    private void loadHiddenDialogs() {
        hiddenList.clear();
        Set<Long> set = CrofoVaultManager.getHiddenDialogIds();
        if (set != null) {
            hiddenList.addAll(set);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        updateEmptyView();
    }

    private void updateEmptyView() {
        if (emptyView != null) {
            if (hiddenList.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Yashirin Seyf");
        actionBar.setSubtitle("Maxfiy chatlar");

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == ITEM_LOCK) {
                    CrofoVaultManager.setVaultUnlocked(false);
                    finishFragment();
                    BulletinFactory.of(CrofoVaultActivity.this).createSimpleBulletin(R.drawable.msg_secret, "Seyf qulflandi!").show();
                } else if (id == ITEM_SETTINGS) {
                    presentFragment(new CrofoVaultPreferencesActivity());
                } else if (id == ITEM_ADD) {
                    Bundle args = new Bundle();
                    args.putBoolean("onlySelect", true);
                    args.putBoolean("checkCanWrite", false);
                    args.putInt("dialogsType", DialogsActivity.DIALOGS_TYPE_DEFAULT);
                    DialogsActivity activity = new DialogsActivity(args);
                    activity.setDelegate((f, dids, message, param, topicsFragment) -> {
                        if (dids != null && !dids.isEmpty()) {
                            for (MessagesStorage.TopicKey key : dids) {
                                CrofoVaultManager.setDialogHidden(key.dialogId, true);
                            }
                            activity.finishFragment();
                            loadHiddenDialogs();
                            BulletinFactory.of(CrofoVaultActivity.this).createSimpleBulletin(R.drawable.msg_secret, "Chat seyfga yashirildi!").show();
                        }
                        return true;
                    });
                    presentFragment(activity);
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        menu.addItem(ITEM_ADD, R.drawable.msg_add);
        menu.addItem(ITEM_LOCK, R.drawable.msg_secret);
        menu.addItem(ITEM_SETTINGS, R.drawable.msg_settings);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        emptyView = new EmptyTextProgressView(context);
        emptyView.setShowAtCenter(true);
        emptyView.showTextView();
        emptyView.setText("Hozircha yashirin chatlar yo'q.\n\nChat yashirish uchun yuqoridagi '+' tugmasini bosing.");
        frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        adapter = new VaultAdapter(context);
        listView.setAdapter(adapter);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView.setOnItemClickListener((view, position) -> {
            if (position < 0 || position >= hiddenList.size()) {
                return;
            }
            long did = hiddenList.get(position);
            Bundle args = new Bundle();
            if (DialogObject.isUserDialog(did)) {
                args.putLong("user_id", did);
            } else {
                args.putLong("chat_id", -did);
            }
            if (getMessagesController().checkCanOpenChat(args, CrofoVaultActivity.this)) {
                presentFragment(new ChatActivity(args));
            }
        });

        listView.setOnItemLongClickListener((view, position) -> {
            if (position < 0 || position >= hiddenList.size()) {
                return false;
            }
            long did = hiddenList.get(position);
            AlertDialog.Builder b = new AlertDialog.Builder(getParentActivity());
            b.setTitle("Seyf amallari");
            CharSequence[] items = new CharSequence[]{"Seyfdan chiqarish (Oshkora qilish)", "Chatni ochish"};
            b.setItems(items, (dialog, which) -> {
                if (which == 0) {
                    CrofoVaultManager.setDialogHidden(did, false);
                    loadHiddenDialogs();
                    BulletinFactory.of(CrofoVaultActivity.this).createSimpleBulletin(R.drawable.msg_secret, "Chat seyfdan chiqarildi").show();
                } else if (which == 1) {
                    Bundle args = new Bundle();
                    if (DialogObject.isUserDialog(did)) {
                        args.putLong("user_id", did);
                    } else {
                        args.putLong("chat_id", -did);
                    }
                    if (getMessagesController().checkCanOpenChat(args, CrofoVaultActivity.this)) {
                        presentFragment(new ChatActivity(args));
                    }
                }
            });
            showDialog(b.create());
            return true;
        });

        updateEmptyView();
        return fragmentView;
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.dialogsNeedReload || id == NotificationCenter.updateInterfaces) {
            loadHiddenDialogs();
        }
    }

    private class VaultAdapter extends RecyclerListView.SelectionAdapter {

        private final Context mContext;

        public VaultAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }

        @Override
        public int getItemCount() {
            return hiddenList.size();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            UserCell cell = new UserCell(mContext, 6, 0, false);
            cell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            return new RecyclerListView.Holder(cell);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (position < 0 || position >= hiddenList.size()) {
                return;
            }
            long did = hiddenList.get(position);
            UserCell cell = (UserCell) holder.itemView;
            TLObject object;
            if (DialogObject.isUserDialog(did)) {
                object = getMessagesController().getUser(did);
            } else {
                object = getMessagesController().getChat(-did);
            }
            if (object != null) {
                cell.setData(object, null, "🔒 Yashirin chat", 0);
            }
        }
    }
}
