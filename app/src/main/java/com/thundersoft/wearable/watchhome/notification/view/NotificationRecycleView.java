package com.thundersoft.wearable.watchhome.notification.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NotificationRecycleView extends RecyclerView {

    private LinearLayoutManager mManager;

    public NotificationRecycleView(@NonNull Context context) {
        this(context, null);
    }

    public NotificationRecycleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NotificationRecycleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (ev.getAction() == MotionEvent.ACTION_UP) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (getChildCount() > 0) {
                        int offset = computeVerticalScrollOffset();
                        int child = getChildAt(0).getHeight();
                        int count = offset / child;
                        int os = offset - child * count;
                        int position = count;
                        if (os > child / 2) {
                            position += 1;
                        }
                        if (null != mManager) {
                            mManager.smoothScrollToPosition(NotificationRecycleView.this, null, position);
                        }
                    }
                }
            }, 50);
        }
        return super.onTouchEvent(ev);
    }

    public void setManager(LinearLayoutManager manager) {
        this.mManager = manager;
    }
}
