/*
*   A Controller for uploading and downloading files.
*   Uses a thread pool for file uploads, to asynchronously transfer the uploaded files to the specified
*   server location in `fileserver.properties`.
 */
package jarg.templates.FileServer.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;

@RestController
@RequestMapping("/filemanager")
public class FileUploadController {
    @Autowired
    private ExecutorService execService;
    @Autowired
    private String storageDirectory;
    private final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    /*************************************************************
     * Create the directory where the files will be stored
     * if it doesn't exist
     *************************************************************/
    @PostConstruct
    public void init(){
        Path storagePath = Paths.get(storageDirectory);
        if(Files.notExists(storagePath)){
            try {
                Files.createDirectories(storagePath);
            } catch (IOException e) {
                logger.error("Post Construct - FileUploadController - " + e.getMessage());
            }
        }
    }

    /*************************************************************
     * Manage file uploads
     *************************************************************/
    @PostMapping(path="/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseData uploadFile(@RequestParam("file") MultipartFile file) {
        logger.info("Uploading file "+file.getOriginalFilename());
        execService.execute(() -> {
            try {
                file.transferTo(Paths.get(storageDirectory+file.getOriginalFilename()));
            } catch (IllegalStateException | IOException e) {
                logger.error("Error in moving file " + e.getMessage());
            }
        });
        return new ResponseData("Upload Started");
    }
}

class ResponseData{
    private String data;
    ResponseData(){

    }
    ResponseData(String test){
        data=test;
    }
    public String getData(){
        return data;
    }
    public void setData(String test){
        data = test;
    }
}