package se.l4.dust.core.internal.expression;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.Stage;

import se.l4.dust.api.Value;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.expression.Expression;
import se.l4.dust.api.expression.ExpressionSource;
import se.l4.dust.api.expression.Expressions;
import se.l4.dust.api.resource.ResourceLocation;
import se.l4.dust.core.internal.expression.ast.Node;
import se.l4.dust.core.internal.expression.invoke.Invoker;

/**
 * Implementation of {@link Expressions}.
 *
 * @author Andreas Holstenson
 *
 */
@Singleton
public class ExpressionsImpl
	implements Expressions
{
	private final TypeConverter converter;
	private final ConcurrentMap<String, ExpressionSource> sources;
	private final boolean production;
	private final Cache<Key, Expression> cachedExpressions;

	@Inject
	public ExpressionsImpl(TypeConverter converter, Stage stage)
	{
		this.converter = converter;
		production = stage != Stage.DEVELOPMENT;
		sources = new ConcurrentHashMap<>();

		cachedExpressions = CacheBuilder.newBuilder()
			.build();
	}

	@Override
	public void addSource(String namespace, ExpressionSource source)
	{
		ExpressionSource existing = sources.get(namespace);
		if(existing != null)
		{
			source = new ExpressionSourceChain(existing, source);
		}

		sources.put(namespace, source);
	}

	public ExpressionSource getSource(String namespace)
	{
		return sources.get(namespace);
	}

	/**
	 * Attempt to resolve a suitable type for the given object.
	 *
	 * @param context
	 * @return
	 */
	@Override
	public Class<?> resolveType(Object context)
	{
		if(context instanceof Class)
		{
			return (Class) context;
		}
		else if(context instanceof Value)
		{
			return ((Value) context).getType();
		}
		else
		{
			// Fallback to Object
			return context.getClass();
		}
	}

	@Override
	public Expression compile(ResourceLocation source, Map<String, String> namespaces, String expression, Class<?> localContext)
	{
		Key key = new Key(expression, localContext);
		Expression result = cachedExpressions.getIfPresent(key);
		if(result != null)
		{
			return result;
		}

		if(production)
		{
			ErrorHandler errors = new ErrorHandlerImpl(expression);
			Node node = ExpressionParser.parse(expression);
			Invoker invoker = new ExpressionResolver(
				converter,
				this,
				source,
				namespaces,
				errors,
				node
			).resolve(localContext);

			ExpressionCompiler compiler = new ExpressionCompiler(errors, expression, localContext, invoker);
			result =  compiler.compile();
		}
		else
		{
			result = new ExpressionDebugger(converter, this, source, namespaces, expression, localContext);
		}

		cachedExpressions.put(key, result);
		return result;
	}

	private static class Key
	{
		private final String expression;
		private final Class<?> context;

		public Key(String expression, Class<?> context)
		{
			this.expression = expression;
			this.context = context;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((context == null) ? 0 : context.hashCode());
			result = prime * result
					+ ((expression == null) ? 0 : expression.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if(context == null)
			{
				if(other.context != null)
					return false;
			}
			else if(!context.equals(other.context))
				return false;
			if(expression == null)
			{
				if(other.expression != null)
					return false;
			}
			else if(!expression.equals(other.expression))
				return false;
			return true;
		}
	}
}
