package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.DBFile;
import com.Auctions.backEnd.models.Item;
import com.Auctions.backEnd.models.User;
import com.Auctions.backEnd.repositories.ItemRepository;
import com.Auctions.backEnd.repositories.UserRepository;
import com.Auctions.backEnd.responses.Message;
import com.Auctions.backEnd.repositories.DBFileRepository;
import com.Auctions.backEnd.services.File.DBFileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/media")
public class FileController extends BaseController{

    private final DBFileStorageService dBFileStorageService;
    private final DBFileRepository dbFileRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Autowired
    public FileController(DBFileStorageService dBFileStorageService,
                          DBFileRepository dbFileRepository, ItemRepository itemRepository,
                          UserRepository userRepository) {
        this.dBFileStorageService = dBFileStorageService;
        this.dbFileRepository = dbFileRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }


    /**
     * A user can download an item picture
     *
     * @param fileId
     * @return
     */
    @GetMapping("/downloadFile/{fileId}")
    public ResponseEntity downloadFile(@PathVariable(value = "fileId") String fileId) {

        DBFile dbFile = dBFileStorageService.getFile(fileId);
        FileInputStream FIS = null;

        try{
            final File f = new File("media/" + dbFile.getId());
            byte[] fileBytes = new byte[(int) f.length()];
            FIS = new FileInputStream(f);
            int numberOfBytes = FIS.read(fileBytes);
            System.out.println(numberOfBytes);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(dbFile.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dbFile.getFileName() + "\"")
                    .body(new ByteArrayResource(fileBytes));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
                    "Error",
                    "File not found"
                    ));

        } finally {
            if (FIS != null)
                try {
                    FIS.close();
                } catch (IOException ioex) {
                    System.out.println("Found Error Closing the File: "+ ioex);
                }
        }
    }
}
