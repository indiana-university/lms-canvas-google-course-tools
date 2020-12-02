$(document).ready(function(){

    $('#pickerButton').click(function(event) {
        //In picker.js
        openFile();
    });

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
        var actionValue = $(this).data("action");
        $("#gctSubmit").val(actionValue);

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

    $(".reminder").click(function(e) {
        var username = $('#googleLogin').text();
        var foundStorageItem = localStorage.getItem('gct-account-reminder-' + username);
        if (foundStorageItem == undefined) {
            var me = $(this)
            var url = me.attr('href');
            var target = me.attr('target');

            // Listen for a custom "modalClose" event
            document.addEventListener('modalClose', event => {
              if (event.detail.name() === 'modal-account-reminder') {
                //Handle focus back on the target link
                me.attr('data-modal-trigger', 'modal-account-reminder');
                Modal.focusTrigger('modal-account-reminder');
                me.removeAttr('data-modal-trigger');

                //Clear out data variables set on the continue button
                $("#modal-account-reminder-continue").removeAttr('data-gct-url').removeAttr('data-gct-target');
              }
            }, false);

            Modal.open('modal-account-reminder', function() {
                //Setup some stuff for the "continue" button
                $("#modal-account-reminder-continue").attr('data-gct-url', url).attr('data-gct-target', target);
                Modal.focusModal('modal-account-reminder');
            });

            e.preventDefault();
            return false;
        }
    });

    $("#modal-account-reminder-continue").click(function(e) {
        var me = $(this);
        var url = me.data('gct-url');
        var target = me.data('gct-target');
        var username = $('#googleLogin').text();

        localStorage.setItem('gct-account-reminder-' + username, true);

        if (target == undefined) {
            window.location.href = url;
        } else {
            window.open(url, target);
        }
    });


});