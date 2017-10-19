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
public enum RS232BaudRate {
    RBR_9_6,
    RBR_19_2,
    RBR_57_6,
    RBR_115_2,
    RBR_9_6_I,
    RBR_19_2_I,
    RBR_57_6_I,
    RBR_115_2_I;
    
    public String toString() {
        switch (this) {
            case RBR_9_6: return "9600 bps";
            case RBR_19_2: return "19200 bps";
            case RBR_57_6: return "57600 bps";
            case RBR_115_2: return "115200 bps";
            case RBR_9_6_I: return "9600 bps (inverted)";
            case RBR_19_2_I: return "19200 bps (inverted)";
            case RBR_57_6_I: return "57600 bps (inverted)";
            case RBR_115_2_I: return "115200 bps (inverted)";
            default: throw new RuntimeException("cant ahndle value: " + name());
        }
    }
}
