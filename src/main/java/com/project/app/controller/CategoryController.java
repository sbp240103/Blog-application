package com.project.app.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.app.config.AppConstants;
import com.project.app.payloads.CategoryDto;
import com.project.app.payloads.PageResponse;

import jakarta.validation.Valid;
import com.project.app.services.CategoryService;
import com.project.app.payloads.ApiResponse;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // create category
    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto categoryDto) {
        CategoryDto createdCategory = this.categoryService.createCategory(categoryDto);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    // update category

    @PutMapping ("/{categoryId}")
    public ResponseEntity<CategoryDto> updateCategory(@Valid @RequestBody CategoryDto categoryDto, @PathVariable ("categoryId") Integer cId){
        CategoryDto updatedCategory = this.categoryService.updateCategory(categoryDto, cId);
        return ResponseEntity.ok(updatedCategory);
    }

    // delete category

    @DeleteMapping ("/{categoryId}")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable("categoryId") Integer cId){
        this.categoryService.deleteCategory(cId);
        return new ResponseEntity<>(new ApiResponse("category deleted successfully", true), HttpStatus.OK);
    }

    // get category
    @GetMapping
    public ResponseEntity<PageResponse<CategoryDto>> getAllCategory(
        @RequestParam (value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
        @RequestParam (value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
        @RequestParam (value = "sortBy", defaultValue = "categoryId", required = false) String sortBy,
        @RequestParam (value = "sortDirection", defaultValue = AppConstants.SORT_DIRECTION, required = false) String sortDirection
    ){
        return ResponseEntity.ok(this.categoryService.getAllCategory(pageNumber, pageSize, sortBy, sortDirection));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable("categoryId") Integer cId){
        return ResponseEntity.ok(this.categoryService.getCategory(cId));
    }
}
