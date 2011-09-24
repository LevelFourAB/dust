package se.l4.dust.core.internal.expression;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.expression.Expression;
import se.l4.dust.api.expression.Expressions;
import se.l4.dust.api.expression.ExpressionSource;

import com.google.inject.Inject;
import com.google.inject.Singleton;

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
	
	@Inject
	public ExpressionsImpl(TypeConverter converter)
	{
		this.converter = converter;
		sources = new ConcurrentHashMap<String, ExpressionSource>();
	}

	@Override
	public void addSource(String namespace, ExpressionSource source)
	{
		sources.put(namespace, source);
	}
	
	public ExpressionSource getSource(String namespace)
	{
		return sources.get(namespace);
	}

	@Override
	public Expression compile(Map<String, String> namespaces, String expression, Class<?> localContext)
	{
		return new ExpressionDebugger(converter, this, namespaces, expression, localContext);
	}
}
