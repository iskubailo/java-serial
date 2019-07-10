package iskubailo.lab.serial;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.MBusMessage;

import com.fazecast.jSerialComm.SerialPort;

public class JavaSerialApplication {
  
  private static final char[] HEX_DIGITS =
      {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

  public static void main(String[] args) throws IOException, DecoderException, InterruptedException {
    new JavaSerialApplication().run();
  }
  
  private void run() throws IOException, DecoderException, InterruptedException {
    CommandLineInterface cli = new CommandLineInterface(System.in);
    State state = new State();
    cli.println("Started");
    
    try {
      configurePort(state, cli);

      while (state.alive) {
        readFromSerialPort(state, cli);
        String line = cli.nextCommand();
        if (line == null) {
          continue;
        }
        String[] splt = line.split("\\s+", 2);
        String command = splt[0].toLowerCase();
        String arguments = splt.length > 1 ? splt[1] : null;
        
        if ("help".equals(command)) {
          printHelp(state, cli);
        } else if ("reset".equals(command)) {
          configurePort(state, cli);
        } else if ("write".equals(command)) {
          writeToSerial(state, cli, arguments);
        } else if ("exit".equals(command) || "quit".equals(command)) {
          state.alive = false;
        } else if ("delay".equals(command)) {
          delay(cli);
        } else if ("mbus".equals(command)) {
          mbusOn(state, cli);
        } else if (command.isEmpty()) {
          // ignore
        } else {
          cli.println("Unknown command: " + line);
          cli.println();
          printHelp(state, cli);
        }
      }
    } catch (Exception e) {
      cli.printError(e);
    } finally {
      cli.println("Stopping...");
      cli.shutdown();
      closePort(state, cli);
      cli.println("Done");
    }
  }

  private void printHelp(State state, CommandLineInterface cli) {
    if (state.port != null) {
      cli.println("Port: " + serialPortToString(state.port));
      cli.println("\t" + "BaudRate: " + state.port.getBaudRate());
      cli.println("\t" + "Parity: " + state.port.getParity());
      cli.println("\t" + "NumDataBits: " + state.port.getNumDataBits());
      cli.println("\t" + "NumStopBits: " + state.port.getNumStopBits());
    } else {
      cli.println("No port selected");
    }
    cli.println("Please enter the command. Availables commands are:");
    cli.println("\t" + "help - this help text");
    cli.println("\t" + "reset - reconfigure port");
    cli.println("\t" + "write <hex> - write message (enter data in hex, spaces are allowed and skipped)");
    cli.println("\t" + "delay <milliseconds> - dalay entered amount of milliseconds");
    cli.println("\t" + "exit or quit or Ctrl+C - terminate program");
    cli.println();
  }

  private void configurePort(State state, CommandLineInterface cli) throws InterruptedException {
    selectPort(state, cli);
    openPort(state, cli);
    setBaudRate(state, cli);
    setParity(state, cli);
    setNumStopBits(state, cli);
    setNumDataBits(state, cli);
    printHelp(state, cli);
  }

  private void selectPort(State state, CommandLineInterface cli) throws InterruptedException {
    closePort(state, cli);

    cli.println("Available ports:");
    SerialPort[] commPorts = SerialPort.getCommPorts();
    for (int i = 0; i < commPorts.length; i++) {
      SerialPort serialPort = commPorts[i];
      cli.printf("\t%s) %s", i, serialPortToString(serialPort));
      cli.println();
    }

    cli.print("Please choose a port: ");
    String indexOrPortDescriptor = cli.nextCommand(true);
    try {
      Integer index = Integer.valueOf(indexOrPortDescriptor);
      state.port = commPorts[index];
      cli.println(String.format("Choosen port with index %s: %s", index, serialPortToString(state.port)));
    } catch (NumberFormatException e) {
      state.port = SerialPort.getCommPort(indexOrPortDescriptor);
      cli.println(
          String.format("Choosen port with descriptor %s: %s", indexOrPortDescriptor, serialPortToString(state.port)));
    }
    cli.println();
  }

  private void setBaudRate(State state, CommandLineInterface cli) throws InterruptedException {
    cli.println("Standard baud rates: 110, 300, 600, 1200, 2400, 4800, 9600, 14400, 19200, 38400, 57600, 115200, 128000 and 256000");
    cli.print("Set baud rates (default= " + state.port.getBaudRate() + "): ");
    String baudRateStr = cli.nextCommand(true);
    int baudRate = baudRateStr.trim().isEmpty() ? state.port.getBaudRate() : Integer.parseInt(baudRateStr);
    state.port.setBaudRate(baudRate);
    cli.println("Baud rate set: " + baudRate);
    cli.println();
  }

  private void setParity(State state, CommandLineInterface cli) throws InterruptedException {
    cli.println("0 - NO_PARITY, 1 - ODD_PARITY, 2 - EVEN_PARITY, 3 - MARK_PARITY, 4 - SPACE_PARITY");
    cli.print("Set parity (default= " + state.port.getParity() + "): ");
    String parityStr = cli.nextCommand(true);
    int parity = parityStr.trim().isEmpty() ? state.port.getParity() : Integer.parseInt(parityStr);
    state.port.setParity(parity);
    cli.println("Parity set: " + parity);
    cli.println();
  }

  private void setNumStopBits(State state, CommandLineInterface cli) throws InterruptedException {
    cli.print("Set num stop bits (default= " + state.port.getNumStopBits() + "): ");
    String numStopBitsStr = cli.nextCommand(true);
    int numStopBits = numStopBitsStr.trim().isEmpty() ? state.port.getNumStopBits() : Integer.parseInt(numStopBitsStr);
    state.port.setNumStopBits(numStopBits);
    cli.println("NumStopBits set: " + numStopBits);
    cli.println();
  }

  private void setNumDataBits(State state, CommandLineInterface cli) throws InterruptedException {
    cli.print("Set num data bits (default= " + state.port.getNumDataBits() + "): ");
    String numDataBitsStr = cli.nextCommand(true);
    int numDataBits = numDataBitsStr.trim().isEmpty() ? state.port.getNumDataBits() : Integer.parseInt(numDataBitsStr);
    state.port.setNumDataBits(numDataBits);
    cli.println("NumDataBits set: " + numDataBits);
    cli.println();
  }

  private void openPort(State state, CommandLineInterface cli) {
    if (state.port == null) {
      cli.println("Error: no port selected for opening");
    }
    cli.println("Opening port...");
    if (!state.port.openPort()) {
      throw new IllegalStateException("Could not open serial port");
    }
    state.inputStream = state.port.getInputStream();
    state.outputStream = state.port.getOutputStream();
    cli.println("Port opened");
    cli.println();
  }

  private void closePort(State state, CommandLineInterface cli) {
    if (state.port == null) {
      return;
    }
    cli.println("Closing port...");
    if (!state.port.isOpen()) {
      cli.println("Port is not open");
      return;
    }
    if (!state.port.closePort()) {
      throw new IllegalStateException("Could not close serial port");
    }
    state.port = null;
    cli.println("Port closed");
  }

  private void delay(CommandLineInterface cli) throws InterruptedException {
    try {
      Integer millis = Integer.valueOf(cli.nextCommand(true));
      cli.print("Delay " + millis + "ms ... ");
      Thread.sleep(millis);
      cli.println(" Done");
      cli.println();
    } catch (NumberFormatException e) {
      cli.println("Error: delay millis not a number");
    }
  }

  private void readFromSerialPort(State state, CommandLineInterface cli) throws IOException, DecodingException {
    if (state.inputStream == null) {
      return;
    }
    if (state.inputStream.available() > 0) {
      int read = state.inputStream.read(state.readBuffer);
      state.readStream.write(state.readBuffer, 0, read);
      state.lastReadTimestamp = System.currentTimeMillis();
    }
    if (state.readStream.size() > 0 &&  state.lastReadTimestamp + 200 < System.currentTimeMillis()) {
      byte[] data = state.readStream.toByteArray();
      state.readStream.reset();
      cli.println("Read (" + data.length + " bytes): " + dataToHex(data));
      cli.println("As text: " + dataToString(data));
      if (state.mbusOn) {
        MBusMessage mBusMessage = MBusMessage.decode(data, data.length);
        mBusMessage.getVariableDataResponse().decode();
        cli.println("As M-Bus: " + mBusMessage);
      }
      cli.println();
    }
  }

  private void writeToSerial(State state, CommandLineInterface cli, String arguments) throws DecoderException, IOException, InterruptedException {
    if (state.outputStream == null) {
      cli.println("Error: no output stream");
    }
    String hexToSend;
    if (arguments != null) {
      hexToSend = arguments.replaceAll("\\s*", "");
    } else {
      hexToSend = cli.nextCommand(true);
    }
    byte[] data = Hex.decodeHex(hexToSend.toCharArray());
    cli.println("Write (" + data.length + " bytes): " + dataToHex(data));
    cli.println("As text: " + dataToString(data));
    cli.println();
    state.outputStream.write(data);
  }

  private String serialPortToString(SerialPort serialPort) {
    return String.format("%s / %s", serialPort.getSystemPortName(), serialPort.getDescriptivePortName());
  }

  private String dataToHex(byte[] data) {
    final int length = data.length;
    ByteArrayOutputStream out = new ByteArrayOutputStream(length * 3);
    for (byte b : data) {
      if (out.size() > 0) {
        out.write(' ');
      }
      out.write(HEX_DIGITS[(0xF0 & b) >>> 4]);
      out.write(HEX_DIGITS[0x0F & b]);
    }
    return out.toString();
  }

  private String dataToString(byte[] data) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < data.length; i++) {
      if (31 < data[i] && data[i] < 127) {
        sb.append((char) data[i]);
      } else {
        sb.append('.');
      }
    }
    return sb.toString();
  }
  
  private void mbusOn(State state, CommandLineInterface cli) {
    state.mbusOn = true;
    cli.println("M-Bus: on");
    cli.println();
  }

}
