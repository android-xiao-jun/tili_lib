package com.allo.utils;

import android.content.DialogInterface;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

/**
 * desc:扩展 AppCompatDialog 增加  OnShowListener 和 OnDismissListener 方法，，因为需要监听弹窗消失所以加了这个方法
 * verson:
 * create by zj on 2023/06/19 10:16
 * update by zj on 2023/06/19 10:16
 */
public class ListenerDialogFragment implements LifecycleEventObserver, DialogInterface.OnShowListener, DialogInterface.OnDismissListener {

    private final ListenersWrapper<ListenerDialogFragment> mListeners = new ListenersWrapper<>(this);
    /**
     * 排序标记(值越高，，显示越上面)
     */
    private int weight = 0;


    @Nullable
    private List<OnShowListener> mShowListeners;

    @Nullable
    private List<OnDismissListener> mDismissListeners;

    private DialogFragment mDialog;
    private final FragmentManager mFragmentManager;
    private boolean isAddDelayShow = false;
    private boolean isAddStackShow = false;
    private String uniqueTag = getClass().getSimpleName();

    public ListenerDialogFragment(DialogFragment dialog, FragmentManager fragmentManager) {
        mDialog = dialog;
        mFragmentManager = fragmentManager;
        mDialog.getLifecycle().addObserver(this);
    }

    public ListenerDialogFragment(DialogFragment dialog, FragmentManager fragmentManager, int weight) {
        mDialog = dialog;
        mFragmentManager = fragmentManager;
        this.weight = weight;
        mDialog.getLifecycle().addObserver(this);
    }

    /**
     * 设置唯一属性
     * @param uniqueTag tag
     */
    public void setUniqueTag(@Nullable String uniqueTag) {
        this.uniqueTag = uniqueTag;
    }

    public String getUniqueTag() {
        return uniqueTag;
    }

    public boolean isAddStackShow() {
        return isAddStackShow;
    }

    /**
     * 设置堆叠显示  默认false 一个一个显示
     * @param addStackShow
     */
    public void setAddStackShow(boolean addStackShow) {
        isAddStackShow = addStackShow;
    }

    /**
     * 当前是否显示
     *
     * @return
     */
    public boolean isShowing() {
        return mDialog != null && isAddDelayShow;
    }

    /**
     * 显示
     */
    public void show() {
        if (mDialog != null) {
            if (mFragmentManager.isDestroyed()) {
                return;
            }
            String tag = this.uniqueTag;
            if (TextUtils.isEmpty(tag)) {
                tag = this.getClass().getSimpleName();
            }

            mFragmentManager.beginTransaction()
                    .remove(mDialog)
                    .add(mDialog, tag)
                    .commitAllowingStateLoss();

            isAddDelayShow = true;
        }
    }

    /**
     * 隐藏
     */
    public void dismiss() {
        if (mDialog != null) {
            mDialog.dismissAllowingStateLoss();
        }
    }

    /**
     * 是否相等
     */
    public boolean equalDialog(DialogFragment dialogFragment) {
        return dialogFragment == mDialog;
    }

    /**
     * 是否是这个类
     */
    public boolean equalClass(Class<?> clz) {
        if (mDialog == null) {
            return false;
        }
        return clz == mDialog.getClass();
    }

    @Nullable
    public DialogFragment getInnerDialog() {
        return mDialog;
    }

    public int getWeight() {
        return weight;
    }

    /**
     * 弹窗权重--显示层级
     *
     * @param weight
     */
    public void setWeight(int weight) {
        this.weight = weight;
    }

    /**
     * 设置新的对话框（如果弹窗有多层 需要处理的话）
     *
     * @param newDialog 下级弹窗
     * @return 上级弹窗
     */
    @Nullable
    public DialogFragment setDialog(DialogFragment newDialog) {
        DialogFragment old = mDialog;
        old.getLifecycle().removeObserver(this);

        mDialog = newDialog;
        mDialog.getLifecycle().addObserver(this);
        return old;
    }

    /**
     * 添加一个显示监听器
     *
     * @param listener 监听器对象
     */
    public void addOnShowListener(@Nullable OnShowListener listener) {
        if (mShowListeners == null) {
            mShowListeners = new ArrayList<>();
        }
        mShowListeners.add(listener);
    }

    /**
     * 添加一个销毁监听器
     *
     * @param listener 监听器对象
     */
    public void addOnDismissListener(@Nullable OnDismissListener listener) {
        if (mDismissListeners == null) {
            mDismissListeners = new ArrayList<>();
        }
        mDismissListeners.add(listener);
    }

    /**
     * 移除一个销毁监听器
     *
     * @param listener 监听器对象
     */
    public void removeOnDismissListener(@Nullable OnDismissListener listener) {
        if (mDismissListeners == null) {
            return;
        }
        mDismissListeners.remove(listener);
    }

    /**
     * 移除一个显示监听器
     *
     * @param listener 监听器对象
     */
    public void removeOnShowListener(@Nullable OnShowListener listener) {
        if (mShowListeners == null) {
            return;
        }
        mShowListeners.remove(listener);
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        isAddDelayShow = false;
        if (mDismissListeners == null) {
            return;
        }

        for (int i = 0; i < mDismissListeners.size(); i++) {
            mDismissListeners.get(i).onDismiss(this);
        }
    }

    @Override
    public void onShow(DialogInterface dialog) {
        isAddDelayShow = true;
        if (mShowListeners == null) {
            return;
        }

        for (int i = 0; i < mShowListeners.size(); i++) {
            mShowListeners.get(i).onShow(this);
        }

    }


    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_START) {
            mListeners.onShow(null);
        } else if (event == Lifecycle.Event.ON_DESTROY) {
            mListeners.onDismiss(null);
        }
    }


    /**
     * Dialog 监听包装类（修复原生 Dialog 监听器对象导致的内存泄漏）
     */
    private static final class ListenersWrapper<T extends DialogInterface.OnShowListener & DialogInterface.OnDismissListener>
            extends SoftReference<T> implements DialogInterface.OnShowListener, DialogInterface.OnDismissListener {

        private ListenersWrapper(T referent) {
            super(referent);
        }

        @Override
        public void onShow(DialogInterface dialog) {
            if (get() == null) {
                return;
            }
            get().onShow(dialog);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            if (get() == null) {
                return;
            }
            get().onDismiss(dialog);
        }
    }

    /**
     * 销毁监听器
     */
    public interface OnShowListener {

        /**
         * @param dialog 弹窗
         */
        void onShow(ListenerDialogFragment dialog);
    }

    /**
     * 销毁监听器
     */
    public interface OnDismissListener {

        /**
         * Dialog 销毁了
         *
         * @param dialog 弹窗
         */
        void onDismiss(ListenerDialogFragment dialog);
    }

}

