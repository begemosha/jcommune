/**
 * Copyright (C) 2011  JTalks.org Team
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.jtalks.jcommune.plugin.registration.poulpe;

import org.jtalks.jcommune.model.entity.PluginConfiguration;
import org.jtalks.jcommune.model.entity.PluginConfigurationProperty;

import org.jtalks.jcommune.model.plugins.SimpleRegistrationPlugin;
import org.jtalks.jcommune.model.plugins.StatefullPlugin;
import org.jtalks.jcommune.model.plugins.exceptions.NoConnectionException;
import org.jtalks.jcommune.model.plugins.exceptions.UnexpectedErrorException;
import org.jtalks.jcommune.plugin.registration.poulpe.service.PoulpeRegistrationService;
import org.jtalks.jcommune.plugin.registration.poulpe.service.RegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.*;

/**
 * Provides user registration service via Poulpe.
 *
 * @author Andrey Pogorelov
 */
public class PoulpeRegistrationPlugin extends StatefullPlugin implements SimpleRegistrationPlugin {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private RegistrationService service;
    private PluginConfiguration pluginConfiguration;
    private State state;

    @Override
    public Map<String, String> registerUser(String username, String password, String email)
            throws NoConnectionException, UnexpectedErrorException {
        try {
            return service.registerUser(username, password, email);
        } catch (IOException | JAXBException e) {
            logger.error("Parse response error", e);
            throw new UnexpectedErrorException(e);
        } catch (NoConnectionException e) {
            logger.error("Can't connect to Poulpe", e);
            throw e;
        }
    }

    @Override
    public boolean supportsJCommuneVersion(String version) {
        return true;
    }

    @Override
    public String getName() {
        return "Poulpe registration plugin";
    }

    @Override
    public List<PluginConfigurationProperty> getConfiguration() {
        return pluginConfiguration.getProperties();
    }

    @Override
    public List<PluginConfigurationProperty> getDefaultConfiguration() {
        PluginConfigurationProperty url =
                new PluginConfigurationProperty(PluginConfigurationProperty.Type.STRING, "http://localhost/rest/private/user");
        url.setName("PoulpeUrl");
        PluginConfigurationProperty login = new PluginConfigurationProperty(PluginConfigurationProperty.Type.STRING, "user");
        login.setName("Login");
        PluginConfigurationProperty password = new PluginConfigurationProperty(PluginConfigurationProperty.Type.STRING, "1234");
        login.setName("Password");
        return Arrays.asList(url, login, password);
    }

    @Override
    public void configure(PluginConfiguration pluginConfiguration) {
        this.pluginConfiguration = pluginConfiguration;
        loadConfiguration();
    }

    @Override
    protected Map<PluginConfigurationProperty, String> applyConfiguration(List<PluginConfigurationProperty> properties) {
        this.pluginConfiguration.setProperties(properties);
        return Collections.emptyMap();
    }

    @Override
    public State getState() {
        return state;
    }

    private void loadConfiguration() {
        String url = null;
        String login = null;
        String password = null;
        for (PluginConfigurationProperty property : pluginConfiguration.getProperties()) {
            if (property.getName().equalsIgnoreCase("PoulpeUrl")) {
                url = property.getValue();
            } else if (property.getName().equalsIgnoreCase("Login")) {
                login = property.getValue();
            } else if (property.getName().equalsIgnoreCase("Password")) {
                password = property.getValue();
            }
        }
        if(url != null && login != null && password != null) {
            service = new PoulpeRegistrationService(url, login, password);
            state = State.ENABLED;
        } else {
            state = State.IN_ERROR;
        }
    }
}