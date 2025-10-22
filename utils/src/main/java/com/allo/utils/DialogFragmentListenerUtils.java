package com.allo.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * desc:控制 dialog 弹窗先后顺序 需要配合 {@link ListenerDialog} 监听包装类使用
 * verson:
 * create by zj on 2023/06/19 09:59
 * update by zj on 2023/06/19 09:59
 */
public class DialogFragmentListenerUtils implements LifecycleEventObserver, ListenerDialogFragment.OnDismissListener {

    private final static HashMap<LifecycleOwner, DialogFragmentListenerUtils> DIALOG_MANAGER = new HashMap<>();

    /**
     * 开启标识，，临时 可能还不需要弹出任何弹窗
     */
    private boolean enable = true;


    public static DialogFragmentListenerUtils getInstance(LifecycleOwner lifecycleOwner) {
        DialogFragmentListenerUtils manager = DIALOG_MANAGER.get(lifecycleOwner);
        if (manager == null) {
            manager = new DialogFragmentListenerUtils(lifecycleOwner);
            DIALOG_MANAGER.put(lifecycleOwner, manager);
        }
        return manager;
    }

    private final List<ListenerDialogFragment> mDialogs = new ArrayList<>();

    private DialogFragmentListenerUtils(LifecycleOwner lifecycleOwner) {
        lifecycleOwner.getLifecycle().addObserver(this);
    }

    /**
     * 排序对话框
     */
    private synchronized void sortDialog() {
        if (mDialogs.size() == 1) {
            return;
        }
        Collections.sort(mDialogs, (o1, o2) -> Integer.compare(o1.getWeight(), o2.getWeight()));
    }

    public void enableShowDialog(@Nullable Boolean enable) {
        if (enable == null) {
            return;
        }
        this.enable = enable;

        if (mDialogs.isEmpty()) {
            return;
        }
        ListenerDialogFragment lastDialog = mDialogs.get(0);

        if (!enable && lastDialog.isShowing()) {
            lastDialog.dismiss();
        } else if (enable && !lastDialog.isShowing()) {
            lastDialog.show();
        }
    }

    /**
     * 排队显示 Dialog
     */
    public void addShow(ListenerDialogFragment dialog) {
        if (dialog == null || dialog.isShowing()) {
            if (BuildConfig.DEBUG) {
                throw new IllegalStateException("这个弹窗已经显示了，，调用位置不正确");
            } else {
                return;
            }
        }
        mDialogs.add(dialog);

        ListenerDialogFragment lastDialog = mDialogs.get(0);
        sortDialog();
        ListenerDialogFragment firstDialog = mDialogs.get(0);
        if (lastDialog != firstDialog) {
            //层级不对，需要调整层级-取消监听
            if (lastDialog.isShowing()) {
                lastDialog.removeOnDismissListener(this);
                lastDialog.dismiss();
            }
        }
        if (!firstDialog.isShowing()) {
            firstDialog.addOnDismissListener(this);
            if (enable){
                firstDialog.show();
            }
        }
    }

    /**
     * 取消所有 Dialog 的显示
     */
    public void clearShow() {
        if (mDialogs.isEmpty()) {
            return;
        }
        ListenerDialogFragment firstDialog = mDialogs.get(0);
        if (firstDialog.isShowing()) {
            firstDialog.removeOnDismissListener(this);
            firstDialog.dismiss();
        }
        mDialogs.clear();
    }

    @Override
    public void onDismiss(ListenerDialogFragment dialog) {
        dialog.removeOnDismissListener(this);
        mDialogs.remove(dialog);
        for (ListenerDialogFragment nextDialog : mDialogs) {
            if (!nextDialog.isShowing() && enable) {
                nextDialog.addOnDismissListener(this);
                nextDialog.show();
                break;
            }
        }
    }

    /**
     * {@link LifecycleEventObserver}
     */

    @Override
    public void onStateChanged(@NonNull LifecycleOwner lifecycleOwner, @NonNull Lifecycle.Event event) {
        if (event != Lifecycle.Event.ON_DESTROY) {
            return;
        }
        DIALOG_MANAGER.remove(lifecycleOwner);
        lifecycleOwner.getLifecycle().removeObserver(this);
        clearShow();
    }


}
