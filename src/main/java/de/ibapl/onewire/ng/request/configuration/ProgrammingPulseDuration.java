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
public enum ProgrammingPulseDuration {
    PPD_32(32),
    PPD_64(64),
    PPD_128(128),
    PPD_256(256),
    PPD_512(512),
    PPD_1024(1024),
    PPD_2048(2048),
    PPD_POSITIVE_INFINITY(Float.POSITIVE_INFINITY);
    public final float value;

    private ProgrammingPulseDuration(float value) {
        this.value = value;
    }
    
    public String toString() {
        return value + " µs";
    }
}
