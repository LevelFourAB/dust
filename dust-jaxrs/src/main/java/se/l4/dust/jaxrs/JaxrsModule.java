package se.l4.dust.jaxrs;

import se.l4.crayon.CrayonModule;
import se.l4.crayon.annotation.Contribution;
import se.l4.crayon.annotation.Order;
import se.l4.dust.api.discovery.NamespaceDiscovery;
import se.l4.dust.jaxrs.internal.ConversionParamProvider;
import se.l4.dust.jaxrs.internal.PageDiscoveryHandler;
import se.l4.dust.jaxrs.internal.ProviderDiscoveryHandler;
import se.l4.dust.jaxrs.internal.asset.AssetProvider;
import se.l4.dust.jaxrs.internal.asset.AssetWriter;
import se.l4.dust.jaxrs.internal.template.TemplateWriter;
import se.l4.dust.servlet.WebModule;

/**
 * Module defining shared web bindings.
 * 
 * @author andreas
 *
 */
public class JaxrsModule
	extends CrayonModule
{
	@Override
	protected void configure()
	{
		install(new WebModule());
	}
	
	@Contribution(name="dust-asset-page")
	public void contributeAssetPage(JaxrsConfiguration config)
	{
		config.addPage(AssetProvider.class);
	}
	
	@Contribution(name="dust-default-message-providers")
	public void contributeDefaultMessageProviders(JaxrsConfiguration config,
			AssetWriter w1,
			TemplateWriter w2)
	{
		config.addMessageBodyWriter(w1);
		config.addMessageBodyWriter(w2);
	}
	
	@Contribution(name="dust-default-param-converter")
	@Order("after:dust-default-message-providers")
	public void contributeDefaultParamConverter(JaxrsConfiguration config, ConversionParamProvider provider)
	{
		config.addParamConverterProvider(provider);
	}
	
	@Contribution(name="dust-discovery-pages")
	@Order("after:dust-discovery-providers")
	public void contributePageDiscovery(NamespaceDiscovery discovery,
			PageDiscoveryHandler handler)
	{
		discovery.addHandler(handler);
	}
	
	@Contribution(name="dust-discovery-providers")
	@Order("after:dust-discovery-template-preloading")
	public void contributeProviderDiscovery(NamespaceDiscovery discovery,
			ProviderDiscoveryHandler handler)
	{
		discovery.addHandler(handler);
	}
}
