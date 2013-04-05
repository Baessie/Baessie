package org.baessie.simulator.rest;

public class MissingParameterException extends Exception {

   private static final long serialVersionUID = 2868658273844939175L;

   public MissingParameterException() {
      super();
   }

   public MissingParameterException(final String message, final Throwable cause) {
      super(message, cause);
   }

   public MissingParameterException(final String message) {
      super(message);
   }

   public MissingParameterException(final Throwable cause) {
      super(cause);
   }

}
