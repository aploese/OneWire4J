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
public class ConfigurationReadRequest<R>  extends ConfigurationRequest<R>{

    public static ConfigurationReadRequest<?> of(CommandType commandTypes) {
        final ConfigurationReadRequest<?> result = new ConfigurationReadRequest<>();
        result.commandType = commandTypes;
        return result;
    }
    
    public CommandType commandType;
 
}
