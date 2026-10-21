package com.project.app.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.project.app.exceptions.ApiException;
import com.project.app.services.FileService;

@Service
public class FileServiceImpl implements FileService{

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");

	@Override
	public String uploadImage(String path, MultipartFile file) throws IOException {
        this.validateImage(file);

		//file name
        String name = file.getOriginalFilename();

        //random name generation
        String randomId =  UUID.randomUUID().toString();

        String fileName1 = randomId.concat(name.substring(name.lastIndexOf(".")).toLowerCase());

        //full path
        String filePath = path + File.separator + fileName1;

        // create folder if not created

        File f = new File(path);
        if(!f.exists()){
            f.mkdirs();
        }

        // file copy

        Files.copy(file.getInputStream(), Paths.get(filePath));

        return fileName1;
    }

	@Override
	public InputStream getResource(String path, String fileName) throws FileNotFoundException {
        // only plain file names, never paths like ../../something
        if (fileName == null || !fileName.matches("[A-Za-z0-9._-]+") || fileName.contains("..")) {
            throw new ApiException("invalid image name");
        }
        String fullPath = path + File.separator + fileName;
        InputStream is = new FileInputStream(fullPath);
        return is;
    }

    // only reasonably sized jpg and png images are accepted
    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("please select an image to upload");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new ApiException("image must be smaller than 5 MB");
        }

        String name = file.getOriginalFilename();
        if (name == null || !name.contains(".")) {
            throw new ApiException("image file must have an extension");
        }

        String extension = name.substring(name.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension) || !ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new ApiException("only jpg, jpeg and png images are allowed");
        }
    }

}
