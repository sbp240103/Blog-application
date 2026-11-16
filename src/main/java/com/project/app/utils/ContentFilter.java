package com.project.app.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.project.app.exceptions.ApiException;

// rejects posts and comments containing configured spam or abusive phrases
@Component
public class ContentFilter {

    private final List<Pattern> blocked;

    public ContentFilter(@Value("${app.moderation.banned-words:buy followers,free money,click here to win}") String bannedWords) {
        this.blocked = Arrays.stream(bannedWords.split(","))
                .map(String::trim)
                .filter(word -> !word.isEmpty())
                .map(word -> Pattern.compile("\\b" + Pattern.quote(word.toLowerCase(Locale.ROOT)) + "\\b"))
                .toList();
    }

    public void check(String... texts) {
        for (String text : texts) {
            if (text == null) {
                continue;
            }
            String lower = text.toLowerCase(Locale.ROOT);
            for (Pattern pattern : this.blocked) {
                if (pattern.matcher(lower).find()) {
                    throw new ApiException("content contains words that are not allowed");
                }
            }
        }
    }
}
