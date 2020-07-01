package edu.iu.uits.lms.gct.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenInfo {

   private String clientId;
   private String projectId;
   private String devKey;
}
