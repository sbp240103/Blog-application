package com.project.app.payloads;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import com.project.app.entities.PostStatus;

@NoArgsConstructor 
@Getter 
@Setter 
public class PostDto {

    private Integer postId;

    @NotEmpty(message = "post title should not be empty")
    @Size(min = 4, max = 100, message = "post title must be between 4 and 100 characters")
    private String title;

    @NotEmpty(message = "post content should not be empty")
    @Size(min = 10, max = 1000, message = "post content must be between 10 and 1000 characters")
    private String content;

    private String imageName;

    private Date addDate;

    private CategoryDto category;

    private UserDto user;

    // DRAFT or PUBLISHED, a new post is published unless DRAFT is sent
    private PostStatus status;

    // future time to publish a draft automatically
    private LocalDateTime publishAt;

    private Set<CommentDto> comments = new HashSet<>();

    // e.g. ["java", "spring"], tags are created on first use.
    // left out on update means keep the current tags, an empty list removes them
    @Size(max = 10, message = "a post can have at most 10 tags")
    private Set<String> tagNames;

    // computed on the server, ignored when sent by a client
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long likeCount;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long commentCount;

    // only filled for logged in viewers, false for anonymous requests
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean likedByMe;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean bookmarkedByMe;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer viewCount;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime updatedAt;

}
