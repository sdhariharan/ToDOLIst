package com.tasknest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paginated envelope containing tasks and page metadata")
public class TaskPageResponseDto {

    @Schema(description = "List of tasks on the current page")
    private List<TaskResponseDto> content;

    @Schema(description = "Zero-based current page index", example = "0")
    private int page;

    @Schema(description = "Number of records requested per page", example = "10")
    private int size;

    @Schema(description = "Total number of records matching the filter criteria", example = "42")
    private long totalElements;

    @Schema(description = "Total number of available pages", example = "5")
    private int totalPages;

    @Schema(description = "Flag indicating whether this is the first page", example = "true")
    private boolean first;

    @Schema(description = "Flag indicating whether this is the last page", example = "false")
    private boolean last;

    public TaskPageResponseDto() {
    }

    public TaskPageResponseDto(List<TaskResponseDto> content, int page, int size,
                               long totalElements, int totalPages, boolean first, boolean last) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
    }

    public List<TaskResponseDto> getContent() {
        return content;
    }

    public void setContent(List<TaskResponseDto> content) {
        this.content = content;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }
}
