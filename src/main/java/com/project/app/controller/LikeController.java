package com.project.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.app.config.AppConstants;
import com.project.app.payloads.ApiResponse;
import com.project.app.payloads.PageResponse;
import com.project.app.payloads.UserDto;
import com.project.app.services.LikeService;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Likes", description = "liking posts")
@RequestMapping("/api/posts/{postId}")
public class LikeController {

    @Autowired
    private LikeService likeService;

    // like a post
    @PostMapping("/like")
    public ResponseEntity<ApiResponse> likePost(@PathVariable("postId") Integer postId) {
        this.likeService.likePost(postId);
        return ResponseEntity.ok(new ApiResponse("post liked", true));
    }

    // users who liked a post
    @GetMapping("/likes")
    public ResponseEntity<PageResponse<UserDto>> getUsersWhoLiked(
        @PathVariable("postId") Integer postId,
        @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
        @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
    ) {
        return ResponseEntity.ok(this.likeService.getUsersWhoLikedPost(postId, pageNumber, pageSize));
    }

    // remove like from a post
    @DeleteMapping("/like")
    public ResponseEntity<ApiResponse> unlikePost(@PathVariable("postId") Integer postId) {
        this.likeService.unlikePost(postId);
        return ResponseEntity.ok(new ApiResponse("post unliked", true));
    }

}
