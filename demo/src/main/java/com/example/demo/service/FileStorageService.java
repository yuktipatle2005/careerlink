package com.example.demo.service;

import com.example.demo.exception.FileStorageException;
import com.example.demo.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service managing secure file uploads, validation, storage, and retrieval.
 */
@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "doc", "docx");

    public FileStorageService(@Value("${file.upload-dir:uploads/resumes}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new FileStorageException("Could not create the upload directory: " + this.fileStorageLocation, ex);
        }
    }

    /**
     * Validates and stores a resume file to disk with a UUID prefix.
     *
     * @param file the uploaded MultipartFile
     * @return the unique generated filename saved on disk
     */
    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Cannot upload an empty file.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileStorageException("File size exceeds 5MB limit. Please upload a smaller file.");
        }

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "resume.pdf"
        );

        // Security Check 1: Defend against Path Traversal (e.g. "../../../etc/passwd")
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new FileStorageException("Filename contains invalid path characters: " + originalFilename);
        }

        // Security Check 2: Validate file extension
        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new FileStorageException("Invalid file format. Only PDF, DOC, and DOCX files are permitted.");
        }

        try {
            // Generate unique, collision-proof filename
            String uniqueFileName = UUID.randomUUID() + "_" + originalFilename.replaceAll("\\s+", "_");
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);

            // Copy file stream to target location, replacing if exists
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return uniqueFileName;
        } catch (IOException ex) {
            throw new FileStorageException("Failed to store file " + originalFilename + ". Please try again.", ex);
        }
    }

    /**
     * Loads a file from disk as a Spring Resource for viewing or downloading.
     *
     * @param fileName the filename stored on disk
     * @return Resource representing the file
     */
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();

            // Defend against directory traversal beyond the target directory
            if (!filePath.startsWith(this.fileStorageLocation)) {
                throw new FileStorageException("Access denied: File is outside the permitted directory.");
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Resume file not found: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("Resume file path is invalid: " + fileName, ex);
        }
    }

    /**
     * Deletes an existing file if candidate replaces it.
     */
    public boolean deleteFile(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            if (filePath.startsWith(this.fileStorageLocation)) {
                return Files.deleteIfExists(filePath);
            }
            return false;
        } catch (IOException ex) {
            return false;
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
    }

    public Path getFileStorageLocation() {
        return fileStorageLocation;
    }
}
