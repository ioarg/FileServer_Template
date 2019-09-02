/*
* This implementation does only a shallow file search, avoiding to look inside subdirectories
* */
package jarg.templates.FileServer.file_utilities.operations;

import jarg.templates.FileServer.file_utilities.json_mapping.FileData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ShallowFileSearch implements FileSearching {
    private Logger logger = LoggerFactory.getLogger(ShallowFileSearch.class);

    @Override
    public List<FileData> getFilesList(String storageLocation) {
        List<FileData> files = new ArrayList<>();
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(storageLocation))){
            for(Path path : stream){
                if(!Files.isDirectory(path)){
                    files.add(new FileData(path.getFileName().toString()));
                }
            }
        }catch (IOException e){
            logger.error("Could not read file list from target directory.");
        }
        return files;
    }
}
