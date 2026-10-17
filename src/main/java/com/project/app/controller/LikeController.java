package com.project.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.app.payloads.ApiResponse;
import com.project.app.services.LikeService;

@RestController
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

    // remove like from a post
    @DeleteMapping("/like")
    public ResponseEntity<ApiResponse> unlikePost(@PathVariable("postId") Integer postId) {
        this.likeService.unlikePost(postId);
        return ResponseEntity.ok(new ApiResponse("post unliked", true));
    }

}
