package iskubailo.lab.serial;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.fazecast.jSerialComm.SerialPort;

public class State {
  public boolean alive = true;
  
  public SerialPort port;
  public InputStream inputStream;
  public OutputStream outputStream;

  public byte[] readBuffer = new byte[1024];
  public ByteArrayOutputStream readStream = new ByteArrayOutputStream();
  public long lastReadTimestamp = 0;
  
  public boolean mbusOn = false;
}
