Cognifide Transport Handler
===========================

It's often desirable to use separate dispatcher directories to separate sites. Dispatcher directory `/docroot` can be assigned to the given virtualhost with `/virtualhosts` directive. It can be then specified in Apache virtualhost configuration with `DocumentRoot`. However, there is one problem: flush replication agent on publish which is responsible for refreshing replicated contents. Standard flush agent doesn't know anything about domain — all it's got is content path, so all invalidate request go to the same dispatcher.

Of course you can configure one Flush Agent per domain, but if you have many domains it can be complicated. What's more, sometimes you might want to use more than one dispatcher per domain, so you have to configure NxM flush agents, where N is domain count, and M is dispatcher count.

Cognifide Transport Handler is an solution to that problem.

How does it work?
---------------------

Flush agent uses so-called Transport Handler to connect to the given Transport URI. Transport Handler is an interface, implemented by a few OSGi services. Implementations are assigned to URI scheme (`http://`, `https://`, `zip://`, etc.) Cognifide Transport Handler works exactly like original HTTP transport handler (in fact it wraps HTTP transport handler) with one difference: it allows to use `{domain}` pattern in HTTP headers configuration. Cognifide Transport Handler has it's own URI scheme: `cog-http://` and `cog-https://`.

cog-transport is also able to use mapped (short) paths instead of the longer. Just use `{mappedPath}` placeholder.

How the domain is assigned to the path?
-------------------------------------------

There are 4 chained methods. If one method returns correct result the rest will be skipped.

1. Use `ResourceResolver.map(...)` method to get domain name for a given content path. It's necessary to have appropriate `/etc/map.publish/http` entries. You can check your configuration in *Apache Felix Web Console* (*Sling Resource Resolver*, section *Mapping Map Entries* at the bottom).
2. Use *Fallback mappings* from the `com.cognifide.cq.cogtransport.TransportWrapper` OSGi configuration. You can specify a list of mappings and each mapping consists of domain and path separated by one space. Eg: `mysite.com /content/mysite/`
3. Use *Default domain* from the `com.cognifide.cq.cogtransport.TransportWrapper` OSGi configuration.
4. Use hostname from the *Transport URI* configured in the *Dispatcher Flush*.

How to configure it?
--------------------

Create dispatcher config (with separate `/docroot`) for each domain. Create Apache virtualhosts (probably you already have these) and add appropriate `DocumentRoot`. Configure Flush Agent using `cog-http://` scheme, add `host:{domain}` HTTP header and change `{path}` to `{mappedPath}`. That's all.

You can also set *Fallback mappings* and *Default domain* in the OSGi configuration. Please see above.

IMPORTANT: After each change of the OSGi configuration or after updating Cognifide Transport Handler you have to edit Dispatcher Flush replication agent and save it (even without any change). Otherwise replication agent will be using old version of handler.

What about multiple dispatchers per site?
-----------------------------------------

Because virtualhost is used to recognize site, we need some different mechanism to recognize dispatcher (assuming that there are many dispatchers-per-site on one server). You can use separate port numbers for each one (eg. 80 for the first one, 81 for the second, etc.)
