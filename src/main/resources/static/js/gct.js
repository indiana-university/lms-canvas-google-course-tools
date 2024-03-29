/*-
 * #%L
 * google-course-tools
 * %%
 * Copyright (C) 2015 - 2022 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
$(document).ready(function(){

    // JAWS will not read alert messages if they are rendered on page load. Manually set focus on the message
    // so it's read
    if ($('#index-success').length) {
        $('#index-success').focus();
    }

    if ($('#index-errors').length) {
        $('#index-errors').focus();
    }

    if ($('#setup-errors').length) {
        $('#setup-errors').focus();
    }

    $('#pickerButton').click(function(event) {
        //In picker.js
        launchPicker();
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
        var loader = $(this).find(".rvt-loader");
        loader.removeClass("rvt-display-none");

        // Set screenreader-only text to notify there is some loading action happening
        var srText = loader.data("loader-text");
        $("#spinner-sr-text").text(srText);

        $(this).find(".loading-content").addClass("rvt-button__content");
        $(".loading-btn").addClass("rvt-button--loading");
    });

    $(".loading-inline-btn").click(function(event) {
        $(".loading-inline").show().addClass("rvt-flex");

        // Set screenreader-only text to notify there is some loading action happening
        var srText = $(this).find(".rvt-loader").data("loader-text");
        $("#spinner-sr-text").text(srText).focus();
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

    $(".dialog-close").click(function(e) {
        $("#dialog-account-reminder-continue").removeAttr('data-gct-url').removeAttr('data-gct-target');
        const dialog = document.querySelector('[data-rvt-dialog="dialog-account-reminder"]');
        dialog.close();
        $('#menu-link-share').focus();
    })

    $(".reminder").click(function(e) {
        var username = $('#googleLogin').text();
        var foundStorageItem = localStorage.getItem('gct-account-reminder-' + username);
        if (foundStorageItem == undefined) {
            var me = $(this)
            var url = me.attr('href');
            var target = me.attr('target');
            // Trigger is required if we want the dialog to open, but we don't want to add it to the markup since
            // we only want it if it hasn't been dismissed
            me.attr('data-rvt-dialog-trigger', 'dialog-account-reminder');

            const dialog = document.querySelector('[data-rvt-dialog="dialog-account-reminder"]');
            dialog.open();
            $("#dialog-account-reminder-continue").attr('data-gct-url', url).attr('data-gct-target', target);
            dialog.focusDialog();

            e.preventDefault();
            return false;
        }
    });

    $("#dialog-account-reminder-continue").click(function(e) {
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
