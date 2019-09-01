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
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

@RestController
@RequestMapping("/filemanager")
public class FileUploadController {
    @Autowired
    private ExecutorService execService;
    @Autowired
    private String storageDirectory;
    private final Logger logger = LoggerFactory.getLogger(FileUploadController.class);
    private List<SseEmitter> sseEmitters = Collections.synchronizedList(new ArrayList<>());

    /*************************************************************
     * Subscription to SSE events
     *************************************************************/
    @GetMapping("/notifications")
    public SseEmitter subscribeForNotifications(){
        logger.info("Subscription request");
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitter.onCompletion(()->{sseEmitters.remove(emitter);});
        sseEmitters.add(emitter);
        return emitter;
    }

    private void sendNotification(String message){
        /*  The emitters to be removed must be gathered in a List
        *   Attempting to remove the emitter inside the catch
        *   prevents the loop from continuing, so the other emmiters
        *   do not get the notifications.
         */
        List<SseEmitter> toBeRemoved = new ArrayList<>();
        for(SseEmitter emitter : sseEmitters){
            try {
                emitter.send(SseEmitter.event().name("file_operations").data(message));
                logger.info("Notification sent");
            } catch (IOException e) {
                logger.error(e.getMessage());
                toBeRemoved.add(emitter);
            }
        }
        //Now it is time to clear the list of the unused emitters (Broken Pipe)
        for(SseEmitter emitter : toBeRemoved){
            sseEmitters.remove(emitter);
        }
    }
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
    public DeferredResult<ResponseData> uploadFile(@RequestParam("file") MultipartFile file) {
        String filename = file.getOriginalFilename();
        DeferredResult<ResponseData> dfResult = new DeferredResult<>();
        dfResult.onCompletion(()->logger.info("Uploading file "+filename+" completed."));
        dfResult.onError((Throwable th)->logger.error("Error in completing file upload for file "+filename));
        execService.execute(() -> {
            try {
                file.transferTo(Paths.get(storageDirectory+filename));
                sendNotification("File "+file.getOriginalFilename()+" uploaded.");
                dfResult.setResult(new ResponseData("File"+filename+" uploaded."));
            } catch (IllegalStateException | IOException e) {
                logger.error("Error in moving file " + e.getMessage());
                dfResult.setResult(new ResponseData("Error in uploading file "+filename));
            }
        });
        return dfResult;
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