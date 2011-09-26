package se.l4.dust.core.internal.expression;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.expression.Expression;
import se.l4.dust.api.expression.ExpressionSource;
import se.l4.dust.api.expression.Expressions;
import se.l4.dust.core.internal.expression.ast.Node;
import se.l4.dust.core.internal.expression.invoke.Invoker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.Stage;

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
	
	@Inject
	public ExpressionsImpl(TypeConverter converter, Stage stage)
	{
		this.converter = converter;
		production = stage != Stage.DEVELOPMENT;
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
		if(production)
		{
			ErrorHandler errors = new ErrorHandlerImpl(expression);
			Node node = ExpressionParser.parse(expression);
			Invoker invoker = new ExpressionResolver(
				converter, 
				this,
				namespaces,
				errors, 
				node
			).resolve(localContext);
			
			ExpressionCompiler compiler = new ExpressionCompiler(errors, localContext, invoker);
			return compiler.compile();
		}
		else
		{
			return new ExpressionDebugger(converter, this, namespaces, expression, localContext);
		}
	}
}
