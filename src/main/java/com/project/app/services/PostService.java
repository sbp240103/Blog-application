package com.project.app.services;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.project.app.payloads.PageResponse;
import com.project.app.payloads.PostDto;

public interface PostService {

    PostDto createPost(PostDto postDto, Integer userId, Integer categoryId);
    PostDto updatePost(PostDto postDto, Integer postId);
    void deletePost(Integer postId);
    PostDto publishPost(Integer postId);
    PostDto unpublishPost(Integer postId);
    PostDto uploadPostImage(Integer postId, MultipartFile image) throws IOException;
    PostDto getPostById(Integer postId);
    PostDto getPostBySlug(String slug);
    PageResponse<PostDto> getAllPosts(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection);
    PageResponse<PostDto> getPostsByUser(Integer userId, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection);
    PageResponse<PostDto> getPostsByCategory(Integer categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection);
    PageResponse<PostDto> getPostsByTag(String tagName, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection);
    PageResponse<PostDto> getFeed(Integer pageNumber, Integer pageSize);
    PageResponse<PostDto> getMyDrafts(Integer pageNumber, Integer pageSize);
    List<PostDto> getTrendingPosts(Integer limit);
    PageResponse<PostDto> searchPosts(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection);
}
