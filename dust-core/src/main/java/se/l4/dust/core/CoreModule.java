package se.l4.dust.core;

import com.google.inject.Provider;
import com.google.inject.name.Named;

import se.l4.crayon.Contribution;
import se.l4.crayon.Contributions;
import se.l4.crayon.CrayonModule;
import se.l4.dust.api.Context;
import se.l4.dust.api.ContextScoped;
import se.l4.dust.api.NamespaceBinding;
import se.l4.dust.api.Namespaces;
import se.l4.dust.api.Scopes;
import se.l4.dust.api.discovery.NamespaceDiscovery;
import se.l4.dust.core.internal.InternalContributions;
import se.l4.dust.core.internal.NamespaceDiscoveryImpl;
import se.l4.dust.core.internal.NamespacesImpl;
import se.l4.dust.core.internal.asset.AssetModule;
import se.l4.dust.core.internal.conversion.ConversionModule;
import se.l4.dust.core.internal.expression.ExpressionModule;
import se.l4.dust.core.internal.messages.MessagesModule;
import se.l4.dust.core.internal.resource.ResourceModule;
import se.l4.dust.core.internal.template.TemplateModule;

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
		install(new ExpressionModule());
		install(new MessagesModule());

		bind(Namespaces.class).to(NamespacesImpl.class);
		bindContributions(NamespaceBinding.class);
		bind(NamespaceDiscovery.class).to(NamespaceDiscoveryImpl.class);

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

	@Contribution
	@Named("dust-namespaces")
	public void bindNamespaces(@NamespaceBinding Contributions contributions)
	{
		InternalContributions.add(contributions);

		contributions.run();
	}
}
