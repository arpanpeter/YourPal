package com.example.yourpal;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class messageAdapter extends RecyclerView.Adapter<messageAdapter.myViewHolder> {
    List<Message>messageList;
    public messageAdapter(List<Message>messageList) {
        this.messageList=messageList;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View chatView= LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item,null);
        myViewHolder myViewHolder=new myViewHolder(chatView);
        return myViewHolder;
    }

    @Override


    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        Message message = messageList.get(position);
        Log.d("MessageAdapter", "Message: " + message.getMessage() + ", Sent by: " + message.getSentBy());

        if (message.getSentBy().equals(Message.SENT_BY_ME)) {
            Log.d("MessageAdapter", "Binding to right chat view");
            holder.leftChatView.setVisibility(View.GONE);
            holder.rightChatView.setVisibility(View.VISIBLE);
            holder.rightText.setText(message.getMessage());
        } else {
            Log.d("MessageAdapter", "Binding to left chat view");
            holder.rightChatView.setVisibility(View.GONE);
            holder.leftChatView.setVisibility(View.VISIBLE);
            holder.leftText.setText(message.getMessage());
        }
    }



    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class myViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftChatView, rightChatView;
        TextView leftText, rightText;


        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            leftText=itemView.findViewById(R.id.left_Text);
            rightText=itemView.findViewById(R.id.right_Text);
            leftChatView=itemView.findViewById(R.id.left);
            rightChatView=itemView.findViewById(R.id.right);

        }
    }
}
