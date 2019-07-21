package com.Auctions.backEnd.services.File;

import com.Auctions.backEnd.exception.*;
import com.Auctions.backEnd.models.DBFile;
import com.Auctions.backEnd.repositories.DBFileRepository;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
@NoArgsConstructor
public class DBFileStorageService {

    @Autowired
    private DBFileRepository dbFileRepository;

    public DBFile storeFile(MultipartFile file) {
        FileOutputStream fs = null;
        DBFile savedDbFile = null;

        try {
            file.getBytes();
            // Normalize file name
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            DBFile dbFile = new DBFile(fileName, file.getContentType(), file.getBytes());

            savedDbFile = dbFileRepository.save(dbFile);
            String uuid = savedDbFile.getId();

            fs = new FileOutputStream("media/" + uuid);
            fs.write(file.getBytes());
            return savedDbFile;

        } catch (IOException | NullPointerException ex) {
            System.out.println("Could not store file . Please try again!"+  ex);
            return savedDbFile;
        } finally {
            if (fs != null) {
                try{
                    file.getSize();
                    fs.close();
                } catch (IOException ioex){
                    System.out.println("Could not close file . Please try again! "+ ioex);
                }
            }
        }
    }

    public DBFile getFile(String fileId) {

        return dbFileRepository.findById(fileId)
                .orElseThrow(() -> new FileStorageException("File not found with id " + fileId));
    }

    //TODO download file

}