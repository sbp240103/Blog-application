package com.project.app.payloads;

import java.sql.Date;

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
    private String content;

    private String imageName;

    private Date addDate;

    private CategoryDto category;

    private UserDto user;

}
