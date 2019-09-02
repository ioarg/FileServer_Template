package jarg.templates.FileServer.controllers;

import jarg.templates.FileServer.file_utilities.json_mapping.FileData;
import jarg.templates.FileServer.file_utilities.json_mapping.MessageResponse;
import jarg.templates.FileServer.file_utilities.operations.FileSearching;
import jarg.templates.FileServer.file_utilities.operations.ShallowFileSearch;
import jarg.templates.FileServer.notifications.ClientNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

@RestController
@RequestMapping("/filemanager")
public class FileRequestsController {
    @Autowired
    private ExecutorService execService;
    @Autowired
    private String storageDirectory;
    @Autowired
    private ClientNotifier clientNotifier;
    private final Logger logger = LoggerFactory.getLogger(FileRequestsController.class);

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
     * Subscription to SSE events
     *************************************************************/
    @GetMapping("/notifications")
    public SseEmitter subscribeForNotifications(){
        return clientNotifier.subscribe();
    }

    /*************************************************************
     * Manage file uploads
     * - transfer the file to the storage directory
     * - send an `upload complete` notification to the user
     *************************************************************/
    @PostMapping(
            path="/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public DeferredResult<MessageResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        String filename = file.getOriginalFilename();
        DeferredResult<MessageResponse> dfResult = new DeferredResult<>();
        dfResult.onCompletion(()->logger.info("Uploading file "+filename+" completed."));
        dfResult.onError((Throwable th)->logger.error("Error in completing file upload for file "+filename));

        execService.execute(() -> {
            try {
                file.transferTo(Paths.get(storageDirectory+filename));
                clientNotifier.sendNotification("File "+file.getOriginalFilename()+" uploaded.");
                dfResult.setResult(new MessageResponse("File"+filename+" uploaded."));
            } catch (IllegalStateException | IOException e) {
                logger.error("Error in moving file " + e.getMessage());
                dfResult.setResult(new MessageResponse("Error in uploading file "+filename));
            }
        });
        return dfResult;
    }

    /*************************************************************
     * Download file list
     *************************************************************/
    @GetMapping(
            path = "/files",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public DeferredResult<List<FileData>> getFiles(){
        DeferredResult<List<FileData>> dfResult = new DeferredResult<>();
        //This implementation omits any subdirectories
        FileSearching fileSearcher = new ShallowFileSearch();
        execService.execute(()->{
            List<FileData> fileList = fileSearcher.getFilesList(storageDirectory);
            dfResult.setResult(fileList);
        });
        return dfResult;
    }

   /* *//*************************************************************
     * Download file
     *************************************************************//*
    @GetMapping(
            path = "/files/{filename}",
            produces = MediaType.
    )
    public void getFile(@PathVariable("filename") String filename, HttpServletResponse response){

    }*/
}q