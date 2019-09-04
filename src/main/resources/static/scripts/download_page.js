/*
*   Script to manage visuals and Ajax requests of the download page
*/

/****************************************************************
*   Globals
****************************************************************/
const fileListUrl = "/filemanager/files";
const downloadUrl = "/filemanager/files/";
const deleteUrl = "/filemanager/files/";

/****************************************************************
*   Visuals Management
****************************************************************/
//Populate the table of files with the given data
function updateFileTable(filenames){
    //Construct table head
    let tableHead =
        `<table id="file_list_table" class="table table-custom table-striped table-bordered">\
            <thead class="thead-dark">\
               <tr>\
                    <th scope="col">FileName</th>\
                    <th scope="col">Operation</th>\
                </tr>\
            </thead>`;
    //Construct table content
    let tableContent = ``;
    for(let i=0; i<filenames.length; i++){
        let filename = filenames[i]["filename"];
        let downloadLink = downloadUrl.concat(filename);
        let deleteId = "del_".concat(filename);
        tableContent +=
            `<tr>\
                 <td class="filename_td">${filename}</td>\
                 <td>\
                     <a class="btn btn-primary btn_link" href="${downloadLink}" target="_blank" download>Download</a>\
                     <button id="${deleteId}" class="btn btn-danger delete_btn">Delete</button>\
                 </td>\
             </tr>`
    }
    let tableEnd = `</table>`;
    $("#file_list_container").html(tableHead + tableContent + tableEnd);
}

/****************************************************************
*   Ajax Calls
****************************************************************/
//Get files request
function getFiles(){
    $.ajax({
        method : "GET",
        headers:{ "Accept" : "application/json"},
        url : fileListUrl,
        success : (files)=>{
            if((files != null) && (files.length != 0)){
                updateFileTable(files);
            }else{
                $("#file_list_container").html("<p>No files to download</p>");
            }
        },
        error : ()=>{
            alert("Unable to get files");
        }
    });
}

//Delete Request - Refresh table on success
function deleteFile(filename){
    $.ajax({
        method : "DELETE",
        url : deleteUrl + filename,
        headers : {"Accept" : "text/plain"},
        success: (message)=>{
            if(message != null){
                alert(message);
                getFiles();
            }
        },
        error : ()=>{
            alert("Error in deleting file");
        }
    });
}

/****************************************************************
*   Document
*   - events for dynamic content
****************************************************************/
//Pressing the delete btn
$(document).on( "click", ".delete_btn", function(){
    let filename = this.id.substring(4, this.id.length);
    deleteFile(filename);
});

/****************************************************************
*   Document Ready
****************************************************************/
$(document).ready(function(){

    //Actions ===================================
    getFiles();
});