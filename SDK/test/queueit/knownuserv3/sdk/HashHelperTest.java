/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package queueit.knownuserv3.sdk;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author MOSA
 */
public class HashHelperTest {
    @Test
      public void generateSHA256Hash() throws Exception  
      {
          String key = "528f01d4-30f9-4753-95b3-2c8c33966abc";
          String stringToHash = "ts_1480593661~cv_10~ce_false~q_944c1f44-60dd-4e37-aabc-f3e4bb1c8895~c_customerid~e_eventid~rt_disabled";
          
          assertTrue( HashHelper.generateSHA256Hash(key, stringToHash).equals("286a17fb82009d8556465b2f0880a8e5e9565b42490669f4b8f1b93b7d6ddd51"));
      }
}
