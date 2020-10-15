function openFile() {
    // Load the client, auth2 and picker libraries
    gapi.load('client:auth2:picker', initClient);
}

function initClient() {
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
            onStatusChange(authenticated);

        }, function () { console.log("error") });
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