package edu.iu.uits.lms.gct.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.iu.uits.lms.gct.Constants;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class PickerResponse implements Serializable {
   private String description;
//   private String driveError;
//   private boolean driveSuccess;
//   private String embedUrl;
   private String iconUrl;
   private String id;

//   @JsonProperty("isShared")
//   private boolean shared;
//   private long lastEditedUtc;
   private String mimeType;
   private String name;
   private String parentId;
//   private String serviceId;
//   private int sizeBytes;
   private String type;
   private String url;

   public boolean isFolder() {
      return Constants.FOLDER_MIME_TYPE.equals(mimeType);
   }

   public String getIconUrl32() {
      return iconUrl.replace("/16/", "/32/");
   }

   /*
   [{"id":"0B5k7uIy6xQbMUWdSLW8zYXlpalU","serviceId":"docs","mimeType":"application/vnd.google-apps.folder","name":"LMS Team","description":"","type":"folder","lastEditedUtc":1597405965912,"iconUrl":"https://drive-thirdparty.googleusercontent.com/16/type/application/vnd.google-apps.folder+shared","url":"https://drive.google.com/drive/folders/0B5k7uIy6xQbMUWdSLW8zYXlpalU","embedUrl":"https://drive.google.com/embeddedfolderview?id=0B5k7uIy6xQbMUWdSLW8zYXlpalU","driveSuccess":false,"driveError":"NETWORK","sizeBytes":0,"parentId":"root","isShared":true},
   {"id":"17OhFWQC9_F02yuCuBdMwhxWBPxpiXAkY","serviceId":"docs","mimeType":"image/jpeg","name":"RHIT_Bonfire_ZoomBG.jpg","description":"","type":"photo","lastEditedUtc":1591794466917,"iconUrl":"https://drive-thirdparty.googleusercontent.com/16/type/image/jpeg","url":"https://drive.google.com/file/d/17OhFWQC9_F02yuCuBdMwhxWBPxpiXAkY/view?usp=drive_web","embedUrl":"https://drive.google.com/file/d/17OhFWQC9_F02yuCuBdMwhxWBPxpiXAkY/preview?usp=drive_web","driveSuccess":false,"driveError":"NETWORK","sizeBytes":933548,"rotation":0,"rotationDegree":0,"parentId":"root"},
   {"id":"0B5k7uIy6xQbMc3RhcnRlcl9maWxlX2Rhc2hlclYw","serviceId":"DoclistBlob","mimeType":"application/pdf","name":"Getting started","description":"","type":"file","lastEditedUtc":1463606052288,"iconUrl":"https://drive-thirdparty.googleusercontent.com/16/type/application/pdf","url":"https://drive.google.com/file/d/0B5k7uIy6xQbMc3RhcnRlcl9maWxlX2Rhc2hlclYw/view?usp=drive_web","embedUrl":"https://drive.google.com/file/d/0B5k7uIy6xQbMc3RhcnRlcl9maWxlX2Rhc2hlclYw/preview?usp=drive_web","driveSuccess":false,"driveError":"NETWORK","sizeBytes":696774,"parentId":"root"}]
    */
}
