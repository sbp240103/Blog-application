package com.project.app.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.project.app.entities.Bookmark;
import com.project.app.entities.Post;
import com.project.app.entities.User;
import com.project.app.exceptions.ApiException;
import com.project.app.exceptions.ResouceNotFoundException;
import com.project.app.repositories.BookmarkRepo;
import com.project.app.repositories.PostRepo;
import com.project.app.security.AuthUtil;
import com.project.app.services.BookmarkService;

@Service
public class BookmarkServiceImpl implements BookmarkService {

    @Autowired
    private PostRepo postRepo;

    @Autowired
    private BookmarkRepo bookmarkRepo;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public void bookmarkPost(Integer postId) {
        Post post = this.postRepo.findById(postId).orElseThrow(() -> new ResouceNotFoundException("Post", "Id", postId));
        User user = this.authUtil.getLoggedInUser();

        if (this.bookmarkRepo.existsByUserAndPost(user, post)) {
            throw new ApiException("post is already bookmarked", HttpStatus.CONFLICT);
        }
        this.bookmarkRepo.save(new Bookmark(user, post));
    }

    @Override
    public void removeBookmark(Integer postId) {
        Post post = this.postRepo.findById(postId).orElseThrow(() -> new ResouceNotFoundException("Post", "Id", postId));
        User user = this.authUtil.getLoggedInUser();

        Bookmark bookmark = this.bookmarkRepo.findByUserAndPost(user, post)
                .orElseThrow(() -> new ApiException("post is not bookmarked"));
        this.bookmarkRepo.delete(bookmark);
    }

}
