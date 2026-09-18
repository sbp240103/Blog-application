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

    private String title;

    private String content;

    private String imageName;

    private Date addDate;

    private CategoryDto category;

    private UserDto user;

}
