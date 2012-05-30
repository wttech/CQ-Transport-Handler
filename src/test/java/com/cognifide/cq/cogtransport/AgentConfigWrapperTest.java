package com.cognifide.cq.cogtransport;

import org.junit.Assert;
import org.junit.Test;

import com.cognifide.cq.cogtransport.mappers.AgentConfigDomainMapper;

public class AgentConfigWrapperTest {

	@Test
	public void testGetHostnameFromURI() {
		checkHostname("flush-dispatcher-1", "http://flush-dispatcher-1/dispatcher/invalidate.cache");
		checkHostname("flush-dispatcher-1", "cog-http://flush-dispatcher-1/dispatcher/invalidate.cache");
	}

	private void checkHostname(String hostname, String url) {
		Assert.assertEquals(hostname, AgentConfigDomainMapper.getHostnameFromURI(url));
	}
}
