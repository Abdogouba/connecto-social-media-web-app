package com.socialmedia.connecto.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagedDTO<T> {
    private List<T> list;
    private int currentPage;
    private int totalPages;
    private long totalItems;
}
