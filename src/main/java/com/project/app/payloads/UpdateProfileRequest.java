package com.project.app.payloads;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// every field is optional, only the ones sent are changed
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateProfileRequest {

    @Size(min = 4, max = 20, message = "name must be between 4 and 20 characters")
    private String name;

    @Size(min = 1, max = 500, message = "about must be between 1 and 500 characters")
    private String about;

}
