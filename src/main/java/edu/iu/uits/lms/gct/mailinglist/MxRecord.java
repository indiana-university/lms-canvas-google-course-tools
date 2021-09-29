package edu.iu.uits.lms.gct.mailinglist;

import lombok.Data;

import java.io.Serializable;

/**
 * {
 *   "result": "success",
 *   "iuGroupEmail": "chmaurer-foobar-iu-group@iu.edu",
 *   "iuGroupForward": "chmaurer-foobar-iu-group@xmail.iu.edu"
 * }
 */
@Data
public class MxRecord implements Serializable {

   public static final String RESULT_FAILED = "failed";
   public static final String RESULT_SUCCESS = "success";

   private String result;
   private String iuGroupEmail;
   private String iuGroupForward;
}
