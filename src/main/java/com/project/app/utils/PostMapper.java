package com.project.app.utils;

import java.util.TreeSet;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project.app.entities.Post;
import com.project.app.entities.Tag;
import com.project.app.payloads.PostDto;
import com.project.app.repositories.PostLikeRepo;
import com.project.app.repositories.CommentRepo;

// single place that turns a post entity into the dto sent to clients,
// so computed fields like like count are filled the same way everywhere
@Component
public class PostMapper {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PostLikeRepo postLikeRepo;

    @Autowired
    private CommentRepo commentRepo;

    public PostDto toDto(Post post) {
        PostDto postDto = this.modelMapper.map(post, PostDto.class);
        postDto.setLikeCount(this.postLikeRepo.countByPost(post));
        postDto.setCommentCount(this.commentRepo.countByPost(post));
        postDto.setTagNames(post.getTags().stream().map(Tag::getName).collect(Collectors.toCollection(TreeSet::new)));
        return postDto;
    }
}
