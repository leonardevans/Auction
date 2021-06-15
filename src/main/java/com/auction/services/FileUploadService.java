package com.auction.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileUploadService {
    public String uploadToLocal(MultipartFile file, String uploadDir) throws IOException;

    public boolean deleteLocalFile(String filePath) throws IOException;
}
