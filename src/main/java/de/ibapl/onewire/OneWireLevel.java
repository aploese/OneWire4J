/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ibapl.onewire;

/**
 *
 * @author aploese
 */
public enum OneWireLevel {
   /** 1-Wire Network level, normal (weak 5Volt pullup)                            */
   NORMAL, // = (char)0;
   /** 1-Wire Network level, (strong 5Volt pullup, used for power delivery) */
   POWER_DELIVERY, // = (char)1;
   /** 1-Wire Network level, (strong pulldown to 0Volts, reset 1-Wire)      */
   BREAK,//  = (char)2;
   /** 1-Wire Network level, (strong 12Volt pullup, used to program eprom ) */
   PROGRAM; // = (char)3;

}
