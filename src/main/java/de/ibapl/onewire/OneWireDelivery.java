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
public enum OneWireDelivery {
       /** Duration used in delivering power to the 1-Wire, 1/2 second         */
   HALF_SECOND, // = 0;
   /** Duration used in delivering power to the 1-Wire, 1 second           */
   ONE_SECOND, // = 1;
   /** Duration used in delivering power to the 1-Wire, 2 seconds          */
   TWO_SECONDS, // = 2;
   /** Duration used in delivering power to the 1-Wire, 4 second           */
   FOUR_SECONDS, // = 3;
   /** Duration used in delivering power to the 1-Wire, smart complete     */
   SMART_DONE, // = 4;
   /** Duration used in delivering power to the 1-Wire, infinite           */
   INFINITE, // = 5;
   /** Duration used in delivering power to the 1-Wire, current detect     */
   CURRENT_DETECT, // = 6;
   /** Duration used in delivering power to the 1-Wire, 480 us             */
   EPROM; // = 7;
}
