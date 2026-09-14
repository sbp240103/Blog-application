package com.project.app.payloads;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor 
@Getter 
@Setter 
public class CategoryDto {

    private Integer CategoryId;

    @NotEmpty(message = "category title should not be empty")
    @Size(min = 4, max = 100, message = "category title must be between 4 and 100 characters")
    private String categoryTitle;

    @NotEmpty(message = "category description should not be empty")
    @Size(min = 10, max = 500, message = "category description must be between 10 and 500 characters")
    private String categoryDescription;
    
}
