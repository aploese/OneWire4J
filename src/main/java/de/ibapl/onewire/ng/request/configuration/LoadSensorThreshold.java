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
public enum LoadSensorThreshold {
    LST_1_8(1.8f),
    LST_2_1(2.1f),
    LST_2_4(2.4f),
    LST_2_7(2.7f),
    LST_3_0(3.0f),
    LST_3_3(3.3f),
    LST_3_6(3.6f),
    LST_3_9(3.9f);
    public final float value;
    
    private LoadSensorThreshold(float value) {
        this.value = value;
    }
    
    public String toString() {
        return value + " mA";
    }
}
