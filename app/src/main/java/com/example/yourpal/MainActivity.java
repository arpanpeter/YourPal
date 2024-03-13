package com.example.yourpal;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    TextView welcomeTextView;
    EditText messageEditText;
    ImageView imageView;
    ImageButton sendButton;
    List<Message> messageList;
    messageAdapter messageAdapter;
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messageList = new ArrayList<>();

        recyclerView = findViewById(R.id.recycler_View);
        welcomeTextView = findViewById(R.id.Welcome);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send);
        imageView = findViewById(R.id.imageView2);
        messageAdapter = new messageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);
        sendButton.setOnClickListener((v) -> {
            String question = messageEditText.getText().toString().trim();
            addToChat(question, Message.SENT_BY_ME);
            messageEditText.setText("");
            callAPI(question);
            welcomeTextView.setVisibility(View.GONE);
            imageView.setVisibility(View.INVISIBLE);
        });
    }

    void addToChat(String message, String sentBy) {
        runOnUiThread(() -> {
            messageList.add(new Message(message, sentBy));
            messageAdapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
        });
    }
    void addResponse(String response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!messageList.isEmpty() && messageList.get(messageList.size() - 1).getMessage().equals("Typing...")) {
                    messageList.remove(messageList.size() - 1);
                }
                messageList.add(new Message(response, Message.SENT_BY_BOT));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
    }
    void callAPI(String question) {
        addToChat("Typing...", Message.SENT_BY_BOT);

        JSONArray messagesArray = new JSONArray();
        try {
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", question);
            messagesArray.put(userMessage);

            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are a helpful assistant.");
            messagesArray.put(systemMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "gpt-3.5-turbo");
            jsonBody.put("messages", messagesArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder().url("https://api.openai.com/v1/chat/completions").header("Authorization", "Bearer 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").post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray choicesArray = jsonObject.getJSONArray("choices");
                        if (choicesArray.length() > 0) {
                            JSONObject choiceObject = choicesArray.getJSONObject(0);
                            JSONObject messageObject = choiceObject.getJSONObject("message");
                            String result = messageObject.getString("content");
                            addResponse(result.trim());
                        } else {
                            addResponse("No response received from the server.");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        addResponse("Failed to parse response: " + e.getMessage());
                    }
                } else {
                    addResponse("Failed to load response due to " + response.body().string());
                }
            }

        });
    }
}
