package com.example.demo.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Maps to the 'categories' table.
 * Job classification domain (e.g., IT, Marketing, Finance).
 */
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 50)
    private String icon = "bi-briefcase";

    // --- Relationships ---

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<JobPost> jobPosts = new ArrayList<>();

    // --- Constructors ---
    public Category() {}

    public Category(String name) {
        this.name = name;
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public List<JobPost> getJobPosts() { return jobPosts; }
    public void setJobPosts(List<JobPost> jobPosts) { this.jobPosts = jobPosts; }
}
