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
public enum PullDownSlewRateParam {
    PDSRC_15(15.0f),
    PDSRC_2_2(2.2f),
    PDSRC_1_65(1.65f),
    PDSRC_1_37(1.37f),
    PDSRC_1_1(1.1f),
    PDSRC_0_83(0.83f),
    PDSRC_0_7(0.7f),
    PDSRC_0_55(0.55f);
    final float value;

    private PullDownSlewRateParam(float value) {
        this.value = value;
    }
    
    public String toString() {
        return value + " V/µs";
    }
    
}
