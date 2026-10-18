package com.project.app.impl;

import java.sql.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.project.app.entities.Category;
import com.project.app.entities.Post;
import com.project.app.entities.Tag;
import com.project.app.entities.User;
import com.project.app.exceptions.ApiException;
import com.project.app.repositories.CategoryRepo;
import com.project.app.repositories.PostRepo;
import com.project.app.repositories.TagRepo;
import com.project.app.repositories.UserRepo;
import com.project.app.payloads.PostDto;
import com.project.app.payloads.PageResponse;
import com.project.app.security.AuthUtil;
import com.project.app.utils.PageUtil;
import com.project.app.utils.PostMapper;
import com.project.app.services.PostService;
import com.project.app.exceptions.ResouceNotFoundException;

@Service 
public class PostServiceImpl implements PostService {

    @Autowired 
    private PostRepo postRepo;

    @Autowired 
    private UserRepo userRepo;

    @Autowired 
    private CategoryRepo categoryRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private TagRepo tagRepo;

    // create
    @Override
    public PostDto createPost(PostDto postDto, Integer userId, Integer categoryId){
        User user = this.userRepo.findById(userId).orElseThrow(()-> new ResouceNotFoundException("User", "Id", userId));
        Category category = this.categoryRepo.findById(categoryId).orElseThrow(()-> new ResouceNotFoundException("Category", "Id", categoryId));

        // nobody can publish a post in somebody else's name, except an admin
        User loggedInUser = this.authUtil.getLoggedInUser();
        if (loggedInUser.getId() != user.getId() && !this.authUtil.isAdmin()) {
            throw new AccessDeniedException("you can only create posts for your own account");
        }

        Post post = this.modelMapper.map(postDto, Post.class);
        post.setPostId(null);
        post.setImageName(postDto.getImageName() == null ? "default.png" : postDto.getImageName());
        post.setAddDate(new Date(System.currentTimeMillis()));
        post.setUser(user);
        post.setCategory(category);
        post.setTags(this.resolveTags(postDto.getTagNames()));

        Post savedPost = this.postRepo.save(post);
        return this.postMapper.toDto(savedPost);
    }

    // update
    @Override
    public PostDto updatePost(PostDto postDto, Integer postId){
        Post post = this.postRepo.findById(postId).orElseThrow(()-> new ResouceNotFoundException("Post", "Id", postId));
        this.checkOwnerOrAdmin(post);

        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        if (postDto.getImageName() != null) {
            post.setImageName(postDto.getImageName());
        }
        if (postDto.getTagNames() != null) {
            post.setTags(this.resolveTags(postDto.getTagNames()));
        }

        Post updatedPost = this.postRepo.save(post);
        return this.postMapper.toDto(updatedPost);
    }

    // delete
    @Override
    public void deletePost(Integer postId){
        Post post = this.postRepo.findById(postId).orElseThrow(()-> new ResouceNotFoundException("Post", "Id", postId));
        this.checkOwnerOrAdmin(post);
        this.postRepo.delete(post);
    }

    // turns tag names into tag entities, reusing existing tags and creating new ones
    private Set<Tag> resolveTags(Set<String> tagNames) {
        Set<Tag> tags = new HashSet<>();
        if (tagNames == null) {
            return tags;
        }
        for (String rawName : tagNames) {
            if (rawName == null || rawName.isBlank()) {
                continue;
            }
            String name = rawName.trim().toLowerCase();
            if (name.length() > 30) {
                throw new ApiException("tag name can be at most 30 characters : " + name);
            }
            tags.add(this.tagRepo.findByName(name).orElseGet(() -> this.tagRepo.save(new Tag(name))));
        }
        return tags;
    }

    // a post can only be changed by the user who wrote it or by an admin
    private void checkOwnerOrAdmin(Post post) {
        User loggedInUser = this.authUtil.getLoggedInUser();
        boolean isOwner = post.getUser() != null && post.getUser().getId() == loggedInUser.getId();
        if (!isOwner && !this.authUtil.isAdmin()) {
            throw new AccessDeniedException("you can only modify your own posts");
        }
    }

    // get 
    @Override
    public PostDto getPostById(Integer postId){
        Post post = this.postRepo.findById(postId).orElseThrow(()-> new ResouceNotFoundException("Post", "Id", postId));
        return this.postMapper.toDto(post);
    }

    // getall
    @Override
    public PageResponse<PostDto> getAllPosts(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection){
        Page<Post> pagePost = this.postRepo.findAll(PageUtil.build(pageNumber, pageSize, sortBy, sortDirection));
        return PageResponse.of(pagePost, this.postMapper::toDto);
    }

    // get by user
    @Override
    public PageResponse<PostDto> getPostsByUser(Integer userId, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection){
        User user = this.userRepo.findById(userId).orElseThrow(()-> new ResouceNotFoundException("User", "Id", userId));
        Page<Post> posts = this.postRepo.findByUser(user, PageUtil.build(pageNumber, pageSize, sortBy, sortDirection));
        return PageResponse.of(posts, this.postMapper::toDto);
    }

    // get by category
    @Override
    public PageResponse<PostDto> getPostsByCategory(Integer categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection){
        Category category = this.categoryRepo.findById(categoryId).orElseThrow(()-> new ResouceNotFoundException("Category", "Id", categoryId));
        Page<Post> posts = this.postRepo.findByCategory(category, PageUtil.build(pageNumber, pageSize, sortBy, sortDirection));
        return PageResponse.of(posts, this.postMapper::toDto);
    }

    // search
    @Override
    public List<PostDto> searchPosts(String keyword){
        List<Post> posts = this.postRepo.searchByTitle("%" + keyword + "%");
        List<PostDto> postDtos= posts.stream().map(this.postMapper::toDto).collect(Collectors.toList());
        return postDtos;
    }

}
