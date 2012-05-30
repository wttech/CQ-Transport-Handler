package com.cognifide.cq.cogtransport;

import com.day.cq.replication.AgentConfig;

/**
 * Interface which can map domain to the content path
 * 
 * @author tomasz.rekawek
 *
 */
public interface Mapper {
	/**
	 * Get content path and return domain name
	 * 
	 * @param path
	 * @return
	 */
	String map(String path, AgentConfig config);
}
