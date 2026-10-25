package com.project.app.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project.app.config.AppConstants;
import com.project.app.payloads.PageResponse;
import com.project.app.payloads.PostDto;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import com.project.app.services.FileService;
import com.project.app.services.PostService;
import com.project.app.payloads.ApiResponse;

@RestController
@RequestMapping("/api")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired 
    private FileService fileService;

    @Value ("${project.image}") 
    private String path;

    // create post
    @PostMapping ("/user/{userId}/category/{categoryId}/posts")
    public ResponseEntity<PostDto> createPost(@Valid @RequestBody PostDto postDto, @PathVariable("userId") Integer userId, @PathVariable("categoryId") Integer categoryId) {
        PostDto createdPost = this.postService.createPost(postDto, userId, categoryId);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    // update post

    @PutMapping ("/posts/{postId}")
    public ResponseEntity<PostDto> updatePost(@Valid @RequestBody PostDto postDto, @PathVariable ("postId") Integer postId){
        PostDto updatedPost = this.postService.updatePost(postDto, postId);
        return ResponseEntity.ok(updatedPost);
    }

    // delete post

    @DeleteMapping ("/posts/{postId}")
    public ResponseEntity<ApiResponse> deletePost(@PathVariable("postId") Integer postId){
        this.postService.deletePost(postId);
        return new ResponseEntity<>(new ApiResponse("post deleted successfully", true), HttpStatus.OK);
    }

    // get all posts
    @GetMapping ("/posts")
    public ResponseEntity<PageResponse<PostDto>> getAllPosts(
        @RequestParam (value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
        @RequestParam (value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
        @RequestParam (value = "sortBy", defaultValue = "postId", required = false) String sortBy,
        @RequestParam (value = "sortDirection", defaultValue = AppConstants.SORT_DIRECTION, required = false) String sortDirection
    ){
        return ResponseEntity.ok(this.postService.getAllPosts(pageNumber, pageSize, sortBy, sortDirection));
    }

    // get single post
    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostDto> getPostById(@PathVariable("postId") Integer postId){
        return ResponseEntity.ok(this.postService.getPostById(postId));
    }

    // get posts by user
    @GetMapping("/user/{userId}/posts")
    public ResponseEntity<PageResponse<PostDto>> getPostsByUser(
        @PathVariable("userId") Integer userId,
        @RequestParam (value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
        @RequestParam (value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
        @RequestParam (value = "sortBy", defaultValue = "postId", required = false) String sortBy,
        @RequestParam (value = "sortDirection", defaultValue = AppConstants.SORT_DIRECTION, required = false) String sortDirection
    ){
        return ResponseEntity.ok(this.postService.getPostsByUser(userId, pageNumber, pageSize, sortBy, sortDirection));
    }

    // get posts by category
    @GetMapping("/category/{categoryId}/posts")
    public ResponseEntity<PageResponse<PostDto>> getPostsByCategory(
        @PathVariable("categoryId") Integer categoryId,
        @RequestParam (value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
        @RequestParam (value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
        @RequestParam (value = "sortBy", defaultValue = "postId", required = false) String sortBy,
        @RequestParam (value = "sortDirection", defaultValue = AppConstants.SORT_DIRECTION, required = false) String sortDirection
    ){
        return ResponseEntity.ok(this.postService.getPostsByCategory(categoryId, pageNumber, pageSize, sortBy, sortDirection));
    }

    // posts from followed users
    @GetMapping("/feed")
    public ResponseEntity<PageResponse<PostDto>> getFeed(
        @RequestParam (value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
        @RequestParam (value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
    ){
        return ResponseEntity.ok(this.postService.getFeed(pageNumber, pageSize));
    }

    // search posts
    @GetMapping("/posts/search/{keyword}")
    public ResponseEntity<PageResponse<PostDto>> searchPosts(
        @PathVariable("keyword") String keyword,
        @RequestParam (value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
        @RequestParam (value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
        @RequestParam (value = "sortBy", defaultValue = "postId", required = false) String sortBy,
        @RequestParam (value = "sortDirection", defaultValue = "desc", required = false) String sortDirection
    ){
        return ResponseEntity.ok(this.postService.searchPosts(keyword, pageNumber, pageSize, sortBy, sortDirection));
    }

    // post image upload
    @PostMapping ("/post/image/upload/{postId}")
    public ResponseEntity<PostDto> uploadPostImage(
        @RequestParam ("image") MultipartFile image,
        @PathVariable Integer postId
    ) throws IOException {
        return ResponseEntity.ok(this.postService.uploadPostImage(postId, image));
    }
    
    // serve files
    @GetMapping ("/post/image/{imageName}")
    public void downloadImage(
        @PathVariable ("imageName") String imageName,
        HttpServletResponse response
    ) throws IOException{
        try (InputStream resource = this.fileService.getResource(path, imageName)) {
            boolean isPng = imageName.toLowerCase().endsWith(".png");
            response.setContentType(isPng ? MediaType.IMAGE_PNG_VALUE : MediaType.IMAGE_JPEG_VALUE);
            StreamUtils.copy(resource, response.getOutputStream());
        }
    }
}
