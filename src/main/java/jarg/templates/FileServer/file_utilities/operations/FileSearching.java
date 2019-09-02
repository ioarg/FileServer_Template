/*
* An interface defining the method of getting a list of the stored files
**/
package jarg.templates.FileServer.file_utilities.operations;

import jarg.templates.FileServer.file_utilities.json_mapping.FileData;

import java.util.List;

public interface FileSearching {
    //Get the list of files in the storage location (only metadata, not the actual files)
    List<FileData> getFilesList(String storageLocation);

}
