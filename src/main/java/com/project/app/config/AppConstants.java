package com.project.app.config;

import java.util.Set;

public class AppConstants {

    public static final Integer NORMAL_USER = 502;
    public static final Integer ADMIN_USER = 501;

    public static final String ROLE_NORMAL = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    // pagination defaults used by the listing endpoints
    public static final String PAGE_NUMBER = "0";
    public static final String PAGE_SIZE = "10";
    public static final String SORT_DIRECTION = "asc";

    // only these fields may be used in sortBy, anything else (like password) is rejected
    public static final Set<String> USER_SORT_FIELDS = Set.of("id", "name", "email");
    public static final Set<String> CATEGORY_SORT_FIELDS = Set.of("categoryId", "categoryTitle");
    public static final Set<String> POST_SORT_FIELDS = Set.of("postId", "title", "addDate", "viewCount");

}
