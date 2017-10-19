/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ibapl.onewire.ng.request.configuration;

/**
 *
 * @author aploese
 */
public enum DataSampleOffsetAndWrite0RecoveryTime {
    DSO_AND_W0RT_3(3),
    DSO_AND_W0RT_4(4),
    DSO_AND_W0RT_5(5),
    DSO_AND_W0RT_6(6),
    DSO_AND_W0RT_7(7),
    DSO_AND_W0RT_8(8),
    DSO_AND_W0RT_9(9),
    DSO_AND_W0RT_10(10);
    public final byte value;
    
    private DataSampleOffsetAndWrite0RecoveryTime(int value) {
        this.value = (byte) value;
    }
    
    public String toString() {
        return value + " µs";
    }
    
}
