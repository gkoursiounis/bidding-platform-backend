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
@RequestMapping("/media")
public class FileController extends BaseController {

    private final DBFileStorageService dBFileStorageService;
    private final DBFileRepository dbFileRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private AccountController accountController;

    @Autowired
    public FileController(DBFileStorageService dBFileStorageService,
                          DBFileRepository dbFileRepository, ItemRepository itemRepository,
                          UserRepository userRepository, AccountController accountController) {
        this.dBFileStorageService = dBFileStorageService;
        this.dbFileRepository = dbFileRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.accountController = accountController;
    }

    /**
     * User can upload more pictures for each item
     *
     * @param itemId
     * @param media
     * @return the updated item
     */
    @PatchMapping("/uploadPicture/{itemId}")
    public ResponseEntity uploadPicture(@PathVariable(value = "itemId") long itemId,
                                        @RequestParam(name = "media") MultipartFile media){

        User requestUser = accountController.requestUser();

        Item item = itemRepository.findItemById(itemId);
        if(item == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
                    "Error",
                    "Item not found. Invalid item Id"
            ));
        }

        if(media != null){

            //check for proper content type
            if (!contentTypes.contains(media.getContentType())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                        "Error",
                        "Image type not supported"
                ));
            }

            //check for size limitations (less than 10 MB)
            if (media.getSize() > DBFile.MAXIMUM_IMAGE_SIZE && (
                    "image/png".equals(media.getContentType())  || "image/jpeg".equals(media.getContentType()) ||
                            "image/gif".equals(media.getContentType()))) {

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                        "Error",
                        "Image over limits"
                ));
            }

            DBFile dbFile = dBFileStorageService.storeFile(media);
            dbFile.setDownloadLink("/downloadFile/" + dbFile.getId() + "." + dbFile.getFileType().split("/")[1]);
            dbFile = dbFileRepository.save(dbFile);
            item.getMedia().add(dbFile);
        }
        else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "Picture is missing"
            ));
        }

        itemRepository.save(item);
        requestUser.getItems().add(item);
        userRepository.save(requestUser);

        return ResponseEntity.ok(item);
    }



    @GetMapping("/downloadPicture/{fileId}")
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
