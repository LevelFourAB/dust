package se.l4.dust.jaxrs.internal;

import java.util.Collection;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;

import se.l4.dust.api.Namespace;
import se.l4.dust.api.discovery.DiscoveryEncounter;
import se.l4.dust.api.discovery.DiscoveryHandler;
import se.l4.dust.jaxrs.JaxrsConfiguration;

public class ProviderDiscoveryHandler
	implements DiscoveryHandler
{
	private static final Logger logger = LoggerFactory.getLogger(ProviderDiscoveryHandler.class);

	private final Injector injector;
	private final JaxrsConfiguration config;

	@Inject
	public ProviderDiscoveryHandler(Injector injector, JaxrsConfiguration config)
	{
		this.injector = injector;
		this.config = config;
	}

	@Override
	public void handle(Namespace ns, DiscoveryEncounter encounter)
	{
		Collection<Class<?>> providers = encounter.getAnnotatedWith(Provider.class);
		for(Class<?> provider : providers)
		{
			Object instance = null;
			if(MessageBodyReader.class.isAssignableFrom(provider))
			{
				if(instance == null) instance = injector.getInstance(provider);
				config.addMessageBodyReader((MessageBodyReader<?>) instance);
			}

			if(MessageBodyWriter.class.isAssignableFrom(provider))
			{
				if(instance == null) instance = injector.getInstance(provider);
				config.addMessageBodyWriter((MessageBodyWriter<?>) instance);
			}

			if(ExceptionMapper.class.isAssignableFrom(provider))
			{
				if(instance == null) instance = injector.getInstance(provider);
				config.addExceptionMapper((ExceptionMapper<?>) instance);
			}

			if(ParamConverterProvider.class.isAssignableFrom(provider))
			{
				if(instance == null) instance = injector.getInstance(provider);
				config.addParamConverterProvider((ParamConverterProvider) instance);
			}
		}

		logger.debug("{}: Found {} providers", ns.getUri(), providers.size());
	}

}
