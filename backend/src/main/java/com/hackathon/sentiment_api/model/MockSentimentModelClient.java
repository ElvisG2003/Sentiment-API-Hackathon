package com.hackathon.sentiment_api.model;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import org.springframework.stereotype.Component;
//Este es modelo NO ES EL REAL, es solo un mock para avanzar

@Component
@ConditionalOnProperty(name="sentiment.model.client", havingValue="mock", matchIfMissing=true)
public class MockSentimentModelClient implements SentimentModelClient{

    private static final List<String> NEGATIVE_KEYWORDS =
            List.of("bad","terrible","awful","hate", "worst");

    @Override
    public ModelResult predict(String text){

        String lower = text.toLowerCase();

        boolean isNegative = NEGATIVE_KEYWORDS.stream().anyMatch(lower::contains);

        if (isNegative){
            return new ModelResult(0, 0.85);
        }

        return new ModelResult(1, 0.85);
    }
}
