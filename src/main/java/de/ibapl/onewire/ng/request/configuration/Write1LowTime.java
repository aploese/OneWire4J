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
public enum Write1LowTime {
    W1LT_8(8),
    W1LT_9(9),
    W1LT_10(10),
    W1LT_11(11),
    W1LT_12(12),
    W1LT_13(13),
    W1LT_14(14),
    W1LT_15(15);
    public final byte value;
    
    private Write1LowTime(final int value) {
        this.value = (byte)value;
    }
    
    public String toString() {
        return value + " µs";
    }
    
}
