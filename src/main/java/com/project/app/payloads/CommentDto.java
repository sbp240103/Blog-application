package com.project.app.payloads;

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
}
