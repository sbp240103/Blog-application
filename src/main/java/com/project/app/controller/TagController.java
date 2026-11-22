package com.project.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.app.config.AppConstants;
import com.project.app.payloads.PageResponse;
import com.project.app.payloads.PostDto;
import com.project.app.payloads.TagDto;
import com.project.app.services.PostService;
import com.project.app.services.TagService;

@RestController
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags", description = "post tags")
@RequestMapping("/api/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    @Autowired
    private PostService postService;

    // all tags
    @GetMapping
    public ResponseEntity<List<TagDto>> getAllTags() {
        return ResponseEntity.ok(this.tagService.getAllTags());
    }

    // most used tags with their post counts
    @GetMapping("/popular")
    public ResponseEntity<List<TagDto>> getPopularTags(
        @RequestParam(value = "limit", defaultValue = "10", required = false) Integer limit
    ) {
        return ResponseEntity.ok(this.tagService.getPopularTags(limit));
    }

    // posts having a tag
    @GetMapping("/{tagName}/posts")
    public ResponseEntity<PageResponse<PostDto>> getPostsByTag(
        @PathVariable("tagName") String tagName,
        @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
        @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
        @RequestParam(value = "sortBy", defaultValue = "postId", required = false) String sortBy,
        @RequestParam(value = "sortDirection", defaultValue = AppConstants.SORT_DIRECTION, required = false) String sortDirection
    ) {
        return ResponseEntity.ok(this.postService.getPostsByTag(tagName, pageNumber, pageSize, sortBy, sortDirection));
    }

}
