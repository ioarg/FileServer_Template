/*
*   Script to manage visuals and Ajax requests of the home page
*/

/****************************************************************
*   Globals
****************************************************************/
const fileListUrl = "/filemanager/files";
const downloadUrl = "/filemanager/download";
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
        tableContent +=
            `<tr>\
                 <td>${filenames[i]["filename"]}</td>\
                 <td>\
                     <button class="btn btn-primary download_btn">Download</button>\
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
//Send get files request
function getFiles(){
    $.ajax({
        method : "GET",
        headers:{ "Accept" : "application/json"},
        url : fileListUrl,
        success : (files)=>{
            console.log(files);
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
*   Document Ready
****************************************************************/
$(document).ready(function(){

    //Event listeners =============================

    //Actions ===================================
    getFiles();
});