package com.cognifide.cq.cogtransport.mappers;

import java.net.URI;

import com.cognifide.cq.cogtransport.Mapper;
import com.day.cq.replication.AgentConfig;

/**
 * Returns agent config transport URI domain
 * 
 * @author tomasz.rekawek
 *
 */
public class AgentConfigDomainMapper implements Mapper {
	@Override
	public String map(String path, AgentConfig agentConfig) {
		String uri = agentConfig.getTransportURI();
		return getHostnameFromURI(uri);
	}

	public static String getHostnameFromURI(String uri) {
		return URI.create(uri).getHost();
	}
}
