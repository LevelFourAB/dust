package se.l4.dust.api.template;

import java.net.URI;

import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.NamespaceManager.Namespace;
import se.l4.dust.api.NamespacePlugin;
import se.l4.dust.api.asset.AssetManager;
import se.l4.dust.api.expression.ExpressionSource;
import se.l4.dust.api.expression.Expressions;
import se.l4.dust.api.messages.MessageManager;
import se.l4.dust.core.internal.asset.AssetExpressionSource;
import se.l4.dust.core.internal.messages.CustomMessageExpressionSource;

import com.google.inject.Injector;
import com.google.inject.Stage;

/**
 * A collection of plugins for {@link NamespaceManager.NamespaceBinder} that
 * works together with the template engine.
 * 
 * @author Andreas Holstenson
 *
 */
public class TemplateNamespacePlugins
{
	private TemplateNamespacePlugins()
	{
	}
	
	/**
	 * Register an expression source for the namespace. Calls
	 * {@link Expressions#addSource(String, ExpressionSource)} with the
	 * given source.
	 * 
	 * @param source
	 * @return
	 */
	public static NamespacePlugin expressions(final ExpressionSource source)
	{
		return new NamespacePlugin()
		{
			@Override
			public void register(Injector injector, Namespace ns)
			{
				injector.getInstance(Expressions.class).addSource(ns.getUri(), source);
			}
		};
	}
	
	/**
	 * Register the namespace to resolve messages from the file
	 * {@code messages(.properties)} relative to its package.
	 * 
	 * @return
	 */
	public static NamespacePlugin messages()
	{
		return messagesIn("messages");
	}

	/**
	 * Register the namespace to resolve messages from the given relative file
	 * to its package.
	 * 
	 * @param relativeFileWithoutExtension
	 * @return
	 */
	public static NamespacePlugin messagesIn(final String relativeFileWithoutExtension)
	{
		return new NamespacePlugin()
		{
			@Override
			public void register(Injector injector, Namespace ns)
			{
				URI uri = ns.resolveResource(relativeFileWithoutExtension);
				CustomMessageExpressionSource source = new CustomMessageExpressionSource(
					injector.getInstance(Stage.class),
					injector.getInstance(MessageManager.class),
					uri.toString()
				);
				
				injector.getInstance(Expressions.class).addSource(ns.getUri(), source);
			}
		};
	}
	
	/**
	 * Register the namespace so that assets can be resolved in templates.
	 * 
	 * @return
	 */
	public static NamespacePlugin assets()
	{
		return new NamespacePlugin()
		{
			@Override
			public void register(Injector injector, Namespace ns)
			{
				AssetManager assets = injector.getInstance(AssetManager.class);
				
				injector.getInstance(Expressions.class)
					.addSource(ns.getUri(), new AssetExpressionSource(assets, ns.getUri()));
			}
		};
	}
	
	/**
	 * Register so that the namespace resolved assets that are registered in
	 * another namespace.
	 * 
	 * @param namespace
	 * @return
	 */
	public static NamespacePlugin assets(final String namespace)
	{
		return new NamespacePlugin()
		{
			@Override
			public void register(Injector injector, Namespace ns)
			{
				AssetManager assets = injector.getInstance(AssetManager.class);
				
				injector.getInstance(Expressions.class)
					.addSource(ns.getUri(), new AssetExpressionSource(assets, namespace));
			}
		};
	}
}
