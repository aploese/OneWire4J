/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ibapl.onewire.ng;

import de.ibapl.onewire.ng.request.OneWireRequest;
import de.ibapl.onewire.ng.request.OneWireRequest.RequestState;
import de.ibapl.onewire.ng.request.Speed;
import de.ibapl.onewire.ng.request.communication.BitResult;
import de.ibapl.onewire.ng.request.communication.DataToSend;
import de.ibapl.onewire.ng.request.communication.SingleBitRequest;
import de.ibapl.onewire.ng.request.communication.SingleBitResponse;
import de.ibapl.onewire.ng.request.configuration.CommandType;
import de.ibapl.onewire.ng.request.configuration.ConfigurationReadRequest;
import de.ibapl.onewire.ng.request.configuration.ConfigurationWriteRequest;
import de.ibapl.onewire.ng.request.configuration.DataSampleOffsetAndWrite0RecoveryTime;
import de.ibapl.onewire.ng.request.configuration.PullDownSlewRateParam;
import de.ibapl.onewire.ng.request.configuration.RS232BaudRate;
import de.ibapl.onewire.ng.request.configuration.Write1LowTime;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author aploese
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
        request.requestState = RequestState.WAIT_FOR_RESPONSE;
        decoder.decode(request);
        assertEquals(PullDownSlewRateParam.PDSRC_1_37, request.response);
        request = ConfigurationWriteRequest.of(Write1LowTime.W1LT_10);
        request.requestState = RequestState.WAIT_FOR_RESPONSE;
        decoder.decode(request);
        assertEquals(Write1LowTime.W1LT_10, request.response);
        request = ConfigurationWriteRequest.of(DataSampleOffsetAndWrite0RecoveryTime.DSO_AND_W0RT_8);
        request.requestState = RequestState.WAIT_FOR_RESPONSE;
        decoder.decode(request);
        assertEquals(DataSampleOffsetAndWrite0RecoveryTime.DSO_AND_W0RT_8, request.response);
        request = ConfigurationReadRequest.of(CommandType.RBR);
        request.requestState = RequestState.WAIT_FOR_RESPONSE;
        decoder.decode(request);
        assertEquals(RS232BaudRate.RBR_9_6, request.response);
        SingleBitRequest singleBitRequest = new SingleBitRequest(Speed.STANDARD, DataToSend.WRITE_0_BIT, false);
        singleBitRequest.requestState = RequestState.WAIT_FOR_RESPONSE;
        decoder.decode(singleBitRequest);
        assertEquals(Speed.STANDARD, singleBitRequest.response.speed);
        assertEquals(BitResult._1_READ_BACK, singleBitRequest.response.bitResult);
        assertEquals(DataToSend.WRITE_1_OR_READ_BIT, singleBitRequest.response.dataToSend);
        

    }
    
}
