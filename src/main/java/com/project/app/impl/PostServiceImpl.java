package com.project.app.impl;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.project.app.entities.Category;
import com.project.app.entities.Post;
import com.project.app.entities.User;
import com.project.app.repositories.CategoryRepo;
import com.project.app.repositories.PostRepo;
import com.project.app.repositories.UserRepo;
import com.project.app.payloads.PostDto;
import com.project.app.payloads.PostResponse;
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

    // create
    @Override
    public PostDto createPost(PostDto postDto, Integer userId, Integer categoryId){
        User user = this.userRepo.findById(userId).orElseThrow(()-> new ResouceNotFoundException("User", "Id", userId));
        Category category = this.categoryRepo.findById(categoryId).orElseThrow(()-> new ResouceNotFoundException("Category", "Id", categoryId));

        Post post = this.modelMapper.map(postDto, Post.class);
        post.setPostId(null);
        post.setImageName(postDto.getImageName() == null ? "default.png" : postDto.getImageName());
        post.setAddDate(new Date(System.currentTimeMillis()));
        post.setUser(user);
        post.setCategory(category);

        Post savedPost = this.postRepo.save(post);
        return this.modelMapper.map(savedPost, PostDto.class);
    }

    // update
    @Override
    public PostDto updatePost(PostDto postDto, Integer postId){
        Post post = this.postRepo.findById(postId).orElseThrow(()-> new ResouceNotFoundException("Post", "Id", postId));
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        if (postDto.getImageName() != null) {
            post.setImageName(postDto.getImageName());
        }

        Post updatedPost = this.postRepo.save(post);
        return this.modelMapper.map(updatedPost, PostDto.class);
    }

    // delete
    @Override
    public void deletePost(Integer postId){
        Post post = this.postRepo.findById(postId).orElseThrow(()-> new ResouceNotFoundException("Post", "Id", postId));
        this.postRepo.delete(post);
    }

    // get 
    @Override
    public PostDto getPostById(Integer postId){
        Post post = this.postRepo.findById(postId).orElseThrow(()-> new ResouceNotFoundException("Post", "Id", postId));
        return this.modelMapper.map(post, PostDto.class);
    }

    // getall
    @Override
    public PostResponse getAllPosts(Integer pageSize, Integer pageNumber, String sortBy, String sortDirection){
        Sort sort = null;
        if(sortDirection.equalsIgnoreCase("asc")){
            sort=Sort.by(sortBy).ascending();
        }
        else{
            sort=Sort.by(sortBy).descending();
        }
        Pageable p = PageRequest.of(pageNumber,pageSize, sort);
        Page<Post> pagePost= this.postRepo.findAll(p);
        List<Post> allPosts = pagePost.getContent();
        List<PostDto> postDtos = allPosts.stream().map(post->this.modelMapper.map(post, PostDto.class)).collect(Collectors.toList());

        PostResponse postResponse = new PostResponse();
        postResponse.setContent(postDtos);
        postResponse.setPageNumber(pagePost.getNumber());
        postResponse.setPageSize(pagePost.getSize());
        postResponse.setTotalElements(pagePost.getTotalElements());
        postResponse.setTotalPages(pagePost.getTotalPages());
        postResponse.setLastPage(pagePost.isLast());
        return postResponse;
    }

    // get by user
    @Override
    public List<PostDto> getPostsByUser(Integer userId){
        User user = this.userRepo.findById(userId).orElseThrow(()-> new ResouceNotFoundException("User", "Id", userId));
        List<Post> posts = this.postRepo.findByUser(user);
        return posts.stream().map(post->this.modelMapper.map(post, PostDto.class)).collect(Collectors.toList());
    }

    // get by category
    @Override
    public List<PostDto> getPostsByCategory(Integer categoryId){
        Category category = this.categoryRepo.findById(categoryId).orElseThrow(()-> new ResouceNotFoundException("Category", "Id", categoryId));
        List<Post> posts = this.postRepo.findByCategory(category);
        return posts.stream().map(post->this.modelMapper.map(post, PostDto.class)).collect(Collectors.toList());
    }

    // search
    @Override
    public List<PostDto> searchPosts(String keyword){
        List<Post> posts = this.postRepo.searchByTitle("%" + keyword + "%");
        List<PostDto> postDtos= posts.stream().map((post)->this.modelMapper.map(post, PostDto.class)).collect(Collectors.toList());
        return postDtos;
    }

    // post image upload

    public ResponseE

}
