/*
* This class defines Server Sent Event interactions with the client
* It can be used as a bean by controllers to register clients and
* send notifications
* */
package jarg.templates.FileServer.notifications.sse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientNotifier {
    private List<SseEmitter> sseEmitters;   //A list of server sent event emitters
    private final Logger logger = LoggerFactory.getLogger(ClientNotifier.class);

    public ClientNotifier() {
        sseEmitters = Collections.synchronizedList(new ArrayList<>());
    }

    /***********************************************************
    *   Getters / Setters
    ************************************************************/
    public List<SseEmitter> getSseEmitters() {
        return sseEmitters;
    }

    public void setSseEmitters(List<SseEmitter> sseEmitters) {
        this.sseEmitters = sseEmitters;
    }

    /***********************************************************
     *   Utilities
     ************************************************************/
    //Subscribe for notifications
    public SseEmitter subscribe(){
        logger.debug("Subscription request");
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitter.onCompletion(()->{sseEmitters.remove(emitter);});
        sseEmitters.add(emitter);
        return emitter;
    }

    //Sends a notification to each subscribed emitter and removes
    //Any ones with a closed connection
    public void sendNotification(String message){
        /*  The emitters to be removed must be gathered in a List
         *   Attempting to remove the emitter inside the catch
         *   prevents the loop from continuing, so the other emitters
         *   do not get the notifications.
         */
        List<SseEmitter> toBeRemoved = new ArrayList<>();
        for(SseEmitter emitter : sseEmitters){
            try {
                emitter.send(SseEmitter.event().name("file_operations").data(message));
                logger.debug("Notification sent");
            } catch (IOException | IllegalStateException e) {
                logger.error(e.getMessage());
                toBeRemoved.add(emitter);
            }
        }
        //Now it is time to clear the list of the unused emitters (Broken Pipe)
        for(SseEmitter emitter : toBeRemoved){
            sseEmitters.remove(emitter);
        }
    }
}
