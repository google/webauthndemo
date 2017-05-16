package com.example.guestbook;

import co.nstant.in.cbor.CborBuilder;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.MajorType;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnicodeString;
import java.util.List;

public class Main {

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO(piperc): Auto-generated method stub

    List<DataItem> l = new CborBuilder()
        .addMap()
        .put(new UnicodeString("authData"), new ByteString(new byte[4]))
        .put(new UnicodeString("fmt"), new UnicodeString("u2f"))
        .put(new UnicodeString("attStmt"), new ByteString(new byte[1]))
        .end()
        .build();
    
    for (DataItem i : l) {
      System.out.println(i.getMajorType());
      System.out.println(i.getTag());
      System.out.println("done");
      if (i.getMajorType() == MajorType.MAP) {
        System.out.println(i instanceof Map);
        Map m = (Map) i;
        for (DataItem mi : m.getKeys()) {
          System.out.print("\t" + mi.getMajorType());
          
          DataItem value = m.get(mi);
          System.out.println(": " + value.getMajorType());
          if (value.getMajorType() == MajorType.UNICODE_STRING) {
            System.out.println(((UnicodeString)value).getString());
            System.out.println(value instanceof UnicodeString);
          }
        }
      }
      
    }
  }

}
