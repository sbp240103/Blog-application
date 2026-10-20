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
import com.project.app.payloads.PostDto;
import com.project.app.services.BookmarkService;

@RestController
@RequestMapping("/api")
public class BookmarkController {

    @Autowired
    private BookmarkService bookmarkService;

    // save a post for later
    @PostMapping("/posts/{postId}/bookmark")
    public ResponseEntity<ApiResponse> bookmarkPost(@PathVariable("postId") Integer postId) {
        this.bookmarkService.bookmarkPost(postId);
        return ResponseEntity.ok(new ApiResponse("post bookmarked", true));
    }

    // posts saved by the logged in user
    @GetMapping("/bookmarks")
    public ResponseEntity<PageResponse<PostDto>> getMyBookmarks(
        @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
        @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
    ) {
        return ResponseEntity.ok(this.bookmarkService.getMyBookmarks(pageNumber, pageSize));
    }

    // remove a saved post
    @DeleteMapping("/posts/{postId}/bookmark")
    public ResponseEntity<ApiResponse> removeBookmark(@PathVariable("postId") Integer postId) {
        this.bookmarkService.removeBookmark(postId);
        return ResponseEntity.ok(new ApiResponse("bookmark removed", true));
    }

}
