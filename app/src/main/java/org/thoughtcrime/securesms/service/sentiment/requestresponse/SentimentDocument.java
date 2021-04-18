package org.thoughtcrime.securesms.service.sentiment.requestresponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SentimentDocument {
    public String id;
    public String sentiment;
}
