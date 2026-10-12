package com.example.demo.dto;

import com.example.demo.model.Category;

/**
 * Data Transfer Object representing a category for REST APIs.
 */
public class CategoryDto {

    private Long id;
    private String name;
    private String icon;

    public CategoryDto() {}

    public CategoryDto(Long id, String name, String icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    public static CategoryDto fromEntity(Category category) {
        if (category == null) return null;
        return new CategoryDto(category.getId(), category.getName(), category.getIcon());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}
