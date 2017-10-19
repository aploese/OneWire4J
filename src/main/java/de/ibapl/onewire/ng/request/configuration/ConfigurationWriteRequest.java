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
public class ConfigurationWriteRequest<R>  extends ConfigurationRequest<R>{

    public static ConfigurationWriteRequest<PullDownSlewRateParam> of (PullDownSlewRateParam pullDownSlewRateParam) {
        final ConfigurationWriteRequest<PullDownSlewRateParam> result = new ConfigurationWriteRequest<>();
        result.commandType = CommandType.PDSRC;
        result.propertyValue = pullDownSlewRateParam;
        return result;
    }

    public static ConfigurationWriteRequest<ProgrammingPulseDuration> of (ProgrammingPulseDuration programmingPulseDuration) {
        final ConfigurationWriteRequest<ProgrammingPulseDuration> result = new ConfigurationWriteRequest<>();
        result.commandType =CommandType.PPD;
        result.propertyValue = programmingPulseDuration;
        return result;
    }
    
    public static ConfigurationWriteRequest<StrongPullupDuration> of (StrongPullupDuration strongPullupDuration) {
        final ConfigurationWriteRequest<StrongPullupDuration> result = new ConfigurationWriteRequest<>();
        result.commandType =CommandType.SPUD;
        result.propertyValue = strongPullupDuration;
        return result;
    }
    
    public static ConfigurationWriteRequest<Write1LowTime> of(Write1LowTime write1LowTime) {
        final ConfigurationWriteRequest<Write1LowTime> result = new ConfigurationWriteRequest<>();
        result.commandType = CommandType.W1LT;
        result.propertyValue = write1LowTime;
        return result;
    }

    public static ConfigurationWriteRequest<DataSampleOffsetAndWrite0RecoveryTime> of(DataSampleOffsetAndWrite0RecoveryTime dataSampleOffsetAndWrite0RecoveryTime) {
        final ConfigurationWriteRequest<DataSampleOffsetAndWrite0RecoveryTime> result = new ConfigurationWriteRequest<>();
        result.commandType = CommandType.DSO_AND_W0RT;
        result.propertyValue = dataSampleOffsetAndWrite0RecoveryTime;
        return result;
    }
    
    public static ConfigurationWriteRequest<LoadSensorThreshold> of(LoadSensorThreshold loadSensorThreshold) {
        final ConfigurationWriteRequest<LoadSensorThreshold> result = new ConfigurationWriteRequest<>();
        result.commandType = CommandType.LST;
        result.propertyValue = loadSensorThreshold;
        return result;
    }

    public static ConfigurationWriteRequest<RS232BaudRate> of(RS232BaudRate rS232BaudRate) {
        final ConfigurationWriteRequest<RS232BaudRate> result = new ConfigurationWriteRequest<>();
        result.commandType = CommandType.RBR;
        result.propertyValue = rS232BaudRate;
        return result;
    }

    public CommandType commandType;
    public R propertyValue;

}
