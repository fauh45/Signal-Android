package org.thoughtcrime.securesms.service.sentiment;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;


import androidx.annotation.RequiresApi;

import com.detectlanguage.DetectLanguage;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.signal.core.util.logging.Log;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.conversation.ConversationItemBodyBubble;
import org.thoughtcrime.securesms.service.sentiment.requestresponse.SentimentRequestResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@RequiresApi(api = Build.VERSION_CODES.N)
public class SentimentService {

    private final static String TAG = SentimentService.class.getSimpleName();

    private final static Set<String> LANGUAGE_SUPPORT = new HashSet<String>(
            Arrays.asList("zh-hans", "zh", "zh-hant", "nl", "en", "fr",
                    "de", "hi", "it", "ja", "ko", "no", "pt-BR", "pt", "pt-PT",
                    "es", "tr"));
    private final static String API_ENDPOINT = "https://signal-text-analytics.cognitiveservices.azure.com";
    private final static String API_REGION = "southeastasia";
    private final static String API_KEY = "d3a0871471ba4b9f91a3b8e76d075124";

    private final Context context;
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public SentimentService(Context context) {
        this.context = context;
    }

    private String getLanguage(String message) throws Exception {
        return "en";
    }

    Call postToAzure(String url, String jsonBody, Callback callback) {
        final MediaType jsonMediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(jsonMediaType, jsonBody);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Ocp-Apim-Subscription-Key", API_KEY)
                .addHeader("Ocp-Apim-Subscription-Region", API_REGION)
                .addHeader("Content-type", "application/json")
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);

        return call;
    }

    public void setColorBasedOnSentiment(String message, ConversationItemBodyBubble bodyBubble) {
        final String TEXT_ANALYTICS_ENDPOINT = API_ENDPOINT +
                "/text/analytics/v3.0/sentiment";
        bodyBubble.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.conversation_blue)));

        try {
            String messageLanguage = getLanguage(message);

            postToAzure(TEXT_ANALYTICS_ENDPOINT,
                    "{\"documents\":[{\"language\":\"" + messageLanguage +
                            "\",\"id\":\"1\",\"text\":\"" + message + "\"}]}",
                    new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e(TAG, e);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                SentimentRequestResponse sentimentResponse = mapper.readValue(response.body().string(), SentimentRequestResponse.class);
                                String sentiment = sentimentResponse.documents.get(0).sentiment;

                                switch (sentiment) {
                                    case "negative":
                                        bodyBubble.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.conversation_crimson)));
                                        break;
                                    case "positive":
                                        bodyBubble.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.conversation_wintergreen)));
                                        break;
                                    case "mixed":
                                        bodyBubble.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.conversation_burlap)));
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, e);
        }
    }
}
