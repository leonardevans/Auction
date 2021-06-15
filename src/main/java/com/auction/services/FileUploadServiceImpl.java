package com.auction.services;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileUploadServiceImpl implements FileUploadService{
    @Override
    public String uploadToLocal(MultipartFile file, String uploadDir) throws IOException {
        try {
            byte[] data = file.getBytes();
            String filename = uploadDir+ System.currentTimeMillis() + "." + FilenameUtils.getExtension(file.getOriginalFilename());
            Path path = Paths.get( filename);
            Files.write(path, data);
            return filename;
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    public boolean deleteLocalFile(String filePath) throws IOException {
        try {
            FileUtils.forceDelete(FileUtils.getFile(filePath));
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
