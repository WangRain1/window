package com.thundersoft.wearable.watchhome.notification.view;

import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thundersoft.wearable.notification.NotificationExtender;
import com.thundersoft.wearable.watchhome.R;
import com.thundersoft.wearable.watchhome.notification.presenter.NotificationRecordManager;
import com.thundersoft.wearable.watchhome.notification.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<StatusBarNotification> mData = new ArrayList<>();

    private PackageManager mManager;
    private Context mContext;
    /**
     * 0:notification list,1:notification details
     */
    private int mFrom;
    private INotification mClear;

    public void setClear(INotification clear) {
        this.mClear = clear;
    }

    public NotificationAdapter(Context context, int from) {
        this.mContext = context;
        this.mFrom = from;
        mManager = context.getPackageManager();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder = null;
        if (viewType == 0) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_head, parent, false);
            viewHolder = new ViewHolderHeader(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);

            if (mFrom == 1) {
                ViewGroup.LayoutParams params = v.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                v.setLayoutParams(params);
            }

            viewHolder = new ViewHolderItem(v);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mFrom == 0) {
            if (position == 0) {
                return 0;
            } else {
                return 1;
            }
        } else {
            if (mData.size() - 1 == position) {
                return 0;
            } else {
                return 1;
            }
        }

    }

    /**
     * Set data.
     *
     * @param list     data
     * @param position ps
     */
    public void setData(List<StatusBarNotification> list, int position) {
        mData.clear();
        mData.addAll(list);
        if (list != null && list.size() > 0) {
            mData.add(position, null);
        }
    }

    static class ViewHolderItem extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView title;
        TextView app;
        TextView text;
        RelativeLayout center;
        ImageView head;
        TextView time;

        public ViewHolderItem(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            app = itemView.findViewById(R.id.item_app);
            title = itemView.findViewById(R.id.item_title);
            text = itemView.findViewById(R.id.item_text);
            center = itemView.findViewById(R.id.center);
            head = itemView.findViewById(R.id.head);
            time = itemView.findViewById(R.id.item_time);
        }
    }

    static class ViewHolderHeader extends RecyclerView.ViewHolder {

        ImageView head;

        public ViewHolderHeader(View itemView) {
            super(itemView);
            head = itemView.findViewById(R.id.head);
        }
    }

    interface INotification {
        void showClear(boolean isNoData);

        void showDetails(String pkg);
    }
}
