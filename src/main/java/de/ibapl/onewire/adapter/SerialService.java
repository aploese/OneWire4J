/*---------------------------------------------------------------------------
 * Copyright (C) 2001-2003 Dallas Semiconductor Corporation, All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY,  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DALLAS SEMICONDUCTOR BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dallas Semiconductor
 * shall not be used except as stated in the Dallas Semiconductor
 * Branding Policy.
 *---------------------------------------------------------------------------
 */
package de.ibapl.onewire.adapter;

import de.ibapl.onewire.OneWireAccessProvider;
import de.ibapl.onewire.utils.Convert;
import de.ibapl.spsw.api.Baudrate;
import de.ibapl.spsw.api.DataBits;
import de.ibapl.spsw.api.FlowControl;
import de.ibapl.spsw.api.Parity;
import de.ibapl.spsw.api.SerialPortSocket;
import de.ibapl.spsw.api.SerialPortSocketFactory;
import de.ibapl.spsw.api.StopBits;
import de.ibapl.spsw.logging.LoggingSerialPortSocket;
import de.ibapl.spsw.logging.TimeStampLogging;
import de.ibapl.spsw.provider.SerialPortSocketFactoryImpl;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  <p>The SerialService class provides serial IO services to the
 *  USerialAdapter class. </p>
 *
 *  @version    1.00, 1 Sep 2003
 *  @author     DS
 *
 */
public class SerialService {
   private final static Logger LOG = Logger.getLogger(SerialService.class.getCanonicalName());

    /** The serial port object for setting serial port parameters */
   private SerialPortSocket serialPortSocket;
   /** The input stream, for reading data from the serial port */
   private InputStream serialInputStream;
   /** The output stream, for writing data to the serial port */
   private OutputStream serialOutputStream;
   /** The hash code of the thread that currently owns this serial port */
   @Deprecated
   private int currentThreadHash;
   /** temporary array, used for converting characters to bytes */
   @Deprecated // TODO use byte in the first place
   private byte[] tempArray = new byte[128];
   /** used to end the Object.wait loop in readWithTimeout method */
   private transient boolean dataAvailable = false;

   /** Vector of thread hash codes that have done an open but no close */
   private final List<Thread> users = new ArrayList<>(4);

   /** Flag to indicate byte banging on read */
   private final boolean byteBang;

   /** Vector of serial port ID strings (i.e. "COM1", "COM2", etc) */
   @Deprecated
   private static final List<String> vPortIDs = new ArrayList<>(2);
   /** static list of threadIDs to the services they are using */
   @Deprecated
   private static Map knownServices = new HashMap();
   /** static list of all unique SerialService classes */
   @Deprecated
   private static Map uniqueServices = new HashMap();


   /**
    * Cleans up the resources used by the thread argument.  If another
    * thread starts communicating with this port, and then goes away,
    * there is no way to relinquish the port without stopping the
    * process. This method allows other threads to clean up.
    *
    * @param  thread that may have used a <code>USerialAdapter</code>
    */
   public static void CleanUpByThread(Thread t)
   {
      LOG.finest("start cleanUpByThread");
      try
      {
         SerialService temp = (SerialService) knownServices.get(t);
         if (temp==null)
           return;

         synchronized(temp)
         {
            if (t.hashCode() == temp.currentThreadHash)
            {
                //then we need to release the lock...
                temp.currentThreadHash = 0;
            }
         }

         temp.closePortByThreadID(t);
         knownServices.remove(t);
      }
      catch(Exception e)
      {
            LOG.log(Level.WARNING, "Exception cleaning: ", e);
      }
   }

   /**
    * do not use default constructor
    * user getSerialService(String) instead.
    */
   private SerialService()
   {
       byteBang = false;
   }

   /**
    * this constructor only for use in the static method:
    * getSerialService(String)
    */
   protected SerialService(SerialPortSocket serialPortSocket)
   {
      this.serialPortSocket = serialPortSocket;
         byteBang = false;
   }

   @Deprecated // TODO cleanup ??
   public static SerialService getSerialService(SerialPortSocketFactory serialPortSocketFactory, String port, String spswLogFile) throws FileNotFoundException
   {
      synchronized(uniqueServices)
      {
               LOG.log(Level.FINEST, "SerialService.getSerialService called: strComPort={}", port);

         String strLowerCaseComPort = port.toLowerCase();
         Object o = uniqueServices.get(strLowerCaseComPort);
         if(o!=null)
         {
            return (SerialService)o;
         }
         else
         {
            SerialService sps = new SerialService();
            sps.serialPortSocket = LoggingSerialPortSocket.wrapWithHexOutputStream(SerialPortSocketFactoryImpl.singleton().createSerialPortSocket(port), new FileOutputStream(spswLogFile), false, TimeStampLogging.NONE);

            uniqueServices.put(strLowerCaseComPort, sps);
            return sps;
         }
      }
   }

   public synchronized void openPort()
      throws IOException
   {
         LOG.log(Level.FINEST, "SerialService.openPort: Thread.currentThread()={}", Thread.currentThread());
      // record this thread as an owner
      if (users.indexOf(Thread.currentThread()) == -1)
         users.add(Thread.currentThread());

      if(isPortOpen())
         return;

      // try to open the port
      try
      {
         serialPortSocket.openRaw(Baudrate.B9600, DataBits.DB_8, StopBits.SB_1, Parity.NONE, FlowControl.getFC_NONE());

         serialInputStream  = serialPortSocket.getInputStream();
         serialOutputStream = serialPortSocket.getOutputStream();
         // bug workaround
         serialOutputStream.write(0);


         serialPortSocket.setDTR(true);
         serialPortSocket.setRTS(true);

            LOG.log(Level.FINEST, "SerialService.openPort: Port Openend ({})", serialPortSocket.getPortName());
      }
      catch(Exception e)
      {
         // close the port if we have an object
         if (serialPortSocket != null)
            serialPortSocket.close();


         throw new IOException(
            "Could not open port (" + serialPortSocket.getPortName() + ") :" + e);
      }
   }

   public synchronized String getPortName()
   {
      return serialPortSocket.getPortName();
   }

   public synchronized boolean isPortOpen()
   {
      return serialPortSocket.isOpen();
   }

   /*TODO ??
   public synchronized boolean isDTR()
   {
      return serialPortSocket.isDTR();
   }
*/
   @Deprecated
   public synchronized void setDTR(boolean newDTR) throws IOException
   {
      serialPortSocket.setDTR(newDTR);
   }

   /* TODO
   public synchronized boolean isRTS()
   {
      return serialPort.isRTS();
   }
*/
   @Deprecated
   public synchronized void setRTS(boolean newRTS) throws IOException
   {
      serialPortSocket.setRTS(newRTS);
   }

   /*
    * Send a break on this serial port
    *
    * @param  duration - break duration in ms
    
   @Deprecated
   public synchronized void sendBreak (int duration) throws IOException
   {
      serialPortSocket.sendBreak(duration);
   }
*/
   @Deprecated
   public synchronized Baudrate getBaudRate() throws IOException
   {
      return serialPortSocket.getBaudrate();
   }

   @Deprecated
   public synchronized void setBaudRate(Baudrate baudRate)
      throws IOException
   {
      if(!isPortOpen())
         throw new IOException("Port Not Open");

         serialPortSocket.setBaudrate(baudRate);
   }

   /**
    * Close this serial port.
    *
    * @throws IOException - if port is in use by another application
    */
   public synchronized void closePort()
      throws IOException
   {
      LOG.finest("SerialService.closePort");
      closePortByThreadID(Thread.currentThread());
   }

   @Deprecated //TODO handle lower level ???
   public synchronized void flush()
      throws IOException
   {
      if(!isPortOpen())
         throw new IOException("Port Not Open");

      serialOutputStream.flush();
      while(serialInputStream.available()>0)
         serialInputStream.read();
   }
   // ------------------------------------------------------------------------
   // BeginExclusive/EndExclusive Mutex Methods
   // ------------------------------------------------------------------------
   /**
    * Gets exclusive use of the 1-Wire to communicate with an iButton or
    * 1-Wire Device.
    * This method should be used for critical sections of code where a
    * sequence of commands must not be interrupted by communication of
    * threads with other iButtons, and it is permissible to sustain
    * a delay in the special case that another thread has already been
    * granted exclusive access and this access has not yet been
    * relinquished. <p>
    *
    * @param blocking <code>true</code> if want to block waiting
    *                 for an excluse access to the adapter
    * @return <code>true</code> if blocking was false and a
    *         exclusive session with the adapter was aquired
    *
    */
   public boolean beginExclusive (boolean blocking)
   {
         LOG.finest("SerialService.beginExclusive(bool)");
      if (blocking)
      {
         while (!beginExclusive())
         {
             try{Thread.sleep(50);}catch(Exception e){}
         }

         return true;
      }
      else
         return beginExclusive();
   }

   /**
    * Relinquishes exclusive control of the 1-Wire Network.
    * This command dynamically marks the end of a critical section and
    * should be used when exclusive control is no longer needed.
    */
   public synchronized void endExclusive ()
   {
         LOG.finest("SerialService.endExclusive");
      // if own then release
      if (currentThreadHash == Thread.currentThread().hashCode())
      {
            currentThreadHash = 0;
      }
      knownServices.remove(Thread.currentThread());
   }

   /**
    * Check if this thread has exclusive control of the port.
    */
   public synchronized boolean haveExclusive ()
   {
      return (currentThreadHash == Thread.currentThread().hashCode());
   }

   /**
    * Gets exclusive use of the 1-Wire to communicate with an iButton or
    * 1-Wire Device.
    * This method should be used for critical sections of code where a
    * sequence of commands must not be interrupted by communication of
    * threads with other iButtons, and it is permissible to sustain
    * a delay in the special case that another thread has already been
    * granted exclusive access and this access has not yet been
    * relinquished. This is private and non blocking<p>
    *
    * @return <code>true</code> a exclusive session with the adapter was
    *         aquired
    *
    * @throws IOException
    */
   private synchronized boolean beginExclusive ()
   {
         LOG.finest("SerialService.beginExclusive()");
      if (currentThreadHash == 0)
      {
         // not owned so take
         currentThreadHash = Thread.currentThread().hashCode();
         knownServices.put(Thread.currentThread(), this);

         return true;
      }
      else if (currentThreadHash == Thread.currentThread().hashCode())
      {
         // already own
         return true;
      }
      else
      {
         // want port but don't own
         return false;
      }
   }

   /**
    * Allows clean up port by thread
    */
   private synchronized void closePortByThreadID(Thread t) throws IOException
   {
         LOG.log(Level.FINEST, "SerialService.closePortByThreadID(Thread), Thread={}", t);

      // added singleUser object for case where one thread creates the adapter
      // (like the main thread), and another thread closes it (like the AWT event)
      boolean singleUser = (users.size()==1);

      // remove this thread as an owner
      users.remove(t);

      // if this is the last owner then close the port
      if (singleUser || users.isEmpty())
      {
         // if don't own a port then just return
         if (!isPortOpen())
            return;

         // close the port
            LOG.finest("SerialService.closePortByThreadID(Thread): calling serialPort.removeEventListener() and .close()");
         serialPortSocket.close();
         serialInputStream = null;
         serialOutputStream = null;
      }
      else
            LOG.finest("SerialService.closePortByThreadID(Thread): can't close port, owned by another thread");
   }


   // ------------------------------------------------------------------------
   // Standard InputStream methods
   // ------------------------------------------------------------------------


 
   public synchronized int readWithTimeout(byte[] buffer, int offset, int length)
      throws IOException
   {
         LOG.log(Level.FINEST, "SerialService.readWithTimeout(): length={}", length);

      // set max_timeout to be very long
      long max_timeout = System.currentTimeMillis() + length*20 + 800;
      int count = 0;

      // check which mode of reading
      if (byteBang)
      {
            LOG.finest("SerialService.readWithTimeout(): byte-banging read");

         int new_byte;
         do
         {
            new_byte = serialInputStream.read();

            if (new_byte != -1)
            {
               buffer[count+offset] = (byte)new_byte;
               count++;
            }
            else
            {
               // check for timeout
               if (System.currentTimeMillis() > max_timeout)
                  break;

               // no bytes available yet so yield
               Thread.yield();

            }
         }
         while (length > count);
      }
      else
      {
         do
         {
            int get_num = serialInputStream.available();
            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
            //if(DEBUG)
            //   System.out.println("SerialService.readWithTimeout(): get_num=" + get_num + ", ms left=" + (max_timeout - System.currentTimeMillis()));
            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
            if (get_num > 0)
            {
               // check for block bigger then buffer
               if ((get_num + count) > length)
                  get_num = length - count;

               // read the block
               count += serialInputStream.read(buffer, count+offset, get_num);
            }
            else
            {
               // check for timeout
               if (System.currentTimeMillis() > max_timeout  )
                  length = 0;
               Thread.yield();
            }
         }
         while (length > count);
      }

         LOG.log(Level.FINEST, "SerialService.readWithTimeout: read {} bytes", count);
         if (LOG.isLoggable(Level.FINEST)) {
         LOG.log(Level.FINEST, "SerialService.readWithTimeout: {}", Convert.toHexString(buffer, offset, count));
         }
      // return the number of characters found
      return count;
   }

   public synchronized char[] readWithTimeout(int length)
      throws IOException
   {
      byte[] buffer = new byte[length];

      int count = readWithTimeout(buffer, 0, length);

      if (length != count)
         throw new IOException(
            "readWithTimeout, timeout waiting for return bytes (wanted "
               + length + ", got " + count + ")");

      char[] returnBuffer = new char[length];
      for(int i=0; i<length; i++)
         returnBuffer[i] = (char)(buffer[i] & 0x00FF);

      return returnBuffer;
   }

   // ------------------------------------------------------------------------
   // Standard OutputStream methods
   // ------------------------------------------------------------------------
   public synchronized void write(int data)
      throws IOException
   {
      if(!isPortOpen())
         throw new IOException("Port Not Open");

         LOG.finest("SerialService.write: write 1 byte");
         if (LOG.isLoggable(Level.FINEST)) {
             LOG.log(Level.FINEST, "SerialService.write: {}", Convert.toHexString((byte)data));
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

      try
      {
         serialOutputStream.write(data);
         serialOutputStream.flush();
      }
      catch (IOException e)
      {

         // drain IOExceptions that are 'Interrrupted' on Linux
         // convert the rest to IOExceptions
         if (!((System.getProperty("os.name").indexOf("Linux") != -1)
               && (e.toString().indexOf("Interrupted") != -1)))
            throw new IOException("write(char): " + e);
      }
   }
   public synchronized void write(byte[] data, int offset, int length)
      throws IOException
   {
      if(!isPortOpen())
         throw new IOException("Port Not Open");

         LOG.log(Level.FINEST, "SerialService.write: write {} bytes", length);
         if (LOG.isLoggable(Level.FINEST)) {
         LOG.log(Level.FINEST, "SerialService.write: {}", Convert.toHexString(data, offset, length));
      }

      try
      {
         serialOutputStream.write(data, offset, length);
         serialOutputStream.flush();
      }
      catch (IOException e)
      {

         // drain IOExceptions that are 'Interrrupted' on Linux
         // convert the rest to IOExceptions
         if (!((System.getProperty("os.name").indexOf("Linux") != -1)
               && (e.toString().indexOf("Interrupted") != -1)))
            throw new IOException("write(char): " + e);
      }
   }

   public synchronized void write(char data)
      throws IOException
   {
      write((int)data);
   }

   public synchronized void write(char[] data)
      throws IOException
   {
      write(data, 0, data.length);
   }

   @Deprecated //TODO use byte
   public synchronized void write(char[] data, int offset, int length)
      throws IOException
   {
         LOG.log(Level.FINEST, "SerialService.write: write {} chars", length);

      if(length>tempArray.length)
         tempArray = new byte[length];

      for(int i=0; i<length; i++)
         tempArray[i] = (byte)data[i];

      write(tempArray, 0, length);
   }

    void sendBreak(int durationInMs) throws IOException {
        serialPortSocket.sendBreak(durationInMs);
    }

}