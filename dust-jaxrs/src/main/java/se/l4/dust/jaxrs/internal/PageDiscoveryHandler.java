package se.l4.dust.jaxrs.internal;

import java.util.Collection;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.dust.api.Namespace;
import se.l4.dust.api.discovery.DiscoveryEncounter;
import se.l4.dust.api.discovery.DiscoveryHandler;
import se.l4.dust.jaxrs.JaxrsConfiguration;

import com.google.inject.Inject;

public class PageDiscoveryHandler
	implements DiscoveryHandler
{
	private static final Logger logger = LoggerFactory.getLogger(PageDiscoveryHandler.class);

	private final JaxrsConfiguration config;

	@Inject
	public PageDiscoveryHandler(JaxrsConfiguration config)
	{
		this.config = config;
	}

	@Override
	public void handle(Namespace ns, DiscoveryEncounter encounter)
	{
		Collection<Class<?>> pages = encounter.getAnnotatedWith(Path.class);
		for(Class<?> page : pages)
		{
			config.addPage(page);
		}

		logger.debug("{}: Found {} pages", ns.getUri(), pages.size());
	}

}
