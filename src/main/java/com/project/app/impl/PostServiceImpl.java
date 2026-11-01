package com.project.app.impl;

import java.io.IOException;
import java.sql.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
import com.project.app.services.FileService;
import com.project.app.services.PostService;
import com.project.app.exceptions.ResouceNotFoundException;
import com.project.app.config.AppConstants;
import com.project.app.entities.PostStatus;

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

    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String imagePath;

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
        post.setViewCount(0);
        post.setStatus(postDto.getStatus() == null ? PostStatus.PUBLISHED : postDto.getStatus());
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
        String imageName = post.getImageName();
        this.postRepo.delete(post);
        this.fileService.deleteImage(this.imagePath, imageName);
    }

    // make a draft visible to everyone, the publish time becomes the post date
    @Override
    public PostDto publishPost(Integer postId) {
        Post post = this.postRepo.findById(postId).orElseThrow(()-> new ResouceNotFoundException("Post", "Id", postId));
        this.checkOwnerOrAdmin(post);
        if (post.getStatus() == PostStatus.PUBLISHED) {
            throw new ApiException("post is already published", HttpStatus.CONFLICT);
        }
        post.setStatus(PostStatus.PUBLISHED);
        post.setAddDate(new Date(System.currentTimeMillis()));
        return this.postMapper.toDto(this.postRepo.save(post));
    }

    // take a post back to draft
    @Override
    public PostDto unpublishPost(Integer postId) {
        Post post = this.postRepo.findById(postId).orElseThrow(()-> new ResouceNotFoundException("Post", "Id", postId));
        this.checkOwnerOrAdmin(post);
        if (post.getStatus() == PostStatus.DRAFT) {
            throw new ApiException("post is already a draft", HttpStatus.CONFLICT);
        }
        post.setStatus(PostStatus.DRAFT);
        return this.postMapper.toDto(this.postRepo.save(post));
    }

    // ownership is checked before the file is written so rejected uploads leave nothing behind
    @Override
    public PostDto uploadPostImage(Integer postId, MultipartFile image) throws IOException {
        Post post = this.postRepo.findById(postId).orElseThrow(()-> new ResouceNotFoundException("Post", "Id", postId));
        this.checkOwnerOrAdmin(post);

        String oldImageName = post.getImageName();
        String newImageName = this.fileService.uploadImage(this.imagePath, image);
        post.setImageName(newImageName);
        Post updatedPost = this.postRepo.save(post);

        this.fileService.deleteImage(this.imagePath, oldImageName);
        return this.postMapper.toDto(updatedPost);
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

        // every read of a single post counts as one view
        this.postRepo.incrementViewCount(postId);
        post.setViewCount(post.getViewCount() + 1);

        return this.postMapper.toDto(post);
    }

    // getall
    @Override
    public PageResponse<PostDto> getAllPosts(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection){
        Page<Post> pagePost = this.postRepo.findAll(PageUtil.build(pageNumber, pageSize, sortBy, sortDirection, AppConstants.POST_SORT_FIELDS));
        return PageResponse.of(pagePost, this.postMapper::toDto);
    }

    // get by user
    @Override
    public PageResponse<PostDto> getPostsByUser(Integer userId, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection){
        User user = this.userRepo.findById(userId).orElseThrow(()-> new ResouceNotFoundException("User", "Id", userId));
        Page<Post> posts = this.postRepo.findByUser(user, PageUtil.build(pageNumber, pageSize, sortBy, sortDirection, AppConstants.POST_SORT_FIELDS));
        return PageResponse.of(posts, this.postMapper::toDto);
    }

    // get by category
    @Override
    public PageResponse<PostDto> getPostsByCategory(Integer categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection){
        Category category = this.categoryRepo.findById(categoryId).orElseThrow(()-> new ResouceNotFoundException("Category", "Id", categoryId));
        Page<Post> posts = this.postRepo.findByCategory(category, PageUtil.build(pageNumber, pageSize, sortBy, sortDirection, AppConstants.POST_SORT_FIELDS));
        return PageResponse.of(posts, this.postMapper::toDto);
    }

    // get by tag, tag names are stored in lower case
    @Override
    public PageResponse<PostDto> getPostsByTag(String tagName, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection){
        String name = tagName.trim().toLowerCase();
        if (this.tagRepo.findByName(name).isEmpty()) {
            throw new ApiException("tag not found : " + name, HttpStatus.NOT_FOUND);
        }
        Page<Post> posts = this.postRepo.findByTagsName(name, PageUtil.build(pageNumber, pageSize, sortBy, sortDirection, AppConstants.POST_SORT_FIELDS));
        return PageResponse.of(posts, this.postMapper::toDto);
    }

    // newest posts from the people the logged in user follows
    @Override
    public PageResponse<PostDto> getFeed(Integer pageNumber, Integer pageSize){
        User me = this.authUtil.getLoggedInUser();
        Page<Post> posts = this.postRepo.findFeed(me, PageUtil.build(pageNumber, pageSize, "postId", "desc"));
        return PageResponse.of(posts, this.postMapper::toDto);
    }

    // most viewed posts, newer posts win a tie
    @Override
    public List<PostDto> getTrendingPosts(Integer limit){
        if (limit == null || limit < 1 || limit > 50) {
            throw new ApiException("limit must be between 1 and 50");
        }
        Sort sort = Sort.by(Sort.Order.desc("viewCount"), Sort.Order.desc("postId"));
        return this.postRepo.findAll(PageRequest.of(0, limit, sort)).stream()
                .map(this.postMapper::toDto)
                .collect(Collectors.toList());
    }

    // search
    @Override
    public PageResponse<PostDto> searchPosts(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection){
        if (keyword == null || keyword.trim().length() < 2) {
            throw new ApiException("search keyword must have at least 2 characters");
        }
        Page<Post> posts = this.postRepo.search(keyword.trim(), PageUtil.build(pageNumber, pageSize, sortBy, sortDirection, AppConstants.POST_SORT_FIELDS));
        return PageResponse.of(posts, this.postMapper::toDto);
    }

}
