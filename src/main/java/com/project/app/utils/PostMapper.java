package com.project.app.utils;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project.app.entities.Post;
import com.project.app.payloads.PostDto;
import com.project.app.repositories.PostLikeRepo;

// single place that turns a post entity into the dto sent to clients,
// so computed fields like like count are filled the same way everywhere
@Component
public class PostMapper {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PostLikeRepo postLikeRepo;

    public PostDto toDto(Post post) {
        PostDto postDto = this.modelMapper.map(post, PostDto.class);
        postDto.setLikeCount(this.postLikeRepo.countByPost(post));
        return postDto;
    }
}
