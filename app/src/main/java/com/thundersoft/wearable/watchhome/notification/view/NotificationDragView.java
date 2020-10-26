package com.thundersoft.wearable.watchhome.notification.view;

import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.service.notification.StatusBarNotification;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.view.MotionEventCompat;
import androidx.customview.widget.ViewDragHelper;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.thundersoft.wearable.watchhome.R;
import com.thundersoft.wearable.watchhome.notification.utils.Constants;

import java.util.List;

public class NotificationDragView extends RelativeLayout {

    private ViewDragHelper dragHelper;
    private int height;
    private Context mContext;
    private NotificationAdapter adapter;
    private NotificationRecycleView rv;
    private LinearLayoutManager manager;
    /**
     * 0:float pattern new notification icon style.
     * 1:float pattern new notification icon and title style.
     * 2:notification list style.
     */
    private int mType = -1;
    private PackageManager mManager;
    private boolean isExtend = false;
    private int mVdhYOffset;
    private Handler handler = new Handler();
    private ISubWindow mIWindow;
    private View root;
    private ImageView icon;
    private TextView appName;
    private TextView titleText;
    private TextView timeText;
    private ImageView mClearIcon;
    private TextView mTips;
    private RelativeLayout mClearLayout;
    private RelativeLayout ntf;
    private View floatView = null;

    public void setWindow(ISubWindow wd) {
        this.mIWindow = wd;
    }

    public NotificationDragView(Context context) {
        this(context, null);
    }

    public NotificationDragView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NotificationDragView(final Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        height = metrics.heightPixels;
        mManager = context.getPackageManager();
    }

    public void init() {

        dragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return child == root;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                return Math.min(top, height);
            }

            @Override
            public int getViewVerticalDragRange(View child) {
                if (child == root) {

                    return height;
                }
                return super.getViewVerticalDragRange(child);
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);

                if (direction > 0) {
                    if (releasedChild.getTop() > height / 3) {
                        if (!isExtend) {
                            openWindow();
                            isExtend = true;
                        }
                        dragHelper.smoothSlideViewTo(root, 0, height);
                    } else {
                        isExtend = false;
                        closeWindow();
                        dragHelper.smoothSlideViewTo(root, 0, 0);
                    }
                }
                if (direction < 0) {
                    if (!isExtend && mType != 2) {
                        dragHelper.smoothSlideViewTo(root, 0, -Constants.NOTIFICATION_FLOAT_DETAILS);
                        invalidate();
                        closeWindow();
                        return;
                    }
                    if ((height - releasedChild.getTop()) > (root.getHeight() - height) / 3) {
                        dragHelper.smoothSlideViewTo(root, 0, 0);
                        if (isExtend) {
                            closeWindow();
                            isExtend = false;
                        }
                    } else {
                        if (!isExtend) {
                            openWindow();
                            isExtend = true;
                        }
                        dragHelper.smoothSlideViewTo(root, 0, height);
                    }
                }
                invalidate();
            }

            int direction = 0;

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                super.onViewPositionChanged(changedView, left, top, dx, dy);
                direction = dy;
            }
        });
        dragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_ALL);
    }

    public boolean isExtend() {
        return isExtend;
    }

    public void resetLayout() {
        dragHelper.smoothSlideViewTo(root, 0, 0);
    }

    private void closeWindow() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mIWindow != null) {

                    mIWindow.close();
                }
            }
        }, 500);
    }

    private void openWindow() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mIWindow != null) {

                    mIWindow.open();
                }
            }
        }, 500);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (dragHelper.continueSettling(true)) {
            postInvalidate();
        } else {
            mVdhYOffset = root.getTop();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        root.offsetTopAndBottom(mVdhYOffset);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void setView(int type) {
        inflate(type);
    }

    private void inflate(int type) {
        root = LayoutInflater.from(getContext()).inflate(R.layout.notification_list, this, false);
        mClearLayout = root.findViewById(R.id.clear_layout);
        mClearLayout.setVisibility(View.VISIBLE);
        mClearIcon = root.findViewById(R.id.clear_icon);
        mTips = root.findViewById(R.id.tips);
        mClearIcon.setImageResource(R.drawable.no_notification);
        mTips.setText(R.string.no_ntf);
        ntf = root.findViewById(R.id.ntf_list);
        rv = root.findViewById(R.id.list);
        rv.setVisibility(View.INVISIBLE);
        manager = new LinearLayoutManager(mContext);
        rv.setLayoutManager(manager);
        adapter = new NotificationAdapter(mContext, 0);
        adapter.setClear(new NotificationAdapter.INotification() {
            @Override
            public void showClear(boolean isNoData) {
                mClearLayout.setVisibility(View.VISIBLE);
                rv.setVisibility(View.INVISIBLE);
            }

            @Override
            public void showDetails(String pkg) {
                mIWindow.showDetails(pkg);
            }
        });
        SwipeCallback sw = new SwipeCallback(adapter);
        ItemTouchHelper th = new ItemTouchHelper(sw);
        th.attachToRecyclerView(rv);
        rv.setAdapter(adapter);
        rv.setManager(manager);

        ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) rv.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = height;
        rv.setLayoutParams(params);
        floatView = LayoutInflater.from(getContext()).inflate(R.layout.float_ntf, ntf, false);
        setType(type);
        init();
    }

    private void calFloat() {
        removeView(root);
        int outHeight = initFloatView(ntf);
        ViewGroup.LayoutParams pa = (ViewGroup.LayoutParams) root.getLayoutParams();
        pa.width = ViewGroup.LayoutParams.MATCH_PARENT;
        pa.height = outHeight;
        root.setLayoutParams(pa);
        addView(root, pa);
    }

    private int initFloatView(RelativeLayout ntf) {

        int outHeight = height * 2;
        RelativeLayout.LayoutParams params = new LayoutParams(ntf.getLayoutParams());
        ViewGroup.MarginLayoutParams pms = new MarginLayoutParams(params);
        pms.topMargin = height;

        switch (mType) {
            case Constants.WINDOW_TYPE_FLOAT_ICON:
                ntf.removeView(floatView);
                icon = floatView.findViewById(R.id.icon);
                ntf.addView(floatView, pms);
                outHeight = height + Constants.NOTIFICATION_FLOAT_ICON;
                break;
            case Constants.WINDOW_TYPE_FLOAT_DETAILS:
                ntf.removeView(floatView);
                appName = floatView.findViewById(R.id.item_app);
                appName.setVisibility(View.VISIBLE);
                titleText = floatView.findViewById(R.id.item_title);
                titleText.setVisibility(View.VISIBLE);
                timeText = floatView.findViewById(R.id.item_time);
                timeText.setVisibility(View.VISIBLE);
                icon = floatView.findViewById(R.id.icon);
                ntf.addView(floatView, pms);
                outHeight = height + Constants.NOTIFICATION_FLOAT_DETAILS;
                break;
            case Constants.WINDOW_TYPE_LIST:
                ntf.removeView(floatView);
                break;
        }
        return outHeight;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            dragHelper.cancel();
            return false;
        }
        if (!(manager.findFirstVisibleItemPosition() == (adapter.getItemCount() - 1))) {
            return false;
        }
        return dragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dragHelper.processTouchEvent(event);
        return true;
    }

    public void setType(int type) {
        if (dragHelper != null && !isExtend) {
            dragHelper.smoothSlideViewTo(root, 0, 0);
        }
        this.mType = type;
        calFloat();
    }

    public void notifyData(List<StatusBarNotification> list, int type) {
        setType(type);
        if (list.size() == 0) {
            mClearLayout.setVisibility(View.VISIBLE);
            rv.setVisibility(View.INVISIBLE);
        } else {
            mClearLayout.setVisibility(View.INVISIBLE);
            rv.setVisibility(View.VISIBLE);
        }

        switch (mType) {
            case Constants.WINDOW_TYPE_FLOAT_ICON:
                StatusBarNotification sbt = getFirst(list);
                if (null != sbt) {
                    Icon res = sbt.getNotification().getSmallIcon();
                    icon.setImageIcon(res);
                }
                break;
            case Constants.WINDOW_TYPE_FLOAT_DETAILS:
                StatusBarNotification sb = getFirst(list);
                if (null != sb) {
                    sb.getPackageName();
                    Bundle bundle = sb.getNotification().extras;
                    ApplicationInfo app = bundle.getParcelable(Notification.EXTRA_BUILDER_APPLICATION_INFO);
                    CharSequence name = mManager.getApplicationLabel(app);
                    String title = bundle.getString(Notification.EXTRA_TITLE);
                    appName.setText(name);
                    titleText.setText(title);
                    icon.setImageIcon(sb.getNotification().getSmallIcon());
                }
                break;
            case Constants.WINDOW_TYPE_LIST:
                break;
        }
        adapter.setData(list, 0);
        adapter.notifyDataSetChanged();
        scrollToPosition();
    }

    private StatusBarNotification getFirst(List<StatusBarNotification> list) {
        if (null == list || list.size() <= 0) {
            return null;
        }
        StatusBarNotification sbt = list.get(list.size() - 1);
        return sbt;
    }

    public void scrollToPosition() {
        manager.scrollToPosition(adapter.getItemCount() - 1);
    }
}
