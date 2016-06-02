package com.twilio.ipmessaging.ui;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.karan.haas.R;
import com.twilio.ipmessaging.Message;

import org.json.JSONObject;

import uk.co.ribot.easyadapter.ItemViewHolder;
import uk.co.ribot.easyadapter.PositionInfo;
import uk.co.ribot.easyadapter.annotations.LayoutId;
import uk.co.ribot.easyadapter.annotations.ViewId;

/**
 * Created by Karan on 5/15/2016.
 */
@LayoutId(R.layout.message_item_layout)
public class MessageViewHolder extends ItemViewHolder<Message> {

    @ViewId(R.id.body)
    TextView body;

    @ViewId(R.id.horizontalBar)
    View horizontalBar;

    @ViewId(R.id.majorInfo)
    TextView majorInfo;

    @ViewId(R.id.minorInfo)
    TextView minorInfo;

    @ViewId(R.id.singleMessageContainer)
    LinearLayout singleMessageContainer;

    View view;

    public MessageViewHolder(View view) {
        super(view);
        this.view = view;
    }

    @Override
    public void onSetListeners() {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnMessageClickListener listener = getListener(OnMessageClickListener.class);
                if (listener != null) {
                    listener.onMessageClicked(getItem());
                }
            }
        });
    }

    @Override
    public void onSetValues(Message message, PositionInfo pos) {
        if(message != null) {
            // Check for current author
            if(message.getAuthor().equals("system")) {
                body.setTypeface(Typeface.DEFAULT);
                body.setTextColor(Color.BLACK);
                singleMessageContainer.setBackgroundResource(R.drawable.rounded_corner_b);
                ((LinearLayout.LayoutParams) singleMessageContainer.getLayoutParams()).gravity = Gravity.START;
                body.setText(parseMessage(message.getMessageBody()));
            } else if(message.getAuthor().equals("custom")) {
                body.setTextColor(Color.BLACK);
                singleMessageContainer.setBackgroundResource(R.drawable.rounded_corner_b);
                ((LinearLayout.LayoutParams) singleMessageContainer.getLayoutParams()).gravity = Gravity.START;
                parseCustomMessage(message.getMessageBody());
            } else {
                body.setTypeface(Typeface.DEFAULT);
                body.setText(message.getMessageBody());
                body.setTextColor(Color.WHITE);
                singleMessageContainer.setBackgroundResource(R.drawable.rounded_corner);
                ((LinearLayout.LayoutParams) singleMessageContainer.getLayoutParams()).gravity = Gravity.END;
                horizontalBar.setVisibility(View.GONE);
                majorInfo.setVisibility(View.GONE);
                minorInfo.setVisibility(View.GONE);
            }
        }
    }

    public interface OnMessageClickListener {
        void onMessageClicked(Message message);
    }

    public String parseMessage(String jsonMessage) {
        String text = "";
        try {
            JSONObject reader = new JSONObject(jsonMessage);
            text = reader.getString("msg");
            horizontalBar.setVisibility(View.GONE);
            majorInfo.setVisibility(View.GONE);
            majorInfo.setText("");
            minorInfo.setVisibility(View.GONE);
            minorInfo.setText("");
        } catch (Exception e) {
            // TODO: improve error handling
            e.printStackTrace();
        }
        return text;
    }

    public void parseCustomMessage(String jsonMessage) {
        String text;
        String maj;
        String min;
        try {
            JSONObject reader = new JSONObject(jsonMessage);
            text = reader.getString("text");
            body.setText(text);
            body.setTypeface(Typeface.DEFAULT_BOLD);
            maj = reader.getString("majorInfo");
            if(maj.length() > 0) {
                majorInfo.setVisibility(View.VISIBLE);
                majorInfo.setText(Html.fromHtml(maj));
                horizontalBar.setVisibility(View.VISIBLE);
            } else {
                majorInfo.setVisibility(View.GONE);
                horizontalBar.setVisibility(View.GONE);
            }
            min = reader.getString("minorInfo");
            if(min.length() > 0) {
                minorInfo.setVisibility(View.VISIBLE);
                minorInfo.setText(min);
                minorInfo.setGravity(Gravity.END);
            } else {
                minorInfo.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
