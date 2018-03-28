/*-
 * #%L
 * OneWire4J
 * %%
 * Copyright (C) 2017 - 2018 Arne Plöse
 * %%
 * OneWire4J - Drivers for the 1-wire protocol https://github.com/aploese/OneWire4J/
 * Copyright (C) 2009, 2017, Arne Plöse and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 * #L%
 */
package de.ibapl.onewire4j;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.ibapl.onewire4j.request.OneWireRequest;
import de.ibapl.onewire4j.request.communication.BitResult;
import de.ibapl.onewire4j.request.communication.DataToSend;
import de.ibapl.onewire4j.request.communication.OneWireSpeed;
import de.ibapl.onewire4j.request.communication.SingleBitRequest;
import de.ibapl.onewire4j.request.configuration.CommandType;
import de.ibapl.onewire4j.request.configuration.ConfigurationReadRequest;
import de.ibapl.onewire4j.request.configuration.ConfigurationWriteRequest;
import de.ibapl.onewire4j.request.configuration.DataSampleOffsetAndWrite0RecoveryTime;
import de.ibapl.onewire4j.request.configuration.PullDownSlewRateParam;
import de.ibapl.onewire4j.request.configuration.SerialPortSpeed;
import de.ibapl.onewire4j.request.configuration.Write1LowTime;

/**
 *
 * @author Arne Plöse
 */
public class DecoderTest {
    
    public DecoderTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of decode method, of class Decoder.
     */
    @Test
    public void testDecodeInitDS280B() throws IOException {
        System.out.println("decode");
        Decoder decoder = new Decoder(new ByteArrayInputStream(new byte[] {(byte)0x16,(byte)0x44, (byte)0x5A, (byte)0x00, (byte)0x93 }));
        OneWireRequest<?> request = ConfigurationWriteRequest.of(PullDownSlewRateParam.PDSRC_1_37);
        
        request.waitForResponse();
        decoder.decode(request);
        assertEquals(PullDownSlewRateParam.PDSRC_1_37, request.response);
        request = ConfigurationWriteRequest.of(Write1LowTime.W1LT_10);
        
        request.waitForResponse();
        decoder.decode(request);
        assertEquals(Write1LowTime.W1LT_10, request.response);
        request = ConfigurationWriteRequest.of(DataSampleOffsetAndWrite0RecoveryTime.DSO_AND_W0RT_8);
        
        request.waitForResponse();
        decoder.decode(request);
        assertEquals(DataSampleOffsetAndWrite0RecoveryTime.DSO_AND_W0RT_8, request.response);
        request = ConfigurationReadRequest.of(CommandType.RBR);
        
        request.waitForResponse();
        decoder.decode(request);
        assertEquals(SerialPortSpeed.SPS_9_6, request.response);
        SingleBitRequest singleBitRequest = new SingleBitRequest(OneWireSpeed.STANDARD, DataToSend.WRITE_0_BIT, false);

        singleBitRequest.waitForResponse();
        decoder.decode(singleBitRequest);
        assertEquals(OneWireSpeed.STANDARD, singleBitRequest.response.speed);
        assertEquals(BitResult._1_READ_BACK, singleBitRequest.response.bitResult);
        assertEquals(DataToSend.WRITE_1_OR_READ_BIT, singleBitRequest.response.dataToSend);
        

    }
    
}
