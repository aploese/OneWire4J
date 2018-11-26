/*-
 * #%L
 * OneWire4J
 * %%
 * Copyright (C) 2017 - 2018 Arne Plöse
 * %%
 * OneWire4J - Drivers for the 1-wire protocol https://github.com/aploese/OneWire4J/
 * Copyright (C) 2009, 2017, Arne Plöse and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 * #L%
 */
package de.ibapl.onewire4j.request.configuration;

/**
 *
 * @author Arne Plöse
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

    public static ConfigurationWriteRequest<SerialPortSpeed> of(SerialPortSpeed rS232BaudRate) {
        final ConfigurationWriteRequest<SerialPortSpeed> result = new ConfigurationWriteRequest<>();
        result.commandType = CommandType.RBR;
        result.propertyValue = rS232BaudRate;
        return result;
    }

    public CommandType commandType;
    public R propertyValue;

    @Override
    public int responseSize(StrongPullupDuration spd) {
        return 1;
    }

}
