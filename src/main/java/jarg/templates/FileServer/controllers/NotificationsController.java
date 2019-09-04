package jarg.templates.FileServer.controllers;

import jarg.templates.FileServer.notifications.ClientNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Controller
@RequestMapping("/notifications")
public class NotificationsController {
    @Autowired
    private ClientNotifier clientNotifier;

    /*************************************************************
     * Subscription to SSE events
     *************************************************************/
    @GetMapping("/subscribe")
    public SseEmitter subscribeForNotifications(){
        return clientNotifier.subscribe();
    }
}
