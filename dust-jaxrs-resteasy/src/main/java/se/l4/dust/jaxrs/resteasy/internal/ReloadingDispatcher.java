package se.l4.dust.jaxrs.resteasy.internal;

import javax.ws.rs.NotFoundException;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.core.ResourceInvoker;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.dust.jaxrs.internal.PageDiscovery;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.Stage;

/**
 * Custom {@link Dispatcher} that tries to reload pages in development mode.
 * 
 * @author Andreas Holstenson
 *
 */
@Singleton
public class ReloadingDispatcher
	extends SynchronousDispatcher
{
	private static final Logger logger = LoggerFactory.getLogger(ResteasyFilter.class);
	
	private final boolean development;
	private final PageDiscovery discovery;

	@Inject
	public ReloadingDispatcher(Stage stage, 
			ResteasyProviderFactory providerFactory,
			PageDiscovery discovery)
	{
		super(providerFactory);
		this.discovery = discovery;
	
		development = stage != Stage.PRODUCTION;
	}

	@Override
	public void invokePropagateNotFound(HttpRequest request, HttpResponse response)
		throws NotFoundException 
	{
		invoke(request, response, true);
	}
	
	public void invoke(HttpRequest request, HttpResponse response, boolean first)
		throws NotFoundException
	{
		try
		{
			pushContextObjects(request, response);
			if (!preprocess(request, response)) return;
			ResourceInvoker invoker = getInvoker(request);
			invoke(request, response, invoker);
		}
		catch(NotFoundException e)
		{
			if(first && development)
			{
				/*
				 * If we are the first attempt and nothing can be found
				 * try to reindex all namespaces. 
				 */
				try
				{
					discovery.reindexAndDiscover();
				}
				catch(Exception e2)
				{
					logger.error("Unable to reload; " + e2.getMessage(), e2);
				}
				
				// Invoke again
				invoke(request, response, false);
			}
			else
			{
				throw e;
			}
		}
		catch(Exception e)
		{
			writeException(request, response, e);
			return;
		}
		finally
		{
			clearContextData();
		}
	}
}
