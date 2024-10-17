package com.example.relicv2.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileUploadService {
    public String uploadImage(MultipartFile file) throws IOException;
}
