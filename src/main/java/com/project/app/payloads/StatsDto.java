package com.project.app.payloads;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// numbers for an admin dashboard
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StatsDto {

    private long users;
    private long posts;
    private long comments;
    private long categories;
    private long tags;
    private long likes;
    private long bookmarks;
    private long follows;

}
