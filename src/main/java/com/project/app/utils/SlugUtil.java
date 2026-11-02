package com.project.app.utils;

import java.text.Normalizer;
import java.util.Locale;

public class SlugUtil {

    private static final int MAX_LENGTH = 80;

    private SlugUtil() {
    }

    // "Hello World, Spring!" -> "hello-world-spring"
    public static String toSlug(String text) {
        if (text == null) {
            return "post";
        }
        String slug = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
        if (slug.length() > MAX_LENGTH) {
            slug = slug.substring(0, MAX_LENGTH).replaceAll("-+$", "");
        }
        return slug.isEmpty() ? "post" : slug;
    }
}
