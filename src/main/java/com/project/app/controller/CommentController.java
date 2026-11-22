package com.project.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.app.payloads.CommentDto;

import jakarta.validation.Valid;
import com.project.app.services.CommentService;
import com.project.app.payloads.ApiResponse;
import org.springframework.web.bind.annotation.RequestParam;
import com.project.app.config.AppConstants;
import com.project.app.payloads.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Comments", description = "comments and replies on posts")
@RequestMapping("/api")
public class CommentController {

    @Autowired
    private CommentService commentService;

    // create comment
    @PostMapping ("/post/{postId}/comments")
    public ResponseEntity<CommentDto> createComment(@Valid @RequestBody CommentDto commentDto, @PathVariable("postId") Integer postId) {
        CommentDto createdComment = this.commentService.createComment(commentDto, postId);
        return new ResponseEntity<>(createdComment, HttpStatus.CREATED);
    }

    // top level comments of a post with reply counts
    @GetMapping ("/post/{postId}/comments")
    public ResponseEntity<PageResponse<CommentDto>> getCommentsByPost(
        @PathVariable("postId") Integer postId,
        @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
        @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
    ) {
        return ResponseEntity.ok(this.commentService.getCommentsByPost(postId, pageNumber, pageSize));
    }

    // reply to a comment
    @PostMapping ("/comments/{commentId}/replies")
    public ResponseEntity<CommentDto> createReply(@Valid @RequestBody CommentDto commentDto, @PathVariable("commentId") Integer commentId) {
        return new ResponseEntity<>(this.commentService.createReply(commentDto, commentId), HttpStatus.CREATED);
    }

    // replies of a comment
    @GetMapping ("/comments/{commentId}/replies")
    public ResponseEntity<List<CommentDto>> getReplies(@PathVariable("commentId") Integer commentId) {
        return ResponseEntity.ok(this.commentService.getReplies(commentId));
    }

    // update comment
    @PutMapping ("/comments/{commentId}")
    public ResponseEntity<CommentDto> updateComment(@Valid @RequestBody CommentDto commentDto, @PathVariable("commentId") Integer commentId) {
        return ResponseEntity.ok(this.commentService.updateComment(commentDto, commentId));
    }

    // delete comment

    @DeleteMapping ("/comments/{commentId}")
    public ResponseEntity<ApiResponse> deleteComment(@PathVariable("commentId") Integer commentId){
        this.commentService.deleteComment(commentId);
        return new ResponseEntity<>(new ApiResponse("comment deleted successfully", true), HttpStatus.OK);
    }
}
