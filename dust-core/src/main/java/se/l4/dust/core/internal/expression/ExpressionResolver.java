package se.l4.dust.core.internal.expression;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import se.l4.dust.api.conversion.NonGenericConversion;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.expression.DynamicMethod;
import se.l4.dust.api.expression.DynamicProperty;
import se.l4.dust.api.expression.ExpressionEncounter;
import se.l4.dust.api.expression.ExpressionException;
import se.l4.dust.api.expression.ExpressionSource;
import se.l4.dust.core.internal.expression.ast.AddNode;
import se.l4.dust.core.internal.expression.ast.AndNode;
import se.l4.dust.core.internal.expression.ast.ChainNode;
import se.l4.dust.core.internal.expression.ast.DivideNode;
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
import se.l4.dust.core.internal.expression.ast.ModuloNode;
import se.l4.dust.core.internal.expression.ast.MultiplyNode;
import se.l4.dust.core.internal.expression.ast.NegateNode;
import se.l4.dust.core.internal.expression.ast.Node;
import se.l4.dust.core.internal.expression.ast.NotEqualsNode;
import se.l4.dust.core.internal.expression.ast.OrNode;
import se.l4.dust.core.internal.expression.ast.StringNode;
import se.l4.dust.core.internal.expression.ast.SubtractNode;
import se.l4.dust.core.internal.expression.ast.TernaryNode;
import se.l4.dust.core.internal.expression.invoke.AndInvoker;
import se.l4.dust.core.internal.expression.invoke.ChainInvoker;
import se.l4.dust.core.internal.expression.invoke.ConstantInvoker;
import se.l4.dust.core.internal.expression.invoke.ConvertingInvoker;
import se.l4.dust.core.internal.expression.invoke.DynamicMethodInvoker;
import se.l4.dust.core.internal.expression.invoke.DynamicPropertyInvoker;
import se.l4.dust.core.internal.expression.invoke.EqualsInvoker;
import se.l4.dust.core.internal.expression.invoke.Invoker;
import se.l4.dust.core.internal.expression.invoke.MethodInvoker;
import se.l4.dust.core.internal.expression.invoke.MethodPropertyInvoker;
import se.l4.dust.core.internal.expression.invoke.NegateInvoker;
import se.l4.dust.core.internal.expression.invoke.NumericComparisonInvoker;
import se.l4.dust.core.internal.expression.invoke.NumericOperationInvoker;
import se.l4.dust.core.internal.expression.invoke.OrInvoker;
import se.l4.dust.core.internal.expression.invoke.StringConcatInvoker;
import se.l4.dust.core.internal.expression.invoke.TernaryInvoker;
import se.l4.dust.core.internal.expression.invoke.ThisInvoker;

import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.ResolvedMethod;
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
	
	private final ExpressionsImpl expressions;
	private final Map<String, String> namespaces;
	private TypeResolver typeResolver;
	private MemberResolver memberResolver;

	public ExpressionResolver(
			TypeConverter converter, 
			ExpressionsImpl expressions,
			Map<String, String> namespaces,
			ErrorHandler errors, 
			Node root)
	{
		this.converter = converter;
		this.expressions = expressions;
		this.namespaces = namespaces;
		this.errors = errors;
		this.root = root;
		
		typeResolver = new TypeResolver();
		memberResolver = new MemberResolver(typeResolver);
		memberResolver.setIncludeLangObject(true);
	}
	
	/**
	 * Resolve the expression against the given context.
	 * 
	 * @param context
	 * @return
	 */
	public Invoker resolve(Class<?> context)
	{
		EncounterImpl encounter = new EncounterImpl(root, context);
		return resolve0(encounter, root, context, context);
	}
	
	/**
	 * Resolve the given node against the specified context.
	 * @param encounter 
	 * 
	 * @param node
	 * @param context
	 * @return
	 */
	private Invoker resolve(EncounterImpl encounter, Node node, Class<?> root, Class<?> context)
	{
		encounter.setContext(node, context);
		encounter.increaseLevel();
		
		Invoker invoker = resolve0(encounter, node, root, context);
		
		encounter.decreaseLevel();
		return invoker;
	}
	
	private Invoker resolve0(EncounterImpl encounter, Node node, Class<?> root, Class<?> context)
	{
		if(node instanceof IdentifierNode)
		{
			return resolveIdentifier(encounter, (IdentifierNode) node, context);
		}
		else if(node instanceof ChainNode)
		{
			// Resolve a chain of other nodes
			ChainNode chain = (ChainNode) node;
			
			Invoker leftInvoker = resolve(encounter, chain.getLeft(), root, context);
			Invoker rightInvoker = resolve(encounter, chain.getRight(), root, leftInvoker.getReturnClass());
			
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
			Invoker invoker = resolve(encounter, nn.getNode(), root, context);
			
			if(! isBoolean(invoker.getReturnClass()))
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
			Invoker leftInvoker = resolve(encounter, chain.getLeft(), root, context);
			Invoker rightInvoker = resolve(encounter, chain.getRight(), root, context);
			
			if(! isBoolean(leftInvoker.getReturnClass()))
			{
				leftInvoker = toConverting(leftInvoker, boolean.class);
			}
			
			if(! isBoolean(rightInvoker.getReturnClass()))
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
			Invoker leftInvoker = resolve(encounter, chain.getLeft(), root, context);
			Invoker rightInvoker = resolve(encounter, chain.getRight(), root, context);
			
			if(leftInvoker.getReturnClass() != rightInvoker.getReturnClass())
			{
				// Non-matching results, check if we need to do some conversions
				if(leftInvoker.getReturnClass() == void.class || rightInvoker.getReturnClass() == void.class)
				{
					// Void is special, skip for now
				}
				else if(isNumber(leftInvoker.getReturnClass()))
				{
					// Left is number, might need to convert right
					if(! isNumber(rightInvoker.getReturnClass()))
					{
						rightInvoker = toConverting(rightInvoker, double.class);
					}
					
					return new NumericComparisonInvoker(node, leftInvoker, rightInvoker);
				}
				else if(isNumber(rightInvoker.getReturnClass()))
				{
					// Right is number, might need to convert left
					if(! isNumber(leftInvoker.getReturnClass()))
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
			Invoker leftInvoker = resolve(encounter, chain.getLeft(), root, context);
			Invoker rightInvoker = resolve(encounter, chain.getRight(), root, context);
			
			if(! Number.class.isAssignableFrom(Primitives.wrap(leftInvoker.getReturnClass())))
			{
				leftInvoker = toConverting(leftInvoker, Double.class);
			}
			
			if(! Number.class.isAssignableFrom(Primitives.wrap(rightInvoker.getReturnClass())))
			{
				rightInvoker = toConverting(rightInvoker, Double.class);
			}
			
			return new NumericComparisonInvoker(node, leftInvoker, rightInvoker);
		}
		else if(node instanceof TernaryNode)
		{
			TernaryNode tn = (TernaryNode) node;
			Invoker test = resolve(encounter, tn.getTest(), root, context);
			Invoker left = resolve(encounter, tn.getLeft(), root, context);
			Invoker right = tn.getRight() == null ? null : resolve(encounter, tn.getRight(), root, context);
			
			if(! isBoolean(test.getReturnClass()))
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
				actualParams[i] = resolve(encounter, n, root, root);
				i++;
			}
			
			if(id.getNamespace() != null)
			{
				String ns = namespaces.get(id.getNamespace());
				if(ns == null)
				{
					throw errors.error(node, "No namespace bound for prefix " + id.getNamespace());
				}
				
				ExpressionSource source = expressions.getSource(ns);
				if(source == null)
				{
					throw errors.error(node, "There are not properties or methods available in namespace " + ns);
				}
				
				Class[] actualParamTypes = new Class[actualParams.length];
				for(int j=0, n=actualParams.length; j<n; j++)
				{
					actualParamTypes[j] = actualParams[j].getReturnClass();
				}
				
				DynamicMethod method = source.getMethod(encounter, id.getIdentifier(), actualParamTypes);
				if(method == null)
				{
					throw errors.error(node, "There is no method named " + id.getIdentifier() + " in namespace " + ns);
				}
				
				return new DynamicMethodInvoker(node, method, actualParams);
			}
			else
			{
				return resolveMethod(node, id, actualParams, context);
			}
		}
		else if(node instanceof AddNode)
		{
			AddNode an = (AddNode) node;
			Invoker left = resolve(encounter, an.getLeft(), root, context);
			Invoker right = resolve(encounter, an.getLeft(), root, context);
			
			if(isNumber(left.getReturnClass()) && isNumber(right.getReturnClass()))
			{
				return new NumericOperationInvoker(node, left, right);
			}
			
			return new StringConcatInvoker(node, left, right);
		}
		else if(node instanceof SubtractNode || node instanceof DivideNode 
				|| node instanceof MultiplyNode || node instanceof ModuloNode)
		{
			LeftRightNode an = (LeftRightNode) node;
			Invoker left = resolve(encounter, an.getLeft(), root, context);
			Invoker right = resolve(encounter, an.getLeft(), root, context);
			
			return new NumericOperationInvoker(node, left, right);
		}
		
		throw errors.error(node, "Unknown node of type: " + node.getClass());
	}
	
	private ConvertingInvoker toConverting(Invoker invoker, Class<?> output)
	{
		Class<?> in = invoker.getReturnClass();
		if(converter.canConvertBetween(in, output))
		{
			NonGenericConversion<?, ?> conversion = converter.getConversion(in, output);
			return new ConvertingInvoker(invoker.getNode(), conversion, invoker);
		}
		
		// No suitable conversion, throw error
		throw errors.error(invoker.getNode(), "Expected type " + output 
			+ ", but got " + invoker.getReturnClass() 
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
	private Invoker resolveIdentifier(EncounterImpl encounter, IdentifierNode node, Class<?> context)
	{
		if(node.getNamespace() != null)
		{
			// First resolve the namespace
			String ns = namespaces.get(node.getNamespace());
			if(ns == null)
			{
				throw errors.error(node, "No namespace bound for prefix " + node.getNamespace());
			}
			
			ExpressionSource source = expressions.getSource(ns);
			if(source == null)
			{
				throw errors.error(node, "There are not properties or methods available in namespace " + ns);
			}
			
			DynamicProperty property = source.getProperty(encounter, node.getIdentifier());
			if(property == null)
			{
				throw errors.error(node, "There is no property named " + node.getIdentifier() + " in namespace " + ns);
			}
			
			return new DynamicPropertyInvoker(node, property);
		}
		
		ResolvedType type = typeResolver.resolve(context);
		ResolvedTypeWithMembers members = memberResolver.resolve(type, null, null);
		for(ResolvedMethod rm : members.getMemberMethods())
		{
			Method m = rm.getRawMember();
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
				
			String lowerName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
			if(lowerName.equals(node.getIdentifier()))
			{
				Method setter = null;
				try
				{
					setter = context.getMethod("set" + name, m.getReturnType());
				}
				catch(SecurityException e)
				{
					// Ignore, not all getters have setters
				}
				catch(NoSuchMethodException e)
				{
					// Ignore, not all getters have setters
				}
				
				return new MethodPropertyInvoker(node, rm.getReturnType().getErasedType(), m, setter);
			}
		}
		
		throw errors.error(node, "Unable to find a suitable getter for '" + node.toHumanReadable()  + "' in " + context);
	}
	
	private Invoker resolveMethod(Node node, IdentifierNode id, Invoker[] actualParams, Class<?> context)
	{
		// First pass: Look for exact matches
		ResolvedType type = typeResolver.resolve(context);
		ResolvedTypeWithMembers members = memberResolver.resolve(type, null, null);
		
		_outer:
		for(ResolvedMethod rm : members.getMemberMethods())
		{
			Method m = rm.getRawMember();
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
				if(! types[i].isAssignableFrom(actualParams[i].getReturnClass()))
				{
					// Not directly assignable, continue
					continue _outer;
				}
			}
			
			return new MethodInvoker(node, rm.getReturnType().getErasedType(), m, actualParams);
		}
	
		// Second pass: Try to convert params
		_outer:
		for(ResolvedMethod rm : members.getMemberMethods())
		{
			Method m = rm.getRawMember();
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
				if(types[i].isAssignableFrom(actualParams[i].getReturnClass()))
				{
					newParams[i] = actualParams[i];
				}
				else if(converter.canConvertBetween(actualParams[i].getReturnClass(), types[i]))
				{
					newParams[i] = toConverting(actualParams[i], types[i]);
				}
				else
				{
					// No conversion found, skip this method
					continue _outer;
				}
			}
			
			return new MethodInvoker(node, rm.getReturnType().getErasedType(), m, newParams);
		}
		
		throw errors.error(node, "No matching method found");
	}

	private class EncounterImpl
		implements ExpressionEncounter
	{
		private final Class<?> root;
		private Class<?> context;
		private Node node;
		private int level;
		
		public EncounterImpl(Node start, Class<?> root)
		{
			this.root = root;
			this.context = root;
			node = start;
		}
		
		public void setContext(Node node, Class<?> context)
		{
			this.context = context;
			this.node = node;
		}

		@Override
		public boolean isRoot()
		{
			return level == 0;
		}
		
		public void increaseLevel()
		{
			level++;
		}
		
		public void decreaseLevel()
		{
			level--;
		}

		@Override
		public Class<?> getContext()
		{
			return context;
		}

		@Override
		public Class<?> getRoot()
		{
			return root;
		}

		@Override
		public ExpressionException error(String message)
		{
			return errors.error(node, message);
		}
		
	}
}
