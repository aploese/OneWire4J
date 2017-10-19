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
public enum StrongPullupDuration {
    SPUD_16_4,
    SPUD_65_5,
    SPUD_131,
    SPUD_262,
    SPUD_524,
    SPUD_1048,
    SPUD_DYN,
    SPUD_POSITIVE_INFINITY;


    public String toString() {
        switch (this) {
            case SPUD_16_4: return 16.4 + " µs";
            case SPUD_65_5: return 65.5 + " µs";
            case SPUD_131: return "131 µs";
            case SPUD_262: return "262 µs";
            case SPUD_1048: return "1048 µs";
            case SPUD_DYN: return "dyn.";
            case SPUD_POSITIVE_INFINITY: return "∞";
            default: throw new RuntimeException("can't handle: " + name());
        }
    }
}
