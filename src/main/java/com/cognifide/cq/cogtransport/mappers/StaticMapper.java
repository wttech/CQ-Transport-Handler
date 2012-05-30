package com.cognifide.cq.cogtransport.mappers;

import java.util.ArrayList;
import java.util.List;

import com.cognifide.cq.cogtransport.Mapper;
import com.day.cq.replication.AgentConfig;

/**
 * Map content to domain using static entries
 * 
 * @author tomasz.rekawek
 *
 */
public class StaticMapper implements Mapper {
	private List<Mapping> mappings;

	/**
	 * Configuration lines consist of domain and path separated by one space.
	 * 
	 * @param configuration
	 */
	public StaticMapper(String[] configuration) {
		mappings = new ArrayList<Mapping>(configuration.length);
		for (String line : configuration) {
			String[] split = line.split(" ");
			if (split.length == 2) {
				mappings.add(new Mapping(split[0], split[1]));
			}
		}
	}

	@Override
	public String map(String path, AgentConfig agentConfig) {
		for (Mapping map : mappings) {
			if (path.startsWith(map.getPath())) {
				return map.getDomain();
			}
		}
		return null;
	}

	private static class Mapping {
		private final String domain;

		private final String path;

		public Mapping(String domain, String path) {
			this.domain = domain;
			this.path = path;
		}

		public String getDomain() {
			return domain;
		}

		public String getPath() {
			return path;
		}
	}
}
