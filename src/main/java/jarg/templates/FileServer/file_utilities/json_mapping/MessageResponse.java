/*
 * This class will be used to send messages as data in a json object
 * */
package jarg.templates.FileServer.file_utilities.json_mapping;

public class MessageResponse {
    private String message;

    public MessageResponse(){

    }

    public MessageResponse(String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
