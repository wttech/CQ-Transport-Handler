package com.cognifide.cq.cogtransport;

import com.day.cq.replication.AgentConfig;
import com.day.cq.replication.TransportContext;

/**
 * TransportContext implementation which uses AgentConfigWrapper
 * 
 * @author tomasz.rekawek
 * 
 */
public class TransportContextWrapper implements TransportContext {
	private TransportContext ctx;

	private AgentConfig config;

	public TransportContextWrapper(TransportContext ctx, String domain, String mappedPath) {
		this.ctx = ctx;
		this.config = new AgentConfigWrapper(ctx.getConfig(), domain, mappedPath);
	}

	@Override
	public Discardable getAttribute(String name) {
		return ctx.getAttribute(name);
	}

	@Override
	public AgentConfig getConfig() {
		return config;
	}

	@Override
	public String getName() {
		return ctx.getName();
	}

	@Override
	public Discardable setAttribute(String name, Discardable value) {
		return ctx.setAttribute(name, value);
	}

}
