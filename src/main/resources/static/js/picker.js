/*-
 * #%L
 * google-course-tools
 * %%
 * Copyright (C) 2015 - 2025 Indiana University
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

function initClient() {
    // Load the client, auth2 and picker libraries
    gapi.load('client:auth2:picker', function() {
    gapi.client.init({
        clientId: clientId,
        scope: 'https://www.googleapis.com/auth/drive.readonly'
    }).then(
        function () {
            console.log("init");

            // Check if we are logged in.
            auth = gapi.auth2.getAuthInstance();
            auth.isSignedIn.listen(onStatusChange);
            authenticated = auth.isSignedIn.get();

        }, function () { console.log("error") });
        });
}

function launchPicker() {
    onStatusChange(authenticated);
}

function onStatusChange(isSignedIn) {
    if (isSignedIn) {
        authenticated = true;
        user = auth.currentUser.get();
        response = user.getAuthResponse(true);
        token = response.access_token;
        pickerLoaded = true;
        showPicker();
    } else {
        authenticated = false;
        gapi.auth2.getAuthInstance().signIn();
    }
}

function showPicker() {
    if (pickerLoaded && authenticated) {
        var view = new google.picker.DocsView(google.picker.ViewId.DOCS);
        view.setIncludeFolders(true);
        view.setSelectFolderEnabled(true);
        view.setParent('root');
        view.setMode(google.picker.DocsViewMode.LIST);
        var resolvedOrigin = window.location.hostname === 'localhost' ? window.location.origin : canvasOrigin;
        var picker = new google.picker.PickerBuilder()
            .disableFeature(google.picker.Feature.NAV_HIDDEN)
            .enableFeature(google.picker.Feature.MULTISELECT_ENABLED)
            .setAppId(appId)
            .setOAuthToken(token)
            .addView(view)
//            .addView(new google.picker.DocsUploadView())
//            .setDeveloperKey(developerKey)
            .setOrigin(resolvedOrigin)
            .setCallback(onDriveFileOpen)
            .build();
        picker.setVisible(true);
    }
}

function onDriveFileOpen(data) {
    console.log(data);
    if (data.action == google.picker.Action.PICKED) {
        var fileIds = data.docs.map(e => e.id);
        $('#fileIds').val(fileIds);
        $('#fileIds').change();
        var fileNames = data.docs.map(e => e.name).join(", ");

        if (fileIds.length > 1) {
            $("#fileSelectionDescription").html(fileIds.length + " items selected");
            $("#fileSelectionDescription").attr('title', fileNames);
        } else {
            $("#fileSelectionDescription").html(fileNames);
        }
    }
}