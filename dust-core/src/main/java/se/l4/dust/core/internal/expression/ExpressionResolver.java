package se.l4.dust.core.internal.expression;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.ResolvedField;
import com.fasterxml.classmate.members.ResolvedMethod;
import com.fasterxml.classmate.types.ResolvedRecursiveType;
import com.google.common.base.CaseFormat;
import com.google.common.primitives.Primitives;

import se.l4.dust.api.conversion.NonGenericConversion;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.expression.DynamicMethod;
import se.l4.dust.api.expression.DynamicProperty;
import se.l4.dust.api.expression.ExpressionEncounter;
import se.l4.dust.api.expression.ExpressionException;
import se.l4.dust.api.expression.ExpressionSource;
import se.l4.dust.api.resource.ResourceLocation;
import se.l4.dust.api.template.Expose;
import se.l4.dust.core.internal.expression.ast.AddNode;
import se.l4.dust.core.internal.expression.ast.AndNode;
import se.l4.dust.core.internal.expression.ast.ArrayNode;
import se.l4.dust.core.internal.expression.ast.ChainNode;
import se.l4.dust.core.internal.expression.ast.DivideNode;
import se.l4.dust.core.internal.expression.ast.DoubleNode;
import se.l4.dust.core.internal.expression.ast.EqualsNode;
import se.l4.dust.core.internal.expression.ast.GreaterNode;
import se.l4.dust.core.internal.expression.ast.GreaterOrEqualNode;
import se.l4.dust.core.internal.expression.ast.IdentifierNode;
import se.l4.dust.core.internal.expression.ast.IndexNode;
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
import se.l4.dust.core.internal.expression.invoke.ArrayIndexInvoker;
import se.l4.dust.core.internal.expression.invoke.ArrayInvoker;
import se.l4.dust.core.internal.expression.invoke.ChainInvoker;
import se.l4.dust.core.internal.expression.invoke.ConstantInvoker;
import se.l4.dust.core.internal.expression.invoke.ConvertingInvoker;
import se.l4.dust.core.internal.expression.invoke.DynamicConversionInvoker;
import se.l4.dust.core.internal.expression.invoke.DynamicMethodInvoker;
import se.l4.dust.core.internal.expression.invoke.DynamicPropertyInvoker;
import se.l4.dust.core.internal.expression.invoke.EqualsInvoker;
import se.l4.dust.core.internal.expression.invoke.FieldPropertyInvoker;
import se.l4.dust.core.internal.expression.invoke.Invoker;
import se.l4.dust.core.internal.expression.invoke.LongToIntInvoker;
import se.l4.dust.core.internal.expression.invoke.MethodInvoker;
import se.l4.dust.core.internal.expression.invoke.MethodPropertyInvoker;
import se.l4.dust.core.internal.expression.invoke.NegateInvoker;
import se.l4.dust.core.internal.expression.invoke.NumericComparisonInvoker;
import se.l4.dust.core.internal.expression.invoke.NumericOperationInvoker;
import se.l4.dust.core.internal.expression.invoke.OrInvoker;
import se.l4.dust.core.internal.expression.invoke.StringConcatInvoker;
import se.l4.dust.core.internal.expression.invoke.TernaryInvoker;
import se.l4.dust.core.internal.expression.invoke.ThisInvoker;
import se.l4.dust.core.internal.expression.invoke.TypeResolving;

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
	private final ResourceLocation source;

	private TypeResolver typeResolver;
	private MemberResolver memberResolver;

	public ExpressionResolver(
			TypeConverter converter,
			ExpressionsImpl expressions,
			ResourceLocation source,
			Map<String, String> namespaces,
			ErrorHandler errors,
			Node root)
	{
		this.converter = converter;
		this.expressions = expressions;
		this.source = source;
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
		return resolveFromRoot(encounter, root, context);
	}

	/**
	 * Resolve the given node against the specified context.
	 * @param encounter
	 *
	 * @param node
	 * @param context
	 * @return
	 */
	private Invoker resolve(EncounterImpl encounter, Node node, Class<?> root, Class<?> context, ResolvedType typeContext, Invoker left)
	{
		encounter.setContext(node, context);
		encounter.increaseLevel();

		Invoker invoker = resolve0(encounter, node, root, context, typeContext, left);

		encounter.decreaseLevel();
		return invoker;
	}

	private Invoker resolveFromRoot(EncounterImpl encounter, Node node, Class<?> root)
	{
		encounter.setContext(node, root);
		int level = encounter.getLevel();
		encounter.setLevel(0);

		Invoker invoker = resolve0(encounter, node, root, root, null, null);

		encounter.setLevel(level);
		return invoker;
	}

	private Invoker resolve0(EncounterImpl encounter, Node node, Class<?> root, Class<?> context, ResolvedType typeContext, Invoker left)
	{
		if(node instanceof IdentifierNode)
		{
			return resolveIdentifier(encounter, (IdentifierNode) node, context, typeContext, left);
		}
		else if(node instanceof ChainNode)
		{
			// Resolve a chain of other nodes
			ChainNode chain = (ChainNode) node;

			Invoker leftInvoker = resolve0(encounter, chain.getLeft(), root, context, typeContext, left);
			Invoker rightInvoker = resolve(encounter, chain.getRight(), root, leftInvoker.getReturnClass(), leftInvoker.getReturnType(), leftInvoker);
			if(rightInvoker instanceof DynamicPropertyInvoker && ! ((DynamicPropertyInvoker) rightInvoker).getProperty().needsContext())
			{
				return rightInvoker;
			}
			else if(rightInvoker instanceof DynamicMethodInvoker && ! ((DynamicMethodInvoker) rightInvoker).getMethod().needsContext())
			{
				return rightInvoker;
			}

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
			Invoker invoker = resolveFromRoot(encounter, nn.getNode(), root);

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
			Invoker leftInvoker = resolveFromRoot(encounter, chain.getLeft(), root);
			Invoker rightInvoker = resolveFromRoot(encounter, chain.getRight(), root);

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
			Invoker leftInvoker = resolveFromRoot(encounter, chain.getLeft(), root);
			Invoker rightInvoker = resolveFromRoot(encounter, chain.getRight(), root);

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
			else if(isNumber(leftInvoker.getReturnClass()) && isNumber(rightInvoker.getReturnClass()))
			{
				return new NumericComparisonInvoker(node, leftInvoker, rightInvoker);
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
			Invoker leftInvoker = resolveFromRoot(encounter, chain.getLeft(), root);
			Invoker rightInvoker = resolveFromRoot(encounter, chain.getRight(), root);

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
			Invoker test = resolveFromRoot(encounter, tn.getTest(), root);
			Invoker ternaryLeft = resolveFromRoot(encounter, tn.getLeft(), root);
			Invoker ternaryRight = tn.getRight() == null ? null : resolveFromRoot(encounter, tn.getRight(), root);

			if(! isBoolean(test.getReturnClass()))
			{
				test = toConverting(test, boolean.class);
			}

			return new TernaryInvoker(node, test, ternaryLeft, ternaryRight);
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
				actualParams[i] = resolveFromRoot(encounter, n, root);
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
					throw errors.error(node, "There are no properties or methods available in namespace " + ns);
				}

				Class[] actualParamTypes = new Class[actualParams.length];
				for(int j=0, n=actualParams.length; j<n; j++)
				{
					actualParamTypes[j] = actualParams[j].getReturnClass();
				}

				DynamicMethod method = source.getMethod(encounter, id.getIdentifier(), actualParamTypes);
				if(method == null)
				{
					throw errors.error(node, "There is no method named " + id.getIdentifier() + " in namespace " + ns + "; Used " + source + " for lookup");
				}

				return createDynamicMethodInvoker(node, method, actualParams);
			}
			else
			{
				DynamicProperty leftProperty = findProperty(left);
				if(leftProperty != null)
				{
					Class[] actualParamTypes = new Class[actualParams.length];
					for(int j=0, n=actualParams.length; j<n; j++)
					{
						actualParamTypes[j] = actualParams[j].getReturnClass();
					}

					DynamicMethod method = leftProperty.getMethod(encounter, id.getIdentifier(), actualParamTypes);
					if(method != null)
					{
						return createDynamicMethodInvoker(node, method, actualParams);
					}
				}

				return resolveMethod(node, id, actualParams, context, typeContext);
			}
		}
		else if(node instanceof AddNode)
		{
			AddNode an = (AddNode) node;
			Invoker addLeft = resolveFromRoot(encounter, an.getLeft(), root);
			Invoker addRight = resolveFromRoot(encounter, an.getRight(), root);

			if(isNumber(addLeft.getReturnClass()) && isNumber(addRight.getReturnClass()))
			{
				boolean floatingPoint = isFloatingPoint(addLeft.getReturnClass())
					|| isFloatingPoint(addRight.getReturnClass());

				return new NumericOperationInvoker(node, addLeft, addRight, floatingPoint);
			}

			return new StringConcatInvoker(node, addLeft, addRight);
		}
		else if(node instanceof SubtractNode || node instanceof DivideNode
				|| node instanceof MultiplyNode || node instanceof ModuloNode)
		{
			LeftRightNode an = (LeftRightNode) node;
			Invoker nodeLeft = resolveFromRoot(encounter, an.getLeft(), root);
			Invoker nodeRight = resolveFromRoot(encounter, an.getRight(), root);

			if(! isNumber(nodeLeft.getReturnClass()))
			{
				nodeLeft = toConverting(nodeLeft, Number.class);
			}

			if(! isNumber(nodeRight.getReturnClass()))
			{
				nodeRight = toConverting(nodeRight, Number.class);
			}

			boolean floatingPoint = isFloatingPoint(nodeLeft.getReturnClass())
				|| isFloatingPoint(nodeRight.getReturnClass());

			return new NumericOperationInvoker(node, nodeLeft, nodeRight, floatingPoint);
		}
		else if(node instanceof IndexNode)
		{
			IndexNode index = (IndexNode) node;

			// Resolve the left node (will become part of the chain)
			Invoker indexLeft = resolve(encounter, index.getLeft(), root, context, typeContext, null);

			Node[] indexes = index.getIndexes();
			for(int i=0, n=indexes.length; i<n; i++)
			{
				Node ni = indexes[i];
				Invoker ii = resolveFromRoot(encounter, ni, root);

				if(Map.class.isAssignableFrom(indexLeft.getReturnClass()))
				{
					// Left is currently a map, create a method invocation
					Invoker invoker = resolveMethod(
						ni,
						new IdentifierNode(0, 0, null, "get"),
						new Invoker[] { ii },
						indexLeft.getReturnClass(),
						indexLeft.getReturnType()
					);

					indexLeft = new ChainInvoker(index, indexLeft, invoker);
				}
				else if(List.class.isAssignableFrom(indexLeft.getReturnClass()))
				{
					Invoker invoker = resolveMethod(
						ni,
						new IdentifierNode(0, 0, null, "get"),
						new Invoker[] { ii },
						indexLeft.getReturnClass(),
						indexLeft.getReturnType()
					);

					indexLeft = new ChainInvoker(index, indexLeft, invoker);
				}
				else if(indexLeft.getReturnClass().isArray())
				{
					if(ii.getReturnClass() != int.class)
					{
						ii = toConverting(ii, int.class);
					}

					indexLeft = new ChainInvoker(index, indexLeft,
						new ArrayIndexInvoker(node, indexLeft.getReturnClass().getComponentType(), ii)
					);
				}
				else
				{
					throw errors.error(ni, "Return type is not a map, list or an array. Type is " + indexLeft.getReturnClass());
				}
			}

			return indexLeft;
		}
		else if(node instanceof ArrayNode)
		{
			ArrayNode array = (ArrayNode) node;
			Node[] nodes = array.getValues();

			Invoker[] invokers = new Invoker[nodes.length];
			List<ResolvedType> types = new ArrayList<ResolvedType>();

			for(int i=0, n=invokers.length; i<n; i++)
			{
				invokers[i] = resolveFromRoot(encounter, nodes[i], root);
				ResolvedType rt = invokers[i].getReturnType();
				types.add(rt == null ? typeResolver.resolve(invokers[i].getReturnClass()) : rt);
			}

			List<ResolvedType> common = TypeResolving.findCommonTypes(types);

			// TODO: Better selection than just picking the first one
			return new ArrayInvoker(
				node,
				common.isEmpty() ? Object.class : common.get(0).getErasedType(),
				invokers
			);
		}

		throw errors.error(node, "Unknown node of type: " + node.getClass());
	}

	private Invoker createDynamicMethodInvoker(Node node, DynamicMethod method, Invoker[] actualParams)
	{
		Invoker[] params = createInvokers(actualParams, method.getParametersType());
		if(params == null)
		{
			throw errors.error(node, "Could not convert parameters into " + Arrays.toString(method.getParametersType()));
		}

		return new DynamicMethodInvoker(node, method, params);
	}

	private Invoker toConverting(Invoker invoker, Class<?> output)
	{
		if(output == int.class && invoker.getReturnClass() == long.class)
		{
			return new LongToIntInvoker(invoker.getNode(), invoker);
		}

		Class<?> in = invoker.getReturnClass();
		if(converter.canConvertBetween(in, output))
		{
			NonGenericConversion<?, ?> conversion = converter.getConversion(in, output);
			return new ConvertingInvoker(invoker.getNode(), conversion, output, invoker);
		}

		// No suitable conversion, throw error
		throw errors.error(invoker.getNode(), "Expected type " + output
			+ ", but got " + invoker.getReturnClass()
			+ " with no way of converting to " + output.getSimpleName());
	}

	private Invoker toDynamcConverting(Invoker invoker, Class<?> output)
	{
		return new DynamicConversionInvoker(converter, invoker.getNode(), output, invoker);
	}

	private boolean isBoolean(Class<?> type)
	{
		return type == Boolean.class || type == boolean.class;
	}

	private boolean isNumber(Class<?> type)
	{
		return Number.class.isAssignableFrom(Primitives.wrap(type));
	}

	private boolean isFloatingPoint(Class<?> type)
	{
		Class<?> wrapped = Primitives.wrap(type);
		return Float.class.isAssignableFrom(wrapped) || Double.class.isAssignableFrom(wrapped);
	}

	/**
	 * Resolve an identifier based on the specified context. This will look
	 * for a getter and return a {@link MethodPropertyInvoker} if a suitable
	 * one is found.
	 *
	 * @param node
	 * @param classContext
	 * @return
	 */
	private Invoker resolveIdentifier(EncounterImpl encounter, IdentifierNode node, Class<?> classContext, ResolvedType typeContext, Invoker left)
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
				throw errors.error(node, "There are no properties or methods available in namespace " + ns);
			}

			DynamicProperty property = source.getProperty(encounter, node.getIdentifier());
			if(property == null)
			{
				throw errors.error(node, "There is no property named " + node.getIdentifier() + " in namespace " + ns + "; Used " + source + " for lookup");
			}

			return new DynamicPropertyInvoker(node, property);
		}

		// First try resolving this as a custom property
		DynamicProperty leftProperty = findProperty(left);
		if(leftProperty != null)
		{
			DynamicProperty property = leftProperty.getProperty(encounter, node.getIdentifier());
			if(property != null)
			{
				return new DynamicPropertyInvoker(node, property);
			}
		}

		// Then go through properties
		ResolvedType type = typeContext == null ? typeResolver.resolve(classContext) : typeContext;
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

			if(name.equals(node.getIdentifier())
				|| CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name).equals(node.getIdentifier()))
			{
				Method setter = null;
				try
				{
					setter = classContext.getMethod("set" + name, m.getReturnType());
				}
				catch(SecurityException e)
				{
					// Ignore, not all getters have setters
				}
				catch(NoSuchMethodException e)
				{
					// Ignore, not all getters have setters
				}

				return new MethodPropertyInvoker(
					node,
					rm.getReturnType(),
					m,
					setter
				);
			}
		}

		// Go through fields and expose public members
		for(ResolvedField rf : members.getMemberFields())
		{
			Field field = rf.getRawMember();
			if(! field.getName().equals(node.getIdentifier())) continue;

			if(Modifier.isPublic(field.getModifiers())
				|| field.isAnnotationPresent(Expose.class))
			{
				return new FieldPropertyInvoker(
					node,
					rf.getType(),
					field
				);
			}
		}

		throw errors.error(node, "Unable to find a suitable getter or field for '" + node.toHumanReadable()  + "' in " + classContext);
	}

	private DynamicProperty findProperty(Invoker left)
	{
		if(left instanceof ChainInvoker)
		{
			return findProperty(((ChainInvoker) left).getRight());
		}
		else if(left instanceof DynamicPropertyInvoker)
		{
			return ((DynamicPropertyInvoker) left).getProperty();
		}

		return null;
	}

	private Invoker resolveMethod(Node node, IdentifierNode id, Invoker[] actualParams, Class<?> context, ResolvedType typeContext)
	{
		// First pass: Look for exact matches
		if(typeContext instanceof ResolvedRecursiveType)
		{
			typeContext = typeResolver.resolve(typeContext.getTypeBindings(), typeContext.getErasedType());
		}

		ResolvedType type = typeContext == null ? typeResolver.resolve(context) : typeContext;
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
				if((actualParams[i].getReturnClass() == void.class || actualParams[i].getReturnClass() == Void.class)
					&& ! types[i].isPrimitive())
				{
					// Do nothing, input is null and non-primitive input
				}
				else if(! types[i].isAssignableFrom(actualParams[i].getReturnClass()))
				{
					// Not directly assignable, continue
					continue _outer;
				}
			}

			return new MethodInvoker(node, rm.getReturnType(), m, actualParams);
		}

		// Second pass: Try to convert params
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
			Invoker[] newParams = createInvokers(actualParams, types);
			if(newParams == null) continue;

			return new MethodInvoker(node, rm.getReturnType(), m, newParams);
		}

		StringBuilder builder = new StringBuilder()
			.append("No matching method found, looked in ")
			.append(context.getName())
			.append(". Parameter types were: ");

		if(actualParams.length == 0)
		{
			builder.append("[]");
		}
		else
		{
			builder.append("[");
			for(int i=0, n=actualParams.length; i<n; i++)
			{
				if(i > 0) builder.append(", ");

				builder.append(actualParams[i].getReturnClass().getName());
			}
			builder.append("]");
		}

		builder.append("\n\nResolved the methods on ")
			.append(type.getFullDescription())
			.append(":");

		for(ResolvedMethod rm : members.getMemberMethods())
		{
			builder.append("\n");

			Method m = rm.getRawMember();
			builder.append(rm.getName())
				.append("(");

			Class<?>[] types = m.getParameterTypes();
			for(int i=0, n=types.length; i<n; i++)
			{
				if(i > 0) builder.append(", ");

				builder.append(types[i].getName());
			}

			builder.append(")");
		}

		throw errors.error(node, builder.toString());
	}

	private Invoker[] createInvokers(Invoker[] actualParams, Class<?>[] types)
	{
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
			else if((actualParams[i].getReturnClass() == void.class || actualParams[i].getReturnClass() == Void.class)
					&& ! types[i].isPrimitive())
			{
				newParams[i] = actualParams[i];
			}
			else if(actualParams[i].getReturnClass() == Object.class)
			{
				// TODO: Variant return class?
				newParams[i] = toDynamcConverting(actualParams[i], types[i]);
			}
			else
			{
				// No conversion found, skip this method
				return null;
			}
		}
		return newParams;
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

		@Override
		public ResourceLocation getSource()
		{
			return source;
		}

		public int getLevel()
		{
			return level;
		}

		public void setLevel(int level)
		{
			this.level = level;
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
