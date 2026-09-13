package com.project.app.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.app.entities.Category;
import com.project.app.repositories.CategoryRepo;
import com.project.app.payloads.CategoryDto;
import com.project.app.services.CategoryService;
import com.project.app.exceptions.ResouceNotFoundException;

@Service 
public class CategoryServiceImpl implements CategoryService {

    @Autowired 
    private CategoryRepo categoryRepo;

    @Autowired 
    private ModelMapper modelMapper;

    // @Autowired
    // private CategoryDto categoryDto;

    // create
    @Override
    public CategoryDto createCategory(CategoryDto categoryDto){
        Category category = this.modelMapper.map(categoryDto, Category.class);
        Category savedCategory = this.categoryRepo.save(category);
        return this.modelMapper.map(savedCategory, CategoryDto.class);
    }

    // update
    @Override
    public  CategoryDto updateCategory(CategoryDto categoryDto, Integer Id){
        Category category = this.categoryRepo.findById(Id).orElseThrow(()-> new ResouceNotFoundException("Category", "Id", Id));
        category.setCategoryTitle(categoryDto.getCategoryTitle());
        category.setCategoryDescription(categoryDto.getCategoryDescription());
        Category updatedCategory =this.categoryRepo.save(category);
        return this.modelMapper.map(updatedCategory, CategoryDto.class);
    }

    // delete
    @Override
    public void  deleteCategory(Integer Id){
        Category category = this.categoryRepo.findById(Id).orElseThrow(()-> new ResouceNotFoundException("Category", "Id", Id));
        this.categoryRepo.delete(category);
    }

    // get 
    @Override
    public CategoryDto getCategory(Integer Id){
        Category category = this.categoryRepo.findById(Id).orElseThrow(()-> new ResouceNotFoundException("Category", "Id", Id));
        return this.modelMapper.map(category, CategoryDto.class);
    }

    // getall
    @Override
    public List<CategoryDto> getAllCategory(){
        List<Category> allCategories = this.categoryRepo.findAll();
        List<CategoryDto> allCategoryDtos = allCategories.stream().map(category->this.modelMapper.map(category, CategoryDto.class)).collect(Collectors.toList());
        return allCategoryDtos;
    }

    
}
