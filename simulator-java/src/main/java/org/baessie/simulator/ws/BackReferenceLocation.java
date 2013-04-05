package org.baessie.simulator.ws;

public class BackReferenceLocation {

   private final String id;
   private final String location;

   public BackReferenceLocation(final String id, final String location) {
      this.id = id;
      this.location = location;
   }

   public String getId() {
      return id;
   }

   public String getLocation() {
      return location;
   }

}
