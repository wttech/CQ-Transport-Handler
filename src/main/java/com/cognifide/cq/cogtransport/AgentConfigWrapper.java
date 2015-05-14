package com.cognifide.cq.cogtransport;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import com.day.cq.replication.AgentConfig;

/**
 * AgentConfig implementation which replaces {domain} keyword in HTTP headers.
 * 
 * @author tomasz.rekawek
 * 
 */
public class AgentConfigWrapper implements AgentConfig {
	private static final String[] DEFAULT_HTTP_HEADERS = { "Action: {action}", "Path: {path}",
			"Handle: {path}" };

	private static final String PROTOCOL_HTTP_HEADERS = "protocolHTTPHeaders";

	private AgentConfig config;

	private String transportUri;

	private ValueMap properties;

	public AgentConfigWrapper(AgentConfig config, String domainName, String mappedPath) {
		transportUri = config.getTransportURI();
		if (transportUri.startsWith(TransportWrapper.COG_PREFIX)) {
			transportUri = transportUri.substring(TransportWrapper.COG_PREFIX.length());
		}
		this.config = config;
		prepareProperties(domainName, mappedPath);
	}

	private void prepareProperties(String domainName, String mappedPath) {
		ValueMap originalProperties = config.getProperties();
		String[] originalHeaders = originalProperties.get(PROTOCOL_HTTP_HEADERS, DEFAULT_HTTP_HEADERS);
		String[] headers = Arrays.copyOf(originalHeaders, originalHeaders.length);

		for (int i = 0; i < headers.length; i++) {
			headers[i] = headers[i].replace("{domain}", domainName);
			if(mappedPath != null) {
				headers[i] = headers[i].replace("{mappedPath}", mappedPath);
			}
		}

		Map<String, Object> propertiesMap = new HashMap<String, Object>(originalProperties);
		propertiesMap.put(PROTOCOL_HTTP_HEADERS, headers);
		this.properties = new ValueMapDecorator(propertiesMap);
	}

	@Override
	public void checkValid() {
		config.checkValid();
	}

	@Override
	public String getAgentUserId() {
		return config.getAgentUserId();
	}

	@Override
	public String getConfigPath() {
		return config.getConfigPath();
	}

	@Override
	public String getLogLevel() {
		return config.getLogLevel();
	}

	@Override
	public int getMaxRetries() {
		return config.getMaxRetries();
	}

	@Override
	public String getName() {
		return config.getName();
	}

	@Override
	public ValueMap getProperties() {
		return properties;
	}

	@Override
	public long getRetryDelay() {
		return config.getRetryDelay();
	}

	@Override
	public String getSerializationType() {
		return config.getSerializationType();
	}

	@Override
	public String getTransportPassword() {
		return config.getTransportPassword();
	}

	@Override
	public String getTransportURI() {
		return transportUri;
	}

	@Override
	public String getTransportUser() {
		return config.getTransportUser();
	}

	@Override
	public boolean isEnabled() {
		return config.isEnabled();
	}

	@Override
	public boolean isSpecific() {
		return config.isSpecific();
	}

	@Override
	public boolean isTriggeredOnDistribute() {
		return config.isTriggeredOnDistribute();
	}

	@Override
	public boolean isTriggeredOnModification() {
		return config.isTriggeredOnModification();
	}

	@Override
	public boolean isTriggeredOnOffTime() {
		return config.isTriggeredOnOffTime();
	}

	@Override
	public boolean noStatusUpdate() {
		return config.noStatusUpdate();
	}

	@Override
	public boolean noVersions() {
		return config.noVersions();
	}

	@Override
	public boolean usedForReverseReplication() {
		return config.usedForReverseReplication();
	}

}
