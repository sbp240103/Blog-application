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
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import com.project.app.utils.SlugUtil;
import java.time.LocalDate;
import com.project.app.utils.ContentFilter;
import org.springframework.cache.annotation.CacheEvict;

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

    @Autowired
    private ContentFilter contentFilter;

    @Value("${project.image}")
    private String imagePath;

    // create
    @Override
    @CacheEvict(value = {"tags", "popularTags"}, allEntries = true)
    public PostDto createPost(PostDto postDto, Integer userId, Integer categoryId){
        User user = this.userRepo.findById(userId).orElseThrow(()-> new ResouceNotFoundException("User", "Id", userId));
        Category category = this.categoryRepo.findById(categoryId).orElseThrow(()-> new ResouceNotFoundException("Category", "Id", categoryId));

        this.contentFilter.check(postDto.getTitle(), postDto.getContent());

        // nobody can publish a post in somebody else's name, except an admin
        User loggedInUser = this.authUtil.getLoggedInUser();
        if (loggedInUser.getId() != user.getId() && !this.authUtil.isAdmin()) {
            throw new AccessDeniedException("you can only create posts for your own account");
        }

        Post post = this.modelMapper.map(postDto, Post.class);
        post.setPostId(null);
        post.setViewCount(0);
        post.setFeatured(false);
        post.setSlug(this.uniqueSlug(postDto.getTitle()));
        post.setStatus(postDto.getStatus() == null ? PostStatus.PUBLISHED : postDto.getStatus());

        // a scheduled post stays a draft until the scheduler publishes it
        if (postDto.getPublishAt() != null) {
            if (!postDto.getPublishAt().isAfter(LocalDateTime.now())) {
                throw new ApiException("publishAt must be in the future");
            }
            post.setStatus(PostStatus.DRAFT);
        }
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
    @CacheEvict(value = {"tags", "popularTags"}, allEntries = true)
    public PostDto updatePost(PostDto postDto, Integer postId){
        Post post = this.postRepo.findById(postId).orElseThrow(()-> new ResouceNotFoundException("Post", "Id", postId));
        this.checkOwnerOrAdmin(post);
        this.contentFilter.check(postDto.getTitle(), postDto.getContent());

        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        if (post.getSlug() == null) {
            post.setSlug(this.uniqueSlug(postDto.getTitle()));
        }
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
    @CacheEvict(value = {"tags", "popularTags"}, allEntries = true)
    public void deletePost(Integer postId){
        Post post = this.postRepo.findById(postId).orElseThrow(()-> new ResouceNotFoundException("Post", "Id", postId));
        this.checkOwnerOrAdmin(post);
        String imageName = post.getImageName();
        this.postRepo.delete(post);
        this.fileService.deleteImage(this.imagePath, imageName);
    }

    // make a draft visible to everyone, the publish time becomes the post date
    @Override
    @CacheEvict(value = {"tags", "popularTags"}, allEntries = true)
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
    @CacheEvict(value = {"tags", "popularTags"}, allEntries = true)
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

    // adds -2, -3 ... until the slug is not taken by another post
    private String uniqueSlug(String title) {
        String base = SlugUtil.toSlug(title);
        String slug = base;
        int counter = 2;
        while (this.postRepo.existsBySlug(slug)) {
            slug = base + "-" + counter++;
        }
        return slug;
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

        // a draft looks like it does not exist to anyone but its author and admins
        if (post.getStatus() == PostStatus.DRAFT) {
            User viewer = this.authUtil.getLoggedInUserOrNull();
            boolean isOwner = viewer != null && post.getUser() != null && post.getUser().getId() == viewer.getId();
            if (!isOwner && !this.authUtil.isAdmin()) {
                throw new ResouceNotFoundException("Post", "Id", postId);
            }
            return this.postMapper.toDto(post);
        }

        // every read of a single published post counts as one view
        this.postRepo.incrementViewCount(postId);
        post.setViewCount(post.getViewCount() + 1);

        return this.postMapper.toDto(post);
    }

    // same rules as reading by id, drafts stay hidden
    @Override
    public PostDto getPostBySlug(String slug){
        Post post = this.postRepo.findBySlug(slug).orElseThrow(()-> new ApiException("post not found : " + slug, HttpStatus.NOT_FOUND));
        return this.getPostById(post.getPostId());
    }

    // getall
    @Override
    public PageResponse<PostDto> getAllPosts(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection){
        Page<Post> pagePost = this.postRepo.findByStatus(PostStatus.PUBLISHED, PageUtil.build(pageNumber, pageSize, sortBy, sortDirection, AppConstants.POST_SORT_FIELDS));
        return PageResponse.of(pagePost, this.postMapper::toDto);
    }

    // get by user
    @Override
    public PageResponse<PostDto> getPostsByUser(Integer userId, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection){
        User user = this.userRepo.findById(userId).orElseThrow(()-> new ResouceNotFoundException("User", "Id", userId));
        Pageable pageable = PageUtil.build(pageNumber, pageSize, sortBy, sortDirection, AppConstants.POST_SORT_FIELDS);

        // authors and admins also see drafts, everyone else only published posts
        User viewer = this.authUtil.getLoggedInUserOrNull();
        boolean canSeeDrafts = viewer != null && (viewer.getId() == user.getId() || this.authUtil.isAdmin());
        Page<Post> posts = canSeeDrafts
                ? this.postRepo.findByUser(user, pageable)
                : this.postRepo.findByUserAndStatus(user, PostStatus.PUBLISHED, pageable);
        return PageResponse.of(posts, this.postMapper::toDto);
    }

    // get by category
    @Override
    public PageResponse<PostDto> getPostsByCategory(Integer categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection){
        Category category = this.categoryRepo.findById(categoryId).orElseThrow(()-> new ResouceNotFoundException("Category", "Id", categoryId));
        Page<Post> posts = this.postRepo.findByCategoryAndStatus(category, PostStatus.PUBLISHED, PageUtil.build(pageNumber, pageSize, sortBy, sortDirection, AppConstants.POST_SORT_FIELDS));
        return PageResponse.of(posts, this.postMapper::toDto);
    }

    // get by tag, tag names are stored in lower case
    @Override
    public PageResponse<PostDto> getPostsByTag(String tagName, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection){
        String name = tagName.trim().toLowerCase();
        if (this.tagRepo.findByName(name).isEmpty()) {
            throw new ApiException("tag not found : " + name, HttpStatus.NOT_FOUND);
        }
        Page<Post> posts = this.postRepo.findByTagsNameAndStatus(name, PostStatus.PUBLISHED, PageUtil.build(pageNumber, pageSize, sortBy, sortDirection, AppConstants.POST_SORT_FIELDS));
        return PageResponse.of(posts, this.postMapper::toDto);
    }

    // newest posts from the people the logged in user follows
    @Override
    public PageResponse<PostDto> getFeed(Integer pageNumber, Integer pageSize){
        User me = this.authUtil.getLoggedInUser();
        Page<Post> posts = this.postRepo.findFeed(me, PageUtil.build(pageNumber, pageSize, "postId", "desc"));
        return PageResponse.of(posts, this.postMapper::toDto);
    }

    // unpublished posts of the logged in user, most recently changed first
    @Override
    public PageResponse<PostDto> getMyDrafts(Integer pageNumber, Integer pageSize){
        User me = this.authUtil.getLoggedInUser();
        Page<Post> drafts = this.postRepo.findByUserAndStatus(me, PostStatus.DRAFT, PageUtil.build(pageNumber, pageSize, "postId", "desc"));
        return PageResponse.of(drafts, this.postMapper::toDto);
    }

    // posts picked by admins, newest first
    @Override
    public PageResponse<PostDto> getFeaturedPosts(Integer pageNumber, Integer pageSize){
        Page<Post> posts = this.postRepo.findByFeaturedTrueAndStatus(PostStatus.PUBLISHED, PageUtil.build(pageNumber, pageSize, "postId", "desc"));
        return PageResponse.of(posts, this.postMapper::toDto);
    }

    // published posts of a year, or of one month when month is given
    @Override
    public PageResponse<PostDto> getArchive(Integer year, Integer month, Integer pageNumber, Integer pageSize){
        if (year == null || year < 2000 || year > 2100) {
            throw new ApiException("year must be between 2000 and 2100");
        }
        if (month != null && (month < 1 || month > 12)) {
            throw new ApiException("month must be between 1 and 12");
        }

        LocalDate from = month == null ? LocalDate.of(year, 1, 1) : LocalDate.of(year, month, 1);
        LocalDate to = month == null ? from.withDayOfYear(from.lengthOfYear()) : from.withDayOfMonth(from.lengthOfMonth());

        Page<Post> posts = this.postRepo.findByStatusAndAddDateBetween(PostStatus.PUBLISHED, Date.valueOf(from), Date.valueOf(to),
                PageUtil.build(pageNumber, pageSize, "addDate", "desc"));
        return PageResponse.of(posts, this.postMapper::toDto);
    }

    // more to read after finishing a post
    @Override
    public List<PostDto> getRelatedPosts(Integer postId, Integer limit){
        if (limit == null || limit < 1 || limit > 20) {
            throw new ApiException("limit must be between 1 and 20");
        }
        Post post = this.postRepo.findById(postId).orElseThrow(()-> new ResouceNotFoundException("Post", "Id", postId));
        Pageable top = PageRequest.of(0, limit);
        List<Post> related = post.getTags().isEmpty()
                ? this.postRepo.findRelatedByCategory(postId, post.getCategory(), top)
                : this.postRepo.findRelated(postId, post.getCategory(), post.getTags(), top);
        return related.stream().map(this.postMapper::toDto).collect(Collectors.toList());
    }

    // most viewed posts, newer posts win a tie
    @Override
    public List<PostDto> getTrendingPosts(Integer limit){
        if (limit == null || limit < 1 || limit > 50) {
            throw new ApiException("limit must be between 1 and 50");
        }
        Sort sort = Sort.by(Sort.Order.desc("viewCount"), Sort.Order.desc("postId"));
        return this.postRepo.findByStatus(PostStatus.PUBLISHED, PageRequest.of(0, limit, sort)).stream()
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
