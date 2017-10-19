
/*---------------------------------------------------------------------------
 * Copyright (C) 1999-2005 Dallas Semiconductor Corporation, All Rights Reserved.
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

// OneWireAccessProvider.java
package de.ibapl.onewire;

// imports
import de.ibapl.onewire.adapter.DSPortAdapter;
import de.ibapl.onewire.adapter.OneWireIOException;
import de.ibapl.onewire.adapter.USerialAdapter;
import de.ibapl.spsw.api.SerialPortSocketFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;


/**
 * The OneWireAccessProvider class manages the Dallas Semiconductor
 * adapter class derivatives of <code>DSPortAdapter</code>.  An enumeration of all
 * available adapters can be accessed through the
 * member function <code>EnumerateAllAdapters</code>.  This enables an
 * application to be adapter independent. There are also facilities to get a system
 * appropriate default adapter/port combination.<p>
 *
 * <H3> Usage </H3>
 *
 * <DL>
 * <DD> <H4> Example 1</H4>
 * Get an instance of the default 1-Wire adapter.  The adapter will be ready
 * to use if no exceptions are thrown.
 * <PRE> <CODE>
 *  try
 *  {
 *     DSPortAdapter adapter = OneWireAccessProvider.getDefaultAdapter();
 *
 *     System.out.println("Adapter: " + adapter.getAdapterName() + " Port: " + adapter.getPortName());
 *
 *     // use the adapter ...
 *
 *  }
 *  catch(Exception e)
 *  {
 *     System.out.println("Default adapter not present: " + e);
 *  }
 * </CODE> </PRE>
 * </DL>
 *
 * <DL>
 * <DD> <H4> Example 2</H4>
 * Enumerate through the available adapters and ports.
 * <PRE> <CODE>
 *  DSPortAdapter adapter;
 *  String        port;
 *
 *  // get the adapters
 *  for (Enumeration adapter_enum = OneWireAccessProvider.enumerateAllAdapters();
 *                                  adapter_enum.hasMoreElements(); )
 *  {
 *     // cast the enum as a DSPortAdapter
 *     adapter = ( DSPortAdapter ) adapter_enum.nextElement();
 *
 *     System.out.print("Adapter: " + adapter.getAdapterName() + " with ports: ");
 *
 *     // get the ports
 *     for (Enumeration port_enum = adapter.getPortNames();
 *             port_enum.hasMoreElements(); )
 *     {
 *        // cast the enum as a String
 *        port = ( String ) port_enum.nextElement();
 *
 *        System.out.print(port + " ");
 *     }
 *
 *     System.out.println();
 *  }
 * </CODE> </PRE>
 * </DL>
 *
 * <DL>
 * <DD> <H4> Example 3</H4>
 * Display the default adapter name and port without getting an instance of the adapter.
 * <PRE> <CODE>
 *  System.out.println("Default Adapter: " +
 *                      OneWireAccessProvider.getProperty("onewire.adapter.default"));
 *  System.out.println("Default Port: " +
 *                      OneWireAccessProvider.getProperty("onewire.port.default"));
 * </CODE> </PRE>
 * </DL>
 *
 * @see com.dalsemi.onewire.adapter.DSPortAdapter
 *
 * @version    0.00, 30 August 2000
 * @author     DS
 */
public class OneWireAccessProvider
{

   public static DSPortAdapter getAdapter (SerialPortSocketFactory spsf,
                                           String portName,
                                           String spswLogFile)
      throws OneWireIOException, OneWireException, IOException
   {
      USerialAdapter result = new USerialAdapter();
      result.selectPort(spsf, portName, spswLogFile);
      
               // check for the adapter
               if (result.adapterDetected()) {
               }
               else
               {

                  // close the port just opened
                  result.freePort();

                  throw new OneWireException("Port found \"" + portName
                                             + "\" not detected");
               }
         return result;
   }

}
