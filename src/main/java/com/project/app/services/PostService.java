package com.project.app.services;

import java.util.List;

import com.project.app.payloads.PostDto;
import com.project.app.payloads.PostResponse;

public interface PostService {

    PostDto createPost(PostDto postDto, Integer userId, Integer categoryId);
    PostDto updatePost(PostDto postDto, Integer postId);
    void deletePost(Integer postId);
    PostDto getPostById(Integer postId);
    PostResponse getAllPosts(Integer pageSize, Integer pageNumber, String sortBy, String sortDirection);
    List<PostDto> getPostsByUser(Integer userId);
    List<PostDto> getPostsByCategory(Integer categoryId);
    List<PostDto> searchPosts(String keyword);
}
