package com.project.app.payloads;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// what anyone can see on a user's profile page
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProfileDto {

    private UserDto user;
    private long publishedPosts;
    private long followers;
    private long following;
    private long likesReceived;

}
