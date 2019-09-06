/*
 * This controller is used to manage all file related requests
 * */
package jarg.templates.FileServer.controllers;

import jarg.templates.FileServer.file_utilities.json_mapping.FileData;
import jarg.templates.FileServer.file_utilities.json_mapping.MessageResponse;
import jarg.templates.FileServer.file_utilities.operations.FileSearching;
import jarg.templates.FileServer.file_utilities.operations.ShallowFileSearch;
import jarg.templates.FileServer.notifications.sse.ClientNotifier;
import jarg.templates.FileServer.notifications.websockets.CustomWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private ClientNotifier clientNotifier;      //Server Sent Events notifications
    @Autowired
    private CustomWebSocketHandler wbsConnectionsHandler;   //Web Socket notifications
    private final Logger logger = LoggerFactory.getLogger(FileRequestsController.class);

    /*************************************************************
     * Create the directory where the files will be stored
     * if it doesn't exist
     * and initialize other values
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
        dfResult.onCompletion(()->logger.debug("Uploading file "+filename+" completed."));
        dfResult.onError((Throwable th)->logger.error("Error in completing file upload for file "+filename));

        execService.execute(() -> {
            try {
                file.transferTo(Paths.get(storageDirectory+filename));
                wbsConnectionsHandler.broadcastMessage("File "+file.getOriginalFilename()+" uploaded.");
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

    /************************************************************
     * Download file as a stream of bytes
     * - this implementation disregards the file type
     * - and simply streams to the response
     ************************************************************/
    @GetMapping(
            path = "/files/{filename}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<StreamingResponseBody> downloadFile(@PathVariable("filename") String filename, HttpServletResponse response){
        //First check if the file exists
        if(Files.notExists(Paths.get(storageDirectory + filename))){
            return new ResponseEntity<StreamingResponseBody>(HttpStatus.NOT_FOUND);
        }
        StreamingResponseBody stream = (output) ->{
            //Read the file into byte chunks and send them to the response
            try(RandomAccessFile racFile = new RandomAccessFile(storageDirectory + filename, "r");
                FileChannel fileCh = racFile.getChannel()){
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                while(fileCh.read(buffer) > 0){
                    buffer.flip();
                    for(int i=0; i<buffer.limit(); i++){
                        output.write(buffer.get());
                    }
                    buffer.clear();
                }
            }catch (IOException | BufferUnderflowException e){
                logger.error(e.getMessage());
            }
        };
        return new ResponseEntity<StreamingResponseBody>(stream, HttpStatus.OK);
    }

    /************************************************************
     * Delete a file
     ************************************************************/
    @DeleteMapping(
            path = "/files/{filename}",
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public DeferredResult<ResponseEntity<String>> deleteFile(@PathVariable String filename){
        DeferredResult<ResponseEntity<String>> response = new DeferredResult<>();
        execService.execute(()->{
            Path filePath = Paths.get(storageDirectory + filename);
            if(Files.notExists(filePath)){
                logger.error("File could not be found");
                response.setResult(new ResponseEntity<String>("File not found", HttpStatus.NOT_FOUND));
            }else{
                try {
                    Files.delete(filePath);
                    logger.debug("File deleted");
                    response.setResult(new ResponseEntity<String>("File deleted!", HttpStatus.OK));
                } catch (IOException e) {
                    logger.error("Error in deleting file");
                    response.setResult(new ResponseEntity<String>("The server couldn't delete the file",
                            HttpStatus.INTERNAL_SERVER_ERROR));
                }
            }
        });
        return response;
    }


}