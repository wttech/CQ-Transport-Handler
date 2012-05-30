package com.cognifide.cq.cogtransport.mappers;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognifide.cq.cogtransport.Mapper;
import com.day.cq.replication.AgentConfig;

/**
 * Map path to domain using resource resolver.
 * 
 * @author tomasz.rekawek
 * 
 */
public class ResourceResolverMapper implements Mapper {
	private static final Logger LOG = LoggerFactory.getLogger(ResourceResolverMapper.class);

	private ResourceResolver resourceResolver;

	public ResourceResolverMapper(ResourceResolver resourceResolver) {
		this.resourceResolver = resourceResolver;
	}

	@Override
	public String map(String path, AgentConfig agentConfig) {
		String domain = null;
		String mappedUri = resourceResolver.map(path);
		if (mappedUri == null) {
			LOG.error("Can't map " + path + " to URI");
		} else {
			try {
				URI uri = new URI(mappedUri);
				domain = uri.getHost();
			} catch (URISyntaxException e) {
				domain = null;
			}
		}
		return domain;
	}
}
