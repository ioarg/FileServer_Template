/*
*   Script to manage visuals and Ajax requests of the home page
*/

/****************************************************************
*   Globals
****************************************************************/
const fileListUrl = "/filemanager/files";
const downloadUrl = "/filemanager/files/";
const deleteUrl = "/filemanager/delete";

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
        tableContent +=
            `<tr>\
                 <td class="filename_td">${filename}</td>\
                 <td>\
                     <a class="btn btn-primary btn_link" href="${downloadLink}" target="_blank" download>Download</a>\
                     <button class="btn btn-danger delete_btn">Delete</button>\
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
            console.log("Unable to get files");
        }
    });
}

/****************************************************************
*   Document
*   - events for dynamic content
****************************************************************/
//Pressing the delete btn
$(document).on( "click", ".delete_btn", ()=>{

});

/****************************************************************
*   Document Ready
****************************************************************/
$(document).ready(function(){

    //Actions ===================================
    getFiles();
});