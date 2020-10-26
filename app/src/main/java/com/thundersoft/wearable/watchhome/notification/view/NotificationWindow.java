package com.thundersoft.wearable.watchhome.notification.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.service.notification.StatusBarNotification;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.thundersoft.wearable.notification.NotificationExtender;
import com.thundersoft.wearable.watchhome.R;
import com.thundersoft.wearable.watchhome.notification.bean.INotify;
import com.thundersoft.wearable.watchhome.notification.presenter.NotificationRecordManager;

import java.util.List;
import java.util.Map;

public class NotificationWindow implements IWindow, ISubWindow {

    private static final String TAG = "NotificationWindow";
    private static final int DETAILS_MSG = 1;
    private final WindowManager.LayoutParams mParams;
    private View mRoot;
    private NotificationDragView mDragNotification;
    private WindowManager mWM;
    private boolean isWindowShow;
    private RelativeLayout mClear;
    private RecyclerView mDetailsList;
    private RelativeLayout mNtfDetails;
    private Context mContext;

    public NotificationWindow(Context context, int type) {
        this.mContext = context;
        mWM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        mParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
                        | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
                PixelFormat.TRANSLUCENT);
        mParams.token = new Binder();
        mParams.gravity = Gravity.TOP;
        mParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
        mParams.setTitle("nt");
        mParams.packageName = context.getPackageName();
        LayoutInflater inflate = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = inflate.inflate(R.layout.notification, null);
        mDragNotification = mRoot.findViewById(R.id.ntf_drag);
        mNtfDetails = mRoot.findViewById(R.id.ntf_details);
        mClear = mRoot.findViewById(R.id.clear_layout);
        mDetailsList = mRoot.findViewById(R.id.recycle);
        mDragNotification.setWindow(this);
        mDragNotification.setView(type);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        ViewGroup.LayoutParams pm = mDragNotification.getLayoutParams();
        pm.height = metrics.heightPixels * 2;
        ViewGroup.MarginLayoutParams mpm = (ViewGroup.MarginLayoutParams) pm;
        mpm.topMargin = -metrics.heightPixels;

        mDragNotification.setLayoutParams(pm);
    }

    public void notifyData(List<StatusBarNotification> list, int type) {
        mDragNotification.notifyData(list, type);
    }

    public void show(int type) {
        if (!isWindowShow) {
            mDragNotification.setType(type);
            mWM.addView(mRoot, mParams);
            isWindowShow = true;
            mDragNotification.scrollToPosition();
        }
    }

    public boolean isWindowShow() {
        return isWindowShow;
    }

    public boolean isExtend() {
        return mDragNotification.isExtend();
    }

    public void dismiss() {
        if (isWindowShow) {
            mWM.removeViewImmediate(mRoot);
            isWindowShow = false;
        }
    }

    @Override
    public void open() {
    }

    @Override
    public void close() {
        mDragNotification.resetLayout();
        dismiss();
    }

    @Override
    public void showDetails(final String pkg) {

        mNtfDetails.setVisibility(View.VISIBLE);
        final NotificationAdapter adapter = new NotificationAdapter(mContext, 1);
        adapter.setClear(new NotificationAdapter.INotification() {
            @Override
            public void showClear(boolean isNoData) {
                mClear.setVisibility(View.VISIBLE);
                handler.sendEmptyMessageDelayed(DETAILS_MSG, 600);
            }

            @Override
            public void showDetails(String pkg) {

            }
        });
        mDetailsList.setLayoutManager(new LinearLayoutManager(mContext));
        mDetailsList.setAdapter(adapter);
        NotificationRecordManager.getInstance().pullNotifications(NotificationExtender.FLAG_NOTIFICATION_LIST,
                new INotify() {
                    @Override
                    public void notifyEvent(final Map<String, List<StatusBarNotification>> map, int flag) {
                        if (flag != NotificationExtender.FLAG_NOTIFICATION_LIST) {
                            return;
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                List<StatusBarNotification> list = map.get(pkg);
                                if (null == list) {
                                    return;
                                }
                                adapter.setData(list, list.size());
                                if (!mDetailsList.isComputingLayout()) {
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                });

    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == DETAILS_MSG) {
                mNtfDetails.setVisibility(View.GONE);
                mClear.setVisibility(View.GONE);
            }
        }
    };

}
