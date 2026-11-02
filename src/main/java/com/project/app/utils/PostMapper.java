package com.project.app.utils;

import java.util.TreeSet;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.project.app.entities.Post;
import com.project.app.entities.Tag;
import com.project.app.payloads.PostDto;
import com.project.app.repositories.PostLikeRepo;
import com.project.app.repositories.CommentRepo;
import com.project.app.entities.User;
import com.project.app.repositories.BookmarkRepo;
import com.project.app.security.AuthUtil;

// single place that turns a post entity into the dto sent to clients,
// so computed fields like like count are filled the same way everywhere
@Component
public class PostMapper {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PostLikeRepo postLikeRepo;

    @Autowired
    private CommentRepo commentRepo;

    @Autowired
    private BookmarkRepo bookmarkRepo;

    @Autowired
    private AuthUtil authUtil;

    private static final int WORDS_PER_MINUTE = 200;
    private static final int EXCERPT_LENGTH = 160;

    static int readingTimeMinutes(String content) {
        if (content == null || content.isBlank()) {
            return 1;
        }
        int words = content.trim().split("\\s+").length;
        return Math.max(1, (int) Math.ceil(words / (double) WORDS_PER_MINUTE));
    }

    // cuts at a word boundary so the preview never ends mid word
    static String excerpt(String content) {
        if (content == null) {
            return "";
        }
        String text = content.trim().replaceAll("\\s+", " ");
        if (text.length() <= EXCERPT_LENGTH) {
            return text;
        }
        int cut = text.lastIndexOf(' ', EXCERPT_LENGTH);
        return text.substring(0, cut > 0 ? cut : EXCERPT_LENGTH) + "...";
    }

    public PostDto toDto(Post post) {
        PostDto postDto = this.modelMapper.map(post, PostDto.class);
        postDto.setLikeCount(this.postLikeRepo.countByPost(post));
        postDto.setCommentCount(this.commentRepo.countByPost(post));
        postDto.setReadingTimeMinutes(readingTimeMinutes(post.getContent()));
        postDto.setExcerpt(excerpt(post.getContent()));

        User viewer = this.authUtil.getLoggedInUserOrNull();
        if (viewer != null) {
            postDto.setLikedByMe(this.postLikeRepo.existsByUserAndPost(viewer, post));
            postDto.setBookmarkedByMe(this.bookmarkRepo.existsByUserAndPost(viewer, post));
        }
        postDto.setTagNames(post.getTags().stream().map(Tag::getName).collect(Collectors.toCollection(TreeSet::new)));
        return postDto;
    }
}
