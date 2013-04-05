package org.baessie.simulator;

public class SimulatorException extends Exception {

   private static final long serialVersionUID = -3622683825648081911L;

   public SimulatorException(final String msg) {
      super(msg);
   }

   public SimulatorException(final String message, final Throwable cause) {
      super(message, cause);
   }

   public SimulatorException(final Throwable cause) {
      super(cause);
   }

}
