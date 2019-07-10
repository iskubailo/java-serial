package iskubailo.lab.jmbus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.openmuc.jmbus.MBusConnection.MBusSerialBuilder;
import org.openmuc.jmbus.transportlayer.TransportLayer;

import com.fazecast.jSerialComm.SerialPort;

public class MBusCustomSerialBuilder extends MBusSerialBuilder {

  private String storedSerialPortName;


  protected MBusCustomSerialBuilder(String serialPortName) {
    super(serialPortName);
    storedSerialPortName = serialPortName;
  }
  
  @Override
  protected TransportLayer buildTransportLayer() {
    return new CustomSerialTransportLayer(storedSerialPortName);
  }
  

public class CustomSerialTransportLayer implements TransportLayer {
  
  private final SerialPort serialPort;
  private DataOutputStream dataOutputStream;
  private DataInputStream dataInputStream;
  
  public CustomSerialTransportLayer(String portDescriptor) {
    serialPort = SerialPort.getCommPort(portDescriptor);
    serialPort.setBaudRate(2400);
    serialPort.setParity(SerialPort.EVEN_PARITY);
    serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
    serialPort.setNumDataBits(8);
  }

  @Override
  public void open() throws IOException {
    boolean portOpened = serialPort.openPort();
    if (!portOpened) {
      throw new IOException("Serial port was not opened for unknown reasonm");
    }
    dataOutputStream = new DataOutputStream(serialPort.getOutputStream());
    dataInputStream = new DataInputStream(serialPort.getInputStream());
  }

  @Override
  public void close() {
    serialPort.closePort();
  }

  @Override
  public DataOutputStream getOutputStream() {
    return dataOutputStream;
  }

  @Override
  public DataInputStream getInputStream() {
    return dataInputStream;
  }

  @Override
  public boolean isClosed() {
    return !serialPort.isOpen();
  }

  @Override
  public void setTimeout(int timeout) throws IOException {
    // TODO Auto-generated method stub
  }

  @Override
  public int getTimeout() throws IOException {
    // TODO Auto-generated method stub
    return 0;
  }

}
  
}