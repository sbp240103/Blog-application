package com.project.app.payloads;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CommentDto {

    private int id;

    @NotEmpty(message = "comment content should not be empty")
    @Size(min = 2, max = 500, message = "comment content must be between 2 and 500 characters")
    private String content;

    // filled in by the server from the logged in user
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UserDto user;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;

    // id of the comment this one replies to, null for top level comments
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer parentId;
}
