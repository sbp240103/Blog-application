package com.project.app.services;

import java.util.List;
import com.project.app.payloads.CategoryDto;

public interface CategoryService {
    CategoryDto createCategory(CategoryDto categoryDto);
    CategoryDto updateCategory(CategoryDto categoryDto, Integer Id);
    CategoryDto getCategory(Integer Id);
    List<CategoryDto>getAllCategory();
    void deleteCategory(Integer Id);

}
