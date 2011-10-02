package se.l4.dust.core;

import se.l4.crayon.Contributions;
import se.l4.crayon.CrayonModule;
import se.l4.crayon.annotation.Contribution;
import se.l4.dust.api.Context;
import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.Scopes;
import se.l4.dust.api.annotation.ContextScoped;
import se.l4.dust.api.annotation.NamespaceBinding;
import se.l4.dust.core.internal.InternalContributions;
import se.l4.dust.core.internal.NamespaceManagerImpl;
import se.l4.dust.core.internal.asset.AssetModule;
import se.l4.dust.core.internal.conversion.ConversionModule;
import se.l4.dust.core.internal.discovery.DiscoveryModule;
import se.l4.dust.core.internal.expression.ExpressionModule;
import se.l4.dust.core.internal.resource.ResourceModule;
import se.l4.dust.core.internal.template.TemplateModule;

import com.google.inject.Provider;

/**
 * Module containing core functionality.
 * 
 * @author Andreas Holstenson
 *
 */
public class CoreModule
	extends CrayonModule
{

	@Override
	protected void configure()
	{
		install(new ResourceModule());
		install(new AssetModule());
		install(new TemplateModule());
		install(new ConversionModule());
		install(new DiscoveryModule());
		install(new ExpressionModule());
		
		bind(NamespaceManager.class).to(NamespaceManagerImpl.class);
		bindContributions(NamespaceBinding.class);
		
		bindScope(ContextScoped.class, Scopes.CONTEXT);
		
		bind(Context.class).toProvider(new Provider<Context>()
		{
			@Override
			public Context get()
			{
				return Scopes.getActiveContext();
			}
		});
	}

	@Contribution(name="dust-namespaces")
	public void bindNamespaces(@NamespaceBinding Contributions contributions)
	{
		InternalContributions.add(contributions);
		
		contributions.run();
	}
}
