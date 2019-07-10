package iskubailo.lab.jmbus;

import java.io.IOException;
import java.io.InterruptedIOException;

import org.openmuc.jmbus.MBusConnection;
import org.openmuc.jmbus.MBusConnection.MBusSerialBuilder;
import org.openmuc.jmbus.transportlayer.TransportLayer;
import org.openmuc.jmbus.VariableDataStructure;

public class JMBusTestApplication {
  
//  public static void main(String[] args) throws IOException {
//    runRxTxLibrary();
//  }

  public static void runRxTxLibrary() {
    // TODO Auto-generated method stub
    
  }

  public static void runJMbusLibrary() throws IOException, InterruptedIOException {
    MBusSerialBuilder builder = MBusConnection.newSerialBuilder("/dev/ttyUSB0");
    System.out.println("Connecting...");
    try (MBusConnection mBusConnection = builder.build()) {
      System.out.println("Reading...");
      VariableDataStructure response = mBusConnection.read(1);

      System.out.println("Read Result:");
      System.out.println(response);
    }
  }
}
