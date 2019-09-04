/*
*   Script to manage visuals and Ajax requests of the home page
*/

/****************************************************************
*   Globals
****************************************************************/
const uploadUrl = "/filemanager/upload";
const notificationsUrl = "/notifications/subscribe"

/****************************************************************
*   Visuals Management
****************************************************************/
//Resize the content
function resizeContent(){
    let winWidht = $(window).width();
    let bodyContWidth = (winWidht*2)/3;
    let notificationSectWidth = winWidht - bodyContWidth - 20;
    $("#body-container").width(bodyContWidth);
    $("#notification-section").width(notificationSectWidth);
}

//Adds new notifications to the notification section
function addNotification(notification){
    $("#notification-section").append(`<p class="notification">${notification}</p>`);
}

/****************************************************************
*   Ajax Calls
****************************************************************/
//Send upload request
function upload(){
    let uploadData = new FormData($("#uploadForm")[0]);
    $.ajax({
        method : "POST",
        headers:{ "Accept" : "application/json"},
        url : uploadUrl,
        data : uploadData,
        contentType : false,
        cache : false,
        processData :false,
        success : (response)=>{
            console.log(response);
        },
        error : ()=>{
            console.log("Unable to upload file");
        }
    });
}


/****************************************************************
*   Document Ready
****************************************************************/
$(document).ready(function(){

    $(window).resize(resizeContent());

    $("#upload_btn").click(function(){
        upload();
    });

    /* Define Event Source for Server Sent Event */
    var eventSrc = new EventSource(notificationsUrl);
    eventSrc.addEventListener("file_operations", function(event){
        addNotification(event.data);
    });
});