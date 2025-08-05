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

let APP_ID = null;
let API_KEY = null;
let CLIENT_ID = null;
let SCOPES = null;
let ORIGIN = null;

let tokenClient = null;
let accessToken = null;
let pickerInited = false;
let gisInited = false;

function initClient(appId, apiKey, clientId, scopes, origin) {
    APP_ID = appId;
    API_KEY = apiKey;
    CLIENT_ID = clientId;
    SCOPES = scopes;
    ORIGIN = origin;

    let script = document.createElement('script');
    script.src = 'https://apis.google.com/js/api.js';
    script.async = true;
    script.defer = true;
    script.onload = gapiLoaded;
    document.head.appendChild(script);

    script = document.createElement('script');
    script.src = 'https://accounts.google.com/gsi/client';
    script.async = true;
    script.defer = true;
    script.onload = gisLoaded;
    document.head.appendChild(script);
}

function launchPicker() {
    tokenClient.callback = async (response) => {
        if (response.error !== undefined) {
            throw (response);
        }

        accessToken = response.access_token;

        await createPicker();
    };

    if (accessToken === null) {
    // Prompt the user to select a Google Account and ask for consent to share their data
    // when establishing a new session.
        tokenClient.requestAccessToken({prompt: 'consent'});
    } else {
    // Skip display of account chooser and consent dialog for an existing session.
        tokenClient.requestAccessToken({prompt: ''});
    }
}

  /**
   * Callback after api.js is loaded.
   */
  function gapiLoaded() {
    gapi.load('client:picker', initializePicker);
  }

  /**
   * Callback after the API client is loaded. Loads the
   * discovery doc to initialize the API.
   */
  async function initializePicker() {
    await gapi.client.load('https://www.googleapis.com/discovery/v1/apis/drive/v3/rest');
    pickerInited = true;
    maybeEnableButtons();
  }

  /**
   * Callback after Google Identity Services are loaded.
   */
  function gisLoaded() {
    tokenClient = google.accounts.oauth2.initTokenClient({
      client_id: CLIENT_ID,
      scope: SCOPES,
      callback: '', // defined later
    });
    gisInited = true;
    maybeEnableButtons();
  }

  function maybeEnableButtons() {
    if (pickerInited && gisInited) {
      console.log("Enabling buttons");
    }
  }

/**
 *  Create and render a Picker object for searching images.
 */
function createPicker() {
  const docsView = new google.picker.DocsView(google.picker.ViewId.DOCS);
  docsView.setIncludeFolders(true);
  docsView.setMode(google.picker.DocsViewMode.LIST);
  docsView.setSelectFolderEnabled(true);
  docsView.setParent('root');

  console.log("***************** SHOW Picker *****************");

  console.log(">>>> Origin = " + ORIGIN);

  const picker = new google.picker.PickerBuilder()
      .disableFeature(google.picker.Feature.NAV_HIDDEN)
      .enableFeature(google.picker.Feature.MULTISELECT_ENABLED)
      .setDeveloperKey(API_KEY)
      .setAppId(APP_ID)
      .setOAuthToken(accessToken)
      .addView(docsView)
      .setCallback(onDriveFileOpen)
      .setOrigin(ORIGIN)
      .build();
  picker.setVisible(true);
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

window.gapiLoaded = gapiLoaded;
window.gisLoaded = gisLoaded;
