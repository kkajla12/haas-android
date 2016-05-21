package com.twilio.ipmessaging.ui;

import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
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

    /*@ViewId(R.id.txtInfo)
    TextView txtInfo;*/

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
        StringBuilder textInfo = new StringBuilder();
        if(message != null) {
            // Use for displaying message author and delivery time info
            //String dateString = message.getTimeStamp();

            // Check for current author
            if(message.getAuthor().equals("system")) {
                body.setMovementMethod(LinkMovementMethod.getInstance());
                body.setText(parseMessage(message.getMessageBody()));
                body.setBackgroundResource(R.drawable.bubble_b);
                singleMessageContainer.setGravity(Gravity.START);
                /*if(dateString != null) {
                    textInfo.append("HaaS").append(":").append(dateString);
                }*/
            } else {
                body.setText(message.getMessageBody());
                body.setBackgroundResource(R.drawable.bubble_a);
                singleMessageContainer.setGravity(Gravity.END);
                /*if(dateString != null) {
                    textInfo.append("You").append(":").append(dateString);
                }*/
            }
            //txtInfo.setText(textInfo.toString());
        }

    }

    public interface OnMessageClickListener {
        void onMessageClicked(Message message);
    }

    public Spanned parseMessage(String jsonMessage) {
        String displayMessage = "";
        try {
            JSONObject reader = new JSONObject(jsonMessage);
            displayMessage = reader.getString("msg");
        } catch (Exception e) {
            // TODO: improve error handling
            e.printStackTrace();
        }
        return Html.fromHtml(displayMessage);
    }
}
