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

    private Set<CommentDto> comments = new HashSet<>();

    // computed on the server, ignored when sent by a client
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long likeCount;

}
