package com.project.app.payloads;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// one paginated response shape shared by every listing endpoint
@NoArgsConstructor
@Getter
@Setter
public class PageResponse<T> {

    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean lastPage;

    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        PageResponse<T> response = new PageResponse<>();
        response.setContent(page.getContent().stream().map(mapper).collect(Collectors.toList()));
        response.setPageNumber(page.getNumber());
        response.setPageSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLastPage(page.isLast());
        return response;
    }
}
