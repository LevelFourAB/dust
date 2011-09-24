package se.l4.dust.core.internal.expression;

import java.lang.reflect.Method;
import java.util.List;

import se.l4.dust.api.conversion.NonGenericConversion;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.core.internal.expression.ast.AndNode;
import se.l4.dust.core.internal.expression.ast.ChainNode;
import se.l4.dust.core.internal.expression.ast.DoubleNode;
import se.l4.dust.core.internal.expression.ast.EqualsNode;
import se.l4.dust.core.internal.expression.ast.GreaterNode;
import se.l4.dust.core.internal.expression.ast.GreaterOrEqualNode;
import se.l4.dust.core.internal.expression.ast.IdentifierNode;
import se.l4.dust.core.internal.expression.ast.InvokeNode;
import se.l4.dust.core.internal.expression.ast.KeywordNode;
import se.l4.dust.core.internal.expression.ast.LeftRightNode;
import se.l4.dust.core.internal.expression.ast.LessNode;
import se.l4.dust.core.internal.expression.ast.LessOrEqualNode;
import se.l4.dust.core.internal.expression.ast.LongNode;
import se.l4.dust.core.internal.expression.ast.NegateNode;
import se.l4.dust.core.internal.expression.ast.Node;
import se.l4.dust.core.internal.expression.ast.NotEqualsNode;
import se.l4.dust.core.internal.expression.ast.OrNode;
import se.l4.dust.core.internal.expression.ast.StringNode;
import se.l4.dust.core.internal.expression.ast.TernaryNode;
import se.l4.dust.core.internal.expression.invoke.AndInvoker;
import se.l4.dust.core.internal.expression.invoke.ChainInvoker;
import se.l4.dust.core.internal.expression.invoke.ConstantInvoker;
import se.l4.dust.core.internal.expression.invoke.ConvertingInvoker;
import se.l4.dust.core.internal.expression.invoke.EqualsInvoker;
import se.l4.dust.core.internal.expression.invoke.Invoker;
import se.l4.dust.core.internal.expression.invoke.MethodInvoker;
import se.l4.dust.core.internal.expression.invoke.MethodPropertyInvoker;
import se.l4.dust.core.internal.expression.invoke.NegateInvoker;
import se.l4.dust.core.internal.expression.invoke.NumericComparisonInvoker;
import se.l4.dust.core.internal.expression.invoke.OrInvoker;
import se.l4.dust.core.internal.expression.invoke.TernaryInvoker;
import se.l4.dust.core.internal.expression.invoke.ThisInvoker;

import com.google.common.base.CaseFormat;
import com.google.common.primitives.Primitives;

/**
 * Resolver for expressions, turns the AST into an invocation chain.
 * 
 * @author Andreas Holstenson
 *
 */
public class ExpressionResolver
{
	private final Node root;
	private final ErrorHandler errors;
	private final TypeConverter converter;

	public ExpressionResolver(TypeConverter converter, ErrorHandler errors, Node root)
	{
		this.converter = converter;
		this.errors = errors;
		this.root = root;
	}
	
	/**
	 * Resolve the expression against the given context.
	 * 
	 * @param context
	 * @return
	 */
	public Invoker resolve(Class<?> context)
	{
		return resolve(root, context, context);
	}
	
	/**
	 * Resolve the given node against the specified context.
	 * 
	 * @param node
	 * @param context
	 * @return
	 */
	private Invoker resolve(Node node, Class<?> root, Class<?> context)
	{
		if(node instanceof IdentifierNode)
		{
			return resolveIdentifier((IdentifierNode) node, context);
		}
		else if(node instanceof ChainNode)
		{
			// Resolve a chain of other nodes
			ChainNode chain = (ChainNode) node;
			
			Invoker leftInvoker = resolve(chain.getLeft(), root, context);
			Invoker rightInvoker = resolve(chain.getRight(), root, leftInvoker.getResult());
			
			return new ChainInvoker(node, leftInvoker, rightInvoker);
		}
		else if(node instanceof LongNode)
		{
			// Longs are treated as simple constants
			return new ConstantInvoker(node, ((LongNode) node).getValue());
		}
		else if(node instanceof DoubleNode)
		{
			// Doubles are treated as simple constants
			return new ConstantInvoker(node, ((DoubleNode) node).getValue());
		}
		else if(node instanceof StringNode)
		{
			// Strings are also just constants
			return new ConstantInvoker(node, ((StringNode) node).getValue());
		}
		else if(node instanceof KeywordNode)
		{
			// Keywords may be constants or the special this invoker
			KeywordNode kw = (KeywordNode) node;
			switch(kw.getType())
			{
				case FALSE:
					return new ConstantInvoker(node, false);
				case TRUE:
					return new ConstantInvoker(node, true);
				case NULL:
					return new ConstantInvoker(node, null);
				case THIS:
					return new ThisInvoker(node, root);
			}
		}
		else if(node instanceof NegateNode)
		{
			// Resolve the wrapped node and optionally convert
			NegateNode nn = (NegateNode) node;
			Invoker invoker = resolve(nn.getNode(), root, context);
			
			if(! isBoolean(invoker.getResult()))
			{
				invoker = toConverting(invoker, boolean.class);
			}
			
			return new NegateInvoker(node, invoker);
		}
		else if(node instanceof AndNode || node instanceof OrNode)
		{
			/*
			 * The left and right side is resolve and then forcefully
			 * converted to boolean to ensure that it is possible to
			 * execute the condition.
			 */
			LeftRightNode chain = (LeftRightNode) node;
			Invoker leftInvoker = resolve(chain.getLeft(), root, context);
			Invoker rightInvoker = resolve(chain.getRight(), root, context);
			
			if(! isBoolean(leftInvoker.getResult()))
			{
				leftInvoker = toConverting(leftInvoker, boolean.class);
			}
			
			if(! isBoolean(rightInvoker.getResult()))
			{
				rightInvoker = toConverting(rightInvoker, boolean.class);
			}
			
			return node instanceof AndNode 
				? new AndInvoker(node, leftInvoker, rightInvoker)
				: new OrInvoker(node, leftInvoker, rightInvoker);
		}
		else if(node instanceof EqualsNode || node instanceof NotEqualsNode)
		{
			// Not equals is treated as an equals node wrapped with a negation
			LeftRightNode chain = (LeftRightNode) node;
			Invoker leftInvoker = resolve(chain.getLeft(), root, context);
			Invoker rightInvoker = resolve(chain.getRight(), root, context);
			
			if(leftInvoker.getResult() != rightInvoker.getResult())
			{
				// Non-matching results, check if we need to do some conversions
				if(leftInvoker.getResult() == void.class || rightInvoker.getResult() == void.class)
				{
					// Void is special, skip for now
				}
				else if(isNumber(leftInvoker.getResult()))
				{
					// Left is number, might need to convert right
					if(! isNumber(rightInvoker.getResult()))
					{
						rightInvoker = toConverting(rightInvoker, double.class);
					}
					
					return new NumericComparisonInvoker(node, leftInvoker, rightInvoker);
				}
				else if(isNumber(rightInvoker.getResult()))
				{
					// Right is number, might need to convert left
					if(! isNumber(leftInvoker.getResult()))
					{
						leftInvoker = toConverting(leftInvoker, double.class);
					}
					
					return new NumericComparisonInvoker(node, leftInvoker, rightInvoker);
				}
			}
			
			EqualsInvoker invoker = new EqualsInvoker(node, leftInvoker, rightInvoker);
			return node instanceof EqualsNode
				? invoker
				: new NegateInvoker(node, invoker);
		}
		else if(node instanceof LessNode || node instanceof LessOrEqualNode
			|| node instanceof GreaterNode || node instanceof GreaterOrEqualNode)
		{
			// Numeric comparisons are all handled by the same invoker
			LeftRightNode chain = (LeftRightNode) node;
			Invoker leftInvoker = resolve(chain.getLeft(), root, context);
			Invoker rightInvoker = resolve(chain.getRight(), root, context);
			
			if(! Number.class.isAssignableFrom(Primitives.wrap(leftInvoker.getResult())))
			{
				leftInvoker = toConverting(leftInvoker, Double.class);
			}
			
			if(! Number.class.isAssignableFrom(Primitives.wrap(rightInvoker.getResult())))
			{
				rightInvoker = toConverting(rightInvoker, Double.class);
			}
			
			return new NumericComparisonInvoker(node, leftInvoker, rightInvoker);
		}
		else if(node instanceof TernaryNode)
		{
			TernaryNode tn = (TernaryNode) node;
			Invoker test = resolve(tn.getTest(), root, context);
			Invoker left = resolve(tn.getLeft(), root, context);
			Invoker right = tn.getRight() == null ? null : resolve(tn.getRight(), root, context);
			
			if(! isBoolean(test.getResult()))
			{
				test = toConverting(test, boolean.class);
			}
			
			return new TernaryInvoker(node, test, left, right);
		}
		else if(node instanceof InvokeNode)
		{
			InvokeNode in = (InvokeNode) node;
			IdentifierNode id = in.getId();
			
			// Create the parameters first
			List<Node> params = in.getParameters();
			Invoker[] actualParams = new Invoker[params.size()];
			int i = 0;
			for(Node n : params)
			{
				// Resolution always occurs against the root
				actualParams[i] = resolve(n, root, root);
				i++;
			}
			
			if(id.getNamespace() != null)
			{
				// TODO: Resolve method
				throw errors.error(node, "No namespace handler found for " + id.toHumanReadable());
			}
			else
			{
				return resolveMethod(node, id, actualParams, context);
			}
		}
		
		throw errors.error(node, "Unknown node of type: " + node.getClass());
	}
	
	private ConvertingInvoker toConverting(Invoker invoker, Class<?> output)
	{
		Class<?> in = invoker.getResult();
		if(converter.canConvertBetween(in, output))
		{
			NonGenericConversion<?, ?> conversion = converter.getConversion(in, output);
			return new ConvertingInvoker(invoker.getNode(), conversion, invoker);
		}
		
		// No suitable conversion, throw error
		throw errors.error(invoker.getNode(), "Expected type " + output 
			+ ", but got " + invoker.getResult() 
			+ " with no way of converting to " + output.getSimpleName());
	}
	
	private boolean isBoolean(Class<?> type)
	{
		return type == Boolean.class || type == boolean.class;
	}
	
	private boolean isNumber(Class<?> type)
	{
		return Number.class.isAssignableFrom(Primitives.wrap(type));
	}
	
	/**
	 * Resolve an identifier based on the specified context. This will look
	 * for a getter and return a {@link MethodPropertyInvoker} if a suitable
	 * one is found.
	 * 
	 * @param node
	 * @param context
	 * @return
	 */
	private Invoker resolveIdentifier(IdentifierNode node, Class<?> context)
	{
		if(node.getNamespace() != null)
		{
			// TODO: Resolve via property sources
			throw errors.error(node, "No namespace handler found for " + node.toHumanReadable());
		}
		
		for(Method m : context.getMethods())
		{
			if(m.getParameterTypes().length != 0)
			{
				// Only support methods zero parameters
				continue;
			}
				
			String name = m.getName();
			if(name.startsWith("is"))
			{
				name = name.substring(2);
			}
			else if(name.startsWith("get") || name.startsWith("set")
				|| name.startsWith("has"))
			{
				name = name.substring(3);
			}
			else
			{
				continue;
			}
				
			name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
			if(name.equals(node.getIdentifier()))
			{
				return new MethodPropertyInvoker(node, m);
			}
		}
		
		throw errors.error(node, "Unable to find a suitable getter for '" + node.toHumanReadable()  + "' in " + context);
	}
	
	private Invoker resolveMethod(Node node, IdentifierNode id, Invoker[] actualParams, Class<?> context)
	{
		// First pass: Look for exact matches
		_outer:
		for(Method m : context.getMethods())
		{
			String name = m.getName();
			if(false == name.equals(id.getIdentifier()))
			{
				continue;
			}
			
			Class<?>[] types = m.getParameterTypes();
			if(actualParams.length != types.length)
			{
				continue;
			}
			
			// Potentially the correct method
			for(int i=0, n=types.length; i<n; i++)
			{
				if(! types[i].isAssignableFrom(actualParams[i].getResult()))
				{
					// Not directly assignable, continue
					continue _outer;
				}
			}
			
			return new MethodInvoker(node, m, actualParams);
		}
	
		// Second pass: Try to convert params
		_outer:
		for(Method m : context.getMethods())
		{
			String name = m.getName();
			if(false == name.equals(id.getIdentifier()))
			{
				continue;
			}
			
			Class<?>[] types = m.getParameterTypes();
			if(actualParams.length != types.length)
			{
				continue;
			}
			
			// Potentially the correct method
			Invoker[] newParams = new Invoker[actualParams.length];
			for(int i=0, n=types.length; i<n; i++)
			{
				if(types[i].isAssignableFrom(actualParams[i].getResult()))
				{
					newParams[i] = actualParams[i];
				}
				else if(converter.canConvertBetween(actualParams[i].getResult(), types[i]))
				{
					newParams[i] = toConverting(actualParams[i], types[i]);
				}
				else
				{
					// No conversion found, skip this method
					continue _outer;
				}
			}
			
			return new MethodInvoker(node, m, newParams);
		}
		
		throw errors.error(node, "No matching method found");
	}

}
