package org.thoughtcrime.securesms.service.sentiment.requestresponse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SentimentRequestResponse {
    public List<SentimentDocument> documents;
}
