package com.project.app.payloads;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// everything the platform stores about the logged in user
@NoArgsConstructor
@Getter
@Setter
public class DataExportDto {

    private LocalDateTime exportedAt = LocalDateTime.now();
    private UserDto profile;
    private List<PostDto> posts;
    private List<CommentDto> comments;
    private List<Integer> bookmarkedPostIds;
    private Set<Integer> followingUserIds;
    private long followers;

}
