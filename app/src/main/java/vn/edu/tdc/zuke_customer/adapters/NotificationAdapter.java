package vn.edu.tdc.zuke_customer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.squareup.picasso.Picasso;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import vn.edu.tdc.zuke_customer.R;
import vn.edu.tdc.zuke_customer.data_models.Notification;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    Context context;
    ArrayList<Notification> list;
    ItemClickListener itemClickListener;
    ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public NotificationAdapter(Context context, ArrayList<Notification> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_notification, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification item = list.get(position);
        holder.title.setText(item.getTitle());
        holder.content.setText(item.getContent());
        Date now = new Date();
        holder.created_at.setText(timeDiff(item.getCreated_at(), now));
        if(!item.getImage().equals("")) Picasso.get().load(item.getImage()).fit().into(holder.itemImage);

        viewBinderHelper.bind(holder.swipeRevealLayout, item.getKey());
        holder.cardView.setOnClickListener(v -> {
            if(itemClickListener != null) {
                itemClickListener.delete(item.getKey());
            }
        });
    }

    private String timeDiff(String created, Date now) {
        String time = "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date create = sdf.parse(created, new ParsePosition(0));
        long diff = now.getTime() - create.getTime();
        int minute = (int) (diff / (1000 * 60));

        if (minute == 0) {
            time = "Bây giờ";
        } else if (minute <= 59) {
            time = minute + " phút trước";
        } else if (minute >= 60) {
            int hour = (int) (diff / (1000 * 60 * 60));

            if (hour <= 23) {
                time = hour + " giờ trước";
            } else {
                int day = (int) (diff / (1000 * 60 * 60 * 24));
                time = day + " ngày trước";
            }
        }
        return time;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        SwipeRevealLayout swipeRevealLayout;
        CardView cardView;
        private ImageView itemImage;
        private TextView title, content, created_at;
        View.OnClickListener onClickListener;

        public ViewHolder(View view) {
            super(view);
            swipeRevealLayout = view.findViewById(R.id.swipelayout);
            cardView = view.findViewById(R.id.cardView);
            itemImage = view.findViewById(R.id.img);
            title = view.findViewById(R.id.txt_title);
            content = view.findViewById(R.id.txt_content);
            created_at = view.findViewById(R.id.txt_time);
        }
    }

    public interface ItemClickListener {
        void delete(String id);
    }
}
