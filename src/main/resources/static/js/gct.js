$(document).ready(function(){

    $('form#share-form').change(function(){
        //Make sure a folder option is selected

        var destFolderVal = $('select#folderDestination option:selected').val();
        //This should evaluate to true if it's undefined or blank
        var destFolderEmpty = !destFolderVal;

        //And at least one file has been selected
        var filesVal = $('#fileIds').val();
        var filesEmpty = !filesVal;

        var disabled = destFolderEmpty || filesEmpty;
        $('#select_options_continue').prop('disabled', disabled);
        $("#select_options_continue").attr('aria-disabled', disabled);
    });


});