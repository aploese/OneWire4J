package de.ibapl.esh.onewire4j.internal;

/*-
 * #%L
 * OneWire4J Binding
 * %%
 * Copyright (C) 2017 - 2018 Arne Plöse
 * %%
 * Eclipse Smarthome Features (https://www.eclipse.org/smarthome/) and bundles see https://github.com/aploese/esh-ibapl/
 * Copyright (C) 2017 - 2018, Arne Pl\u00f6se and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *  
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import de.ibapl.spsw.api.SerialPortSocketFactory;

/**
 *
 * @author aploese@gmx.de - Initial contribution
 */
@Component(service = ConfigDescriptionProvider.class, immediate = true, configurationPid = "config.rs232-bridge")
public class RS232BridgeConfigurationProvider implements ConfigDescriptionProvider {

    @Reference
    private SerialPortSocketFactory serialPortSocketFactory; // = new de.ibapl.spsw.jniprovider.SerialPortSocketFactoryImpl();

    private static final URI RS_232_URI;
    static {
        try {
            RS_232_URI = new URI("bridge-type:rs-232");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<ConfigDescription> getConfigDescriptions(Locale locale) {
        return Collections.singleton(getConfigDescription(RS_232_URI, locale));
    }

    @Override
    public ConfigDescription getConfigDescription(URI uri, Locale locale) {
        final List<ConfigDescriptionParameter> parameters = new LinkedList<>();
        if (RS_232_URI.equals(uri)) {
            ConfigDescriptionParameter refreshrate = new ConfigDescriptionParameter("refreshrate", Type.INTEGER) {

                @Override
                public String getDefault() {
                    return "60";
                }

                @Override
                public boolean isRequired() {
                    return true;
                }

            };
            parameters.add(refreshrate);
            ConfigDescriptionParameter port = new ConfigDescriptionParameter("port", Type.TEXT) {

                @Override
                public List<ParameterOption> getOptions() {
                    List<ParameterOption> result = new LinkedList<>();
                    // TODO Filter used ports or not ???
                    for (String name : serialPortSocketFactory.getPortNames(true)) {
                        result.add(new ParameterOption(name, name));
                    }
                    return result;
                }

                @Override
                public boolean isRequired() {
                    return true;
                }

            };
            parameters.add(port);
        }
        return new ConfigDescription(uri, parameters);
    }

}
