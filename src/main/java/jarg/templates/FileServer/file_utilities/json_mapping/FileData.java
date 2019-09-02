/*
* This class will be used to map file information data into json
* To be used with file list retrieval requests
* */
package jarg.templates.FileServer.file_utilities.json_mapping;

public class FileData {
    private String filename;

    public FileData(){

    }

    public FileData(String filename){
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
