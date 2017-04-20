package co.transcender.personal_chef.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;
import ai.api.model.Status;
import ai.api.ui.AIButton;
import co.transcender.personal_chef.R;
import co.transcender.personal_chef.adapter.ChatAdapter;
import co.transcender.personal_chef.constants.Consts;
import co.transcender.personal_chef.others.TTS;
import co.transcender.personal_chef.pojo.ChatMessage;

public class MainActivity extends AppCompatActivity {

    private static final String[] PERMISSIONS = {android.Manifest.permission.RECORD_AUDIO};
    AIButton aiButton;
    Button voice, send;
    EditText editText;
    private ListView messagesContainer;
    private ChatAdapter adapter;
    private String TAG = "MainActivity";
    private AIDataService aiDataService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        aiButton = (AIButton) findViewById(R.id.micButton);
        voice = (Button) findViewById(R.id.voice);
        send = (Button) findViewById(R.id.send);
        editText = (EditText) findViewById(R.id.editText);
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);

//        adapter = new ChatAdapter(MainActivity.this, new ArrayList<ChatMessage>());
//        messagesContainer.setAdapter(adapter);

        TTS.init(getApplicationContext());

        voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askSpeechInput();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, String.valueOf(editText.getText().toString().length()));
                if (editText.getText().toString().length() > 0) {
                    sendRequest();
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setMe(false);
                    chatMessage.setMessage(editText.getText().toString());
                    chatMessage.setDate(new SimpleDateFormat("hh:mm a").format(new Date()));
                    setCallerDetail(chatMessage);
                    editText.setText("");
                }
            }
        });

//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1)
//            permissionCheck();
//        else
//            apiAi();

    }

    public void setCallerDetail(ChatMessage message) {
        if (adapter != null) {
            adapter.add(message);
            adapter.notifyDataSetChanged();
            scroll();
        } else {
            ArrayList<ChatMessage> list = new ArrayList<ChatMessage>();
            list.add(message);
            adapter = new ChatAdapter(MainActivity.this, list);
            messagesContainer.setAdapter(adapter);
            adapter.add(message);
            adapter.notifyDataSetChanged();
            scroll();
        }
    }

    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

    public void apiAi() {

        final AIConfiguration config = new AIConfiguration(Consts.API_AI_ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiButton.initialize(config);

        aiButton.setResultsListener(new AIButton.AIButtonListener() {
            @Override
            public void onResult(final AIResponse response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("ApiAi", response.toString());

                        Log.d(TAG, "onResult");

//                        resultTextView.setText(gson.toJson(response));

                        Log.i(TAG, "Received success response");

                        // this is example how to get different parts of result object
                        final Status status = response.getStatus();
                        Log.i(TAG, "Status code: " + status.getCode());
                        Log.i(TAG, "Status type: " + status.getErrorType());

                        final Result result = response.getResult();
                        Log.i(TAG, "Resolved query: " + result.getResolvedQuery());

                        Log.i(TAG, "Action: " + result.getAction());
                        final String speech = result.getFulfillment().getSpeech();
                        Log.i(TAG, "Speech: " + speech);
                        TTS.speak(speech);

                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.setMe(true);
                        chatMessage.setMessage(speech);
                        chatMessage.setDate(new SimpleDateFormat("hh:mm a").format(new Date()));
                        setCallerDetail(chatMessage);

                        final Metadata metadata = result.getMetadata();
                        if (metadata != null) {
                            Log.i(TAG, "Intent id: " + metadata.getIntentId());
                            Log.i(TAG, "Intent name: " + metadata.getIntentName());
                        }

                        final HashMap<String, JsonElement> params = result.getParameters();
                        if (params != null && !params.isEmpty()) {
                            Log.i(TAG, "Parameters: ");
                            for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                                Log.i(TAG, String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
                            }
                        }

                    }
                });
            }

            @Override
            public void onError(final AIError error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Log.d("ApiAi", error.getMessage());
                        // TODO process error here
                    }
                });
            }

            @Override
            public void onCancelled() {

            }
        });
    }

    private void sendRequest() {

        final String queryString = editText.getText().toString();
//        final String contextString = String.valueOf(contextEditText.getText());
        AIConfiguration config = new AIConfiguration(Consts.API_AI_ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiDataService = new AIDataService(MainActivity.this, config);

        final AsyncTask<String, Void, AIResponse> task = new AsyncTask<String, Void, AIResponse>() {

            private AIError aiError;

            @Override
            protected AIResponse doInBackground(final String... params) {
                final AIRequest request = new AIRequest();
                String query = params[0];
//                String event = params[1];
//
//                if (!TextUtils.isEmpty(query))
                request.setQuery(query);
//                if (!TextUtils.isEmpty(event))
//                    request.setEvent(new AIEvent(event));
//                final String contextString = params[2];
//                RequestExtras requestExtras = null;
//                if (!TextUtils.isEmpty(contextString)) {
//                    final List<AIContext> contexts = Collections.singletonList(new AIContext(contextString));
//                    requestExtras = new RequestExtras(contexts, null);
//                }

                try {
                    return aiDataService.request(request);
                } catch (final AIServiceException e) {
                    aiError = new AIError(e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final AIResponse response) {
                if (response != null) {
                    onResult(response);
                } else {
                    onError(aiError);
                }
            }
        };

//        task.execute(queryString, eventString, contextString);
        task.execute(queryString);
    }

    private void onResult(final AIResponse response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onResult");

                Log.i(TAG, "Received success response");

                // this is example how to get different parts of result object
                final Status status = response.getStatus();
                Log.i(TAG, "Status code: " + status.getCode());
                Log.i(TAG, "Status type: " + status.getErrorType());

                final Result result = response.getResult();
                Log.i(TAG, "Resolved query: " + result.getResolvedQuery());

                Log.i(TAG, "Action: " + result.getAction());

                final String speech = result.getFulfillment().getSpeech();
                Log.i(TAG, "Speech: " + speech);
                TTS.speak(speech);

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setMe(true);
                chatMessage.setMessage(speech);
                chatMessage.setDate(new SimpleDateFormat("hh:mm a").format(new Date()));
                setCallerDetail(chatMessage);

                final Metadata metadata = result.getMetadata();
                if (metadata != null) {
                    Log.i(TAG, "Intent id: " + metadata.getIntentId());
                    Log.i(TAG, "Intent name: " + metadata.getIntentName());
                }

                final HashMap<String, JsonElement> params = result.getParameters();
                if (params != null && !params.isEmpty()) {
                    Log.i(TAG, "Parameters: ");
                    for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                        Log.i(TAG, String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
                    }
                }
            }

        });
    }

    private void onError(final AIError error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, error.toString());

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setMe(true);
                chatMessage.setMessage(error.toString());
                chatMessage.setDate(new SimpleDateFormat("hh:mm a").format(new Date()));
                setCallerDetail(chatMessage);
            }
        });
    }

    private void setCallerDetail(LinearLayout parentLayout, String side, String value) {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView;
        if (side.equalsIgnoreCase("bot"))
            convertView = layoutInflater.inflate(R.layout.botchat, parentLayout, false);
        else
            convertView = layoutInflater.inflate(R.layout.mychat, parentLayout, false);
        TextView tv_key = (TextView) convertView.findViewById(R.id.chat);
        tv_key.setText(value);
        if (parentLayout != null) {
            parentLayout.addView(convertView);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Success");
//                    apiAi();
                } else {
                    permissionCheck();
                    Toast.makeText(MainActivity.this, "This application needs permissions to function properly", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Failed");
                }
                break;
        }
    }

    private void permissionCheck() {

        List<String> missingPermissions = getMissingPermissions(PERMISSIONS);

        if (missingPermissions.isEmpty()) {
//            apiAi();
        } else {
            ActivityCompat.requestPermissions(this,
                    missingPermissions.toArray(new String[missingPermissions.size()]),
                    1);
        }
    }

    private List<String> getMissingPermissions(String[] requiredPermissions) {
        List<String> missingPermissions = new ArrayList<>();
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        return missingPermissions;
    }

    // Showing google speech input dialog

    private void askSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Hi speak something");
        try {
            startActivityForResult(intent, 10);
        } catch (ActivityNotFoundException a) {

        }
    }

    // Receiving speech input

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 10: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    editText.setText(result.get(0));

//                    ChatMessage chatMessage = new ChatMessage();
//                    chatMessage.setMe(false);
//                    chatMessage.setMessage(result.get(0));
//                    chatMessage.setDate(new SimpleDateFormat("hh:mm a").format(new Date()));
//                    setCallerDetail(chatMessage);

                }
                break;
            }

        }
    }
}
