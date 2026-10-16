package com.project.app.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.project.app.exceptions.ApiException;

public class PageUtil {

    public static final int MAX_PAGE_SIZE = 100;

    private PageUtil() {
    }

    // builds a pageable from the usual query params and guards against silly values
    public static Pageable build(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection) {
        if (pageNumber == null || pageNumber < 0) {
            throw new ApiException("pageNumber must be 0 or greater");
        }
        if (pageSize == null || pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new ApiException("pageSize must be between 1 and " + MAX_PAGE_SIZE);
        }

        Sort sort = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        return PageRequest.of(pageNumber, pageSize, sort);
    }
}
