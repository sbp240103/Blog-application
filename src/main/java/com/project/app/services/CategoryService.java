package com.project.app.services;

import com.project.app.payloads.CategoryDto;
import com.project.app.payloads.PageResponse;

public interface CategoryService {
    CategoryDto createCategory(CategoryDto categoryDto);
    CategoryDto updateCategory(CategoryDto categoryDto, Integer Id);
    CategoryDto getCategory(Integer Id);
    PageResponse<CategoryDto> getAllCategory(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection);
    void deleteCategory(Integer Id);

}
