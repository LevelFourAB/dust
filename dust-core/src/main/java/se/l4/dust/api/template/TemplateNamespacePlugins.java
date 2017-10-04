package se.l4.dust.api.template;

import com.google.inject.Injector;
import com.google.inject.Stage;

import se.l4.dust.api.Namespace;
import se.l4.dust.api.NamespacePlugin;
import se.l4.dust.api.Namespaces;
import se.l4.dust.api.asset.Assets;
import se.l4.dust.api.expression.ExpressionSource;
import se.l4.dust.api.expression.Expressions;
import se.l4.dust.api.messages.Messages;
import se.l4.dust.core.internal.asset.AssetExpressionSource;
import se.l4.dust.core.internal.messages.NamespaceMessagesExpressionSource;

/**
 * A collection of plugins for {@link Namespaces.NamespaceBinder} that
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
	 * {@code namespace(.messages)} relative to its package.
	 *
	 * @return
	 */
	public static NamespacePlugin messages()
	{
		return messagesIn("namespace");
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
				NamespaceMessagesExpressionSource source = new NamespaceMessagesExpressionSource(
					injector.getInstance(Stage.class),
					injector.getInstance(Messages.class),
					ns,
					relativeFileWithoutExtension
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
				Assets assets = injector.getInstance(Assets.class);

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
				Assets assets = injector.getInstance(Assets.class);

				injector.getInstance(Expressions.class)
					.addSource(ns.getUri(), new AssetExpressionSource(assets, namespace));
			}
		};
	}
}
