package com.project.app.services;

import java.util.List;

import com.project.app.payloads.PageResponse;
import com.project.app.payloads.PostDto;

public interface PostService {

    PostDto createPost(PostDto postDto, Integer userId, Integer categoryId);
    PostDto updatePost(PostDto postDto, Integer postId);
    void deletePost(Integer postId);
    PostDto getPostById(Integer postId);
    PageResponse<PostDto> getAllPosts(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection);
    PageResponse<PostDto> getPostsByUser(Integer userId, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection);
    PageResponse<PostDto> getPostsByCategory(Integer categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection);
    PageResponse<PostDto> getPostsByTag(String tagName, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection);
    List<PostDto> searchPosts(String keyword);
}
