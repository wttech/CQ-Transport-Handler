package com.cognifide.cq.cogtransport.mappers;

import com.cognifide.cq.cogtransport.Mapper;
import com.day.cq.replication.AgentConfig;

/**
 * Map all paths to one, constant domain.
 * 
 * @author tomasz.rekawek
 *
 */
public class ConstantMapper implements Mapper {
	private String domain;

	public ConstantMapper(String domain) {
		this.domain = domain;
	}

	@Override
	public String map(String path, AgentConfig agentConfig) {
		return domain;
	}

}
