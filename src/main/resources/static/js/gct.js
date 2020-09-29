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

    $(".loading-btn").click(function(event) {

        $('form').preventDoubleSubmission();
        $(this).find(".rvt-loader").removeClass("rvt-display-none");
        $(this).find(".loading-content").addClass("rvt-button__content");
        $(".loading-btn").addClass("rvt-button--loading");
    });

    // jQuery plugin to prevent double submission of forms
    jQuery.fn.preventDoubleSubmission = function() {
        $(this).on('submit',function(e){
            var $form = $(this);

            if ($form.data('submitted') === true) {
                // Previously submitted - don't submit again
                e.preventDefault();
            } else {
                // Mark it so that the next submit can be ignored
                $form.data('submitted', true);
                var buttons = $(':button');
                $(buttons).each(function() {
                    $(this).prop('disabled', true);
                    $(this).prop('aria-busy', true);
                });
            }
        });

        // Keep chainability
        return this;
    };


});