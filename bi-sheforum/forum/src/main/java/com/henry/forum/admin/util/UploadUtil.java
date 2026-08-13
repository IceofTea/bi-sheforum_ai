package com.henry.forum.admin.util;

import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;


@Service
public class UploadUtil {

    public static String save(MultipartFile file, String uploadFilePath) throws IOException  {
//        if (file.isEmpty()) {
//            throw new Exception("存储空文件失败 " + file.getOriginalFilename());
//        }
        if (!Files.exists(Path.of(uploadFilePath))) {
            try {
                Files.createDirectory(Path.of(uploadFilePath));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String srtuuid = UUID.randomUUID().toString();
        String fileName=file.getOriginalFilename();
        fileName=srtuuid+fileName.substring(fileName.lastIndexOf('.'));
        File dest = new File(uploadFilePath);
        file.transferTo(new File(dest, fileName));
        return fileName;
    }
    public static String savename(MultipartFile file, String uploadFilePath) throws IOException  {
//      if (file.isEmpty()) {
//          throw new Exception("存储空文件失败 " + file.getOriginalFilename());
//      }
        if (!Files.exists(Path.of(uploadFilePath))) {
            try {
                Files.createDirectory(Path.of(uploadFilePath));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String srtuuid = UUID.randomUUID().toString();
        String fileName=srtuuid+"_"+file.getOriginalFilename();
        System.out.println(fileName);
        File dest = new File(uploadFilePath);
        file.transferTo(new File(dest, fileName));
        return fileName;
    }
    public static void deleteAll(Path rootLocation) {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }
}