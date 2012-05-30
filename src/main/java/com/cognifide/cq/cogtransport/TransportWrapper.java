package com.cognifide.cq.cogtransport;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cognifide.cq.cogtransport.mappers.AgentConfigDomainMapper;
import com.cognifide.cq.cogtransport.mappers.ConstantMapper;
import com.cognifide.cq.cogtransport.mappers.ResourceResolverMapper;
import com.cognifide.cq.cogtransport.mappers.StaticMapper;
import com.day.cq.replication.AgentConfig;
import com.day.cq.replication.ReplicationContent;
import com.day.cq.replication.ReplicationContentFactory;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.ReplicationResult;
import com.day.cq.replication.ReplicationTransaction;
import com.day.cq.replication.TransportContext;
import com.day.cq.replication.TransportHandler;

/**
 * TransportHandler which handles cog-http[s]:// URIs. The {domain} keyword in http headers will be replaced
 * with domain mapped from resource path.
 * 
 * @author tomasz.rekawek
 * 
 */
@Component(immediate = true, metatype = true)
@Service
public class TransportWrapper implements TransportHandler {
	private static final Logger LOG = LoggerFactory.getLogger(TransportWrapper.class);

	@Property(label = "Fallback mappings", description = "Use this mappings if resource resolver doesn't return anything. Separate domain and path name using space", value = {
			"", "" })
	private static final String PROPERTY_FALLBACK_MAPPINGS = "fallbackMappings";

	private static final String[] DEFAULT_FALLBACK_MAPPINGS = new String[] { "", "" };

	@Property(label = "Default domain", description = "Default domain used if there is some mapping problem", value = TransportWrapper.DEFAULT_DOMAIN)
	private static final String PROPERTY_DEFAULT_DOMAIN = "defaultDomain";

	private static final String DEFAULT_DOMAIN = "localhost";

	static final String COG_PREFIX = "cog-";

	private static final String COG_HTTP = COG_PREFIX + "http://";

	private static final String COG_HTTPS = COG_PREFIX + "https://";

	@Reference
	private ResourceResolverFactory resolverFactory;
	
	private ResourceResolver resourceResolver;

	@Reference(referenceInterface = TransportHandler.class, policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MANDATORY_MULTIPLE)
	private Set<TransportHandler> transportHandlers = new HashSet<TransportHandler>();
	
	// If TransportWrapper configuration has changed then you should refresh configuration of related replication agents. Until that
	// replication agent uses deactivated TransportWrapper, with empty set of transport handlers. In order to avoid errors, this
	// is a cached version of this set which will be used in this case.
	private Set<TransportHandler> transportHandlersCache;

	private Mapper[] mappers;

	@Activate
	protected void activate(ComponentContext context) {
		try {
			this.resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
		} catch(LoginException e) {
			LOG.error("Can't get resource resolver", e);
		}
		
		String[] fallbackMappings = OsgiUtil.toStringArray(context.getProperties().get(PROPERTY_FALLBACK_MAPPINGS));
		if (fallbackMappings == null) {
			fallbackMappings = DEFAULT_FALLBACK_MAPPINGS;
		}
		String defaultDomain = OsgiUtil.toString(context.getProperties().get(PROPERTY_DEFAULT_DOMAIN),
				DEFAULT_DOMAIN);
		mappers = new Mapper[] { new ResourceResolverMapper(resourceResolver),
				new StaticMapper(fallbackMappings), new ConstantMapper(defaultDomain),
				new AgentConfigDomainMapper() };
		LOG.info("Component activated");
	}

	@Deactivate
	protected void deactivate(ComponentContext context) {
		LOG.info("Component deactivated");
		transportHandlersCache = new HashSet<TransportHandler>(transportHandlers);
		if(resourceResolver != null && resourceResolver.isLive()) {
			resourceResolver.close();
		}
	}

	public boolean canHandle(AgentConfig config) {
		String uri = config == null ? null : config.getTransportURI();
		return (uri != null) && ((uri.startsWith(COG_HTTP)) || (uri.startsWith(COG_HTTPS)));
	}

	@Override
	public ReplicationResult deliver(TransportContext ctx, ReplicationTransaction tx)
			throws ReplicationException {
		String domain = getDomain(tx.getAction().getPath(), ctx.getConfig());
		String mappedPath = getMappedPath(tx.getAction().getPath());
		TransportContext wrappedCtx = new TransportContextWrapper(ctx, domain, mappedPath);

		TransportHandler handler = getTransportHandler(wrappedCtx.getConfig());
		return handler.deliver(wrappedCtx, tx);
	}

	@Override
	public ReplicationResult poll(TransportContext ctx, ReplicationTransaction tx,
			List<ReplicationContent> result, ReplicationContentFactory factory) throws ReplicationException {
		String domain = getDomain(tx.getAction().getPath(), ctx.getConfig());
		String mappedPath = getMappedPath(tx.getAction().getPath());
		TransportContext wrappedCtx = new TransportContextWrapper(ctx, domain, mappedPath);

		TransportHandler handler = getTransportHandler(wrappedCtx.getConfig());
		return handler.poll(wrappedCtx, tx, result, factory);
	}

	/**
	 * Man content path to domain using created mappers
	 * 
	 * @param path Path to map
	 * @return Domain name
	 */
	private String getDomain(String path, AgentConfig agentConfig) {
		for (Mapper mapper : mappers) {
			String domain = mapper.map(path, agentConfig);
			if (StringUtils.isNotBlank(domain)) {
				LOG.info(path + "->" + domain + "(" + mapper.getClass().getSimpleName() + ")");
				return domain;
			}
		}
		LOG.error("Can't map " + path + " to domain");
		return null;
	}
	
	private String getMappedPath(String path) {
		String resolverResult = resourceResolver.map(path);
		String mappedPath;
		try {
			mappedPath = new URI(resolverResult).getPath();
		} catch (URISyntaxException e) {
			mappedPath = resolverResult;
		}
		
		return mappedPath;		
	}

	/**
	 * Get transport handler for given AgentConfig (to forward deliver and poll methods)
	 * 
	 * @param config Agent configuration
	 * @return TransportHandler
	 * @throws ReplicationException If there is no TransportHandler for given config.
	 */
	private TransportHandler getTransportHandler(AgentConfig config) throws ReplicationException {
		Set<TransportHandler> handlers;
		if(transportHandlers.isEmpty() && transportHandlersCache != null) {
			handlers = transportHandlersCache;
		} else {
			handlers = transportHandlers;
		}
		
		for(TransportHandler handler : handlers) {
			if(handler.canHandle(config)) {
				return handler;
			}
		}
		throw new ReplicationException("Can't get TransportHandler for " + config.getTransportURI());
	}
	
	protected void bindTransportHandlers(TransportHandler transportHandler) {
		if(transportHandler != this) {
			transportHandlers.add(transportHandler);
		}
	}
	
	protected void unbindTransportHandlers(TransportHandler transportHandler) {
		transportHandlers.remove(transportHandler);
	}
}
