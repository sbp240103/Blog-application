package com.project.app.payloads;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TagDto {

    private Integer id;

    private String name;

    // number of published posts using the tag, only filled for popular tags
    private Long postCount;

    public TagDto(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

}
