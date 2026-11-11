package com.project.app.payloads;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// numbers for the logged in user's own dashboard
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MyStatsDto {

    private long publishedPosts;
    private long drafts;
    private long totalViews;
    private long likesReceived;
    private long commentsReceived;
    private long followers;
    private long following;
    private long bookmarksSaved;

}
