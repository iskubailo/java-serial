package iskubailo.lab.serial;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CommandLineInterface extends Thread {
  private InputStream source;
  private BlockingQueue<String> queue = new LinkedBlockingQueue<>();
  private volatile boolean alive = true;

  public CommandLineInterface(InputStream source) {
    this.source = source;
    start();
  }

  @Override
  public void run() {
    try {
      String command = "";
      while (alive) {
        if (source.available() > 0) {
          char ch = (char) source.read();
          if (ch == '\n') {
            queue.put(command);
            command = "";
          } else if (ch == '\r') {
            // skip
          } else {
            command += String.valueOf(ch);
          }
        } else {
          Thread.sleep(100);
        }
      }
    } catch (InterruptedException | IOException e) {
      // ignore
    }
  }

  public void shutdown() {
    alive = false;
    interrupt();
  }

  public String nextCommand() throws InterruptedException {
    return nextCommand(false);
  }


  public String nextCommand(boolean waitForCommand) throws InterruptedException {
    if (waitForCommand) {
      return queue.take();
    } else {
      return queue.poll();
    }
  }

  public void print(String data) {
    System.out.print(data);
  }
  
  public void printf(String data, Object ... args) {
    System.out.printf(data, args);
  }
  
  public void println(String data) {
    System.out.println(data);
  }

  public void println() {
    System.out.println();
  }
  
  public void printError(Exception e) {
    e.printStackTrace();
  }
  
}
