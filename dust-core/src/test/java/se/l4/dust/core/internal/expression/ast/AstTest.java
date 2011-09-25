package se.l4.dust.core.internal.expression.ast;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import se.l4.dust.core.internal.expression.ExpressionParser;

/**
 * Tests that parses expressions and verifies that they match the expected
 * AST.
 * 
 * @author Andreas Holstenson
 *
 */
public class AstTest
{
	@Test
	public void testTrue()
	{
		test("true", keyword(KeywordNode.Type.TRUE));
	}
	
	@Test
	public void testFalse()
	{
		test("false", keyword(KeywordNode.Type.FALSE));
	}
	
	@Test
	public void testNull()
	{
		test("null", keyword(KeywordNode.Type.NULL));
	}
	
	@Test
	public void testThis()
	{
		test("this", keyword(KeywordNode.Type.THIS));
	}
	
	@Test
	public void testString()
	{
		test("'string'", string("string"));
	}
	
	@Test
	public void testStringWithEscape()
	{
		test("'s\\\\tring'", string("s\\tring"));
		test("'s\\u1020tring'", string("s\u1020tring"));
		test("'s\\ntring'", string("s\ntring"));
		test("'s\\rtring'", string("s\rtring"));
		test("'s\\ftring'", string("s\ftring"));
		test("'s\\btring'", string("s\btring"));
	}
	
	@Test
	public void testLong()
	{
		test("1200", longNode(1200));
		test("0", longNode(0));
		test("-2", longNode(-2));
		test("+450", longNode(450));
	}
	
	@Test
	public void testDouble()
	{
		test("1200.0", doubleNode(1200));
		test(".0", doubleNode(0));
		test(".2", doubleNode(0.2));
		test(".2e2", doubleNode(20));
		test("-200.0", doubleNode(-200));
		test("20.23", doubleNode(20.23));
		test("+20.23", doubleNode(20.23));
	}
	
	@Test
	public void testSingleIdentifier()
	{
		test("id", id(null, "id"));
	}
	
	@Test
	public void testNamespacedIdentifier()
	{
		test("ns:id", id("ns", "id"));
	}
	
	@Test
	public void testMethod()
	{
		test("method()", invoke(id(null, "method"), Collections.<Node>emptyList()));
	}
	
	@Test
	public void testNamespacedMethod()
	{
		test("ns:method()", invoke(id("ns", "method"), Collections.<Node>emptyList()));
	}
	
	@Test
	public void testMethodWithParams()
	{
		test("method(p1, p2)", invoke(
			id(null, "method"), 
			Arrays.<Node>asList(
				id(null, "p1"),
				id(null, "p2")
			)
		));
	}
	
	@Test
	public void testMethodWithParams2()
	{
		test("method('string', 12)", invoke(
			id(null, "method"), 
			Arrays.<Node>asList(
				string("string"),
				longNode(12)
			)
		));
	}
	
	@Test
	public void testSimpleChain()
	{
		test("prop1.prop2", chain(
			id(null, "prop1"), 
			id(null, "prop2"))
		);
	}
	
	@Test
	public void testNegate()
	{
		test("! id", negate(id(null, "id")));
		test("! 12", negate(longNode(12)));
		test("! method()", negate(invoke(
			id(null, "method"), 
			Collections.<Node>emptyList()))
		);
	}
	
	@Test
	public void testSignPlus()
	{
		test("+id", sign(false, id(null, "id")));
		test("+ns:id", sign(false, id("ns", "id")));
		
		test("+method()", sign(false, invoke(
			id(null, "method"),
			Collections.<Node>emptyList()
		)));
	}
	
	@Test
	public void testSignNegative()
	{
		test("-id", sign(true, id(null, "id")));
		test("-ns:id", sign(true, id("ns", "id")));
		
		test("-method()", sign(true, invoke(
			id(null, "method"),
			Collections.<Node>emptyList()
		)));
	}
	
	@Test
	public void testSignMultiple()
	{
		test("-+id", sign(true, sign(false, id(null, "id"))));
		test("-+ns:id", sign(true, sign(false, id("ns", "id"))));
		
		test("-+method()", sign(true, sign(false, invoke(
			id(null, "method"),
			Collections.<Node>emptyList()
		))));
	}
	
	@Test
	public void testSubtraction()
	{
		test("12 - 20", subtract(
			longNode(12),
			longNode(20)
		));
		
		test("23.0-20", subtract(
			doubleNode(23),
			longNode(20)
		));
		
		test("id - 20", subtract(
			id(null, "id"),
			longNode(20)
		));
	}
	
	@Test
	public void testAddition()
	{
		test("12 + 20", add(
			longNode(12),
			longNode(20)
		));
		
		test("23.0+20", add(
			doubleNode(23),
			longNode(20)
		));
		
		test("id + 20", add(
			id(null, "id"),
			longNode(20)
		));
	}
	
	@Test
	public void testMultiplication()
	{
		test("12 * 20", multiply(
			longNode(12),
			longNode(20)
		));
		
		test("23.0*20", multiply(
			doubleNode(23),
			longNode(20)
		));
		
		test("id * 20", multiply(
			id(null, "id"),
			longNode(20)
		));
	}
	
	@Test
	public void testDivide()
	{
		test("12 / 20", divide(
			longNode(12),
			longNode(20)
		));
		
		test("23.0/20", divide(
			doubleNode(23),
			longNode(20)
		));
		
		test("id / 20", divide(
			id(null, "id"),
			longNode(20)
		));
	}
	
	@Test
	public void testModulo()
	{
		test("12 % 20", modulo(
			longNode(12),
			longNode(20)
		));
		
		test("23.0%20", modulo(
			doubleNode(23),
			longNode(20)
		));
		
		test("id % 20", modulo(
			id(null, "id"),
			longNode(20)
		));
	}
	
	@Test
	public void testCalculationOrder()
	{
		test("12 * 20 + 4", add(
			multiply(longNode(12), longNode(20)),
			longNode(4)
		));
		
		test("12 * 20 + 4 / 4", add(
			multiply(longNode(12), longNode(20)),
			divide(longNode(4), longNode(4))
		));
		
		test("12 * 20 - 4", subtract(
			multiply(longNode(12), longNode(20)),
			longNode(4)
		));
		
		test("12 * 20 - 4 / 4", subtract(
			multiply(longNode(12), longNode(20)),
			divide(longNode(4), longNode(4))
		));
		
		test("12 * (20 + 4)", multiply(
			longNode(12),
			add(longNode(20), longNode(4))
		));
		
		test("2 - 20 + 4", add(
			subtract(longNode(2), longNode(20)),
			longNode(4)
		));
		
		test("2 + 20 + 4", add(
			add(longNode(2), longNode(20)),
			longNode(4)
		));
	}
	
	@Test
	public void testOr()
	{
		test("t1 || t2", or(
			id(null, "t1"),
			id(null, "t2")
		));
		
		test("t1 || t2 || t3", or(
			or(id(null, "t1"), id(null, "t2")),
			id(null, "t3")
		));
		
		test("12 || t2", or(
			longNode(12),
			id(null, "t2")
		));
		
		test("t1 or t2", or(
			id(null, "t1"),
			id(null, "t2")
		));
	}
	
	@Test
	public void testAnd()
	{
		test("t1 && t2", and(
			id(null, "t1"),
			id(null, "t2")
		));
		
		test("t1 && t2 && t3", and(
			and(id(null, "t1"), id(null, "t2")),
			id(null, "t3")
		));
		
		test("12 && t2", and(
			longNode(12),
			id(null, "t2")
		));
		
		test("t1 and t2", and(
			id(null, "t1"),
			id(null, "t2")
		));
	}
	
	@Test
	public void testBooleanOrder()
	{
		test("t1 && t2 || t3", or(
			and(id(null, "t1"), id(null, "t2")),
			id(null, "t3")
		));
		
		test("(t1 || t2) && t3", and(
			or(id(null, "t1"), id(null, "t2")),
			id(null, "t3")
		));
		
		test("t1 || t2 && t3", or(
			id(null, "t1"),
			and(id(null, "t2"), id(null, "t3"))
		));
		
		test("t1 || (t2 && t3)", or(
			id(null, "t1"),
			and(id(null, "t2"), id(null, "t3"))
		));
	}
	
	@Test
	public void testEquals()
	{
		test("t1 == 12", equals(
			id(null, "t1"),
			longNode(12)
		));
		
		test("t1 == 12 == t2", equals(
			equals(
				id(null, "t1"),
				longNode(12)
			),
			id(null, "t2")
		));
	}
	
	@Test
	public void testNotEquals()
	{
		test("t1 != 12", notEquals(
			id(null, "t1"),
			longNode(12)
		));
		
		test("t1 != 12 != t2", notEquals(
			notEquals(
				id(null, "t1"),
				longNode(12)
			),
			id(null, "t2")
		));
	}
	
	@Test
	public void testLessThan()
	{
		test("t1 < 12", lessThan(
			id(null, "t1"),
			longNode(12)
		));
		
		test("t1 < 12 < t2", lessThan(
			lessThan(
				id(null, "t1"),
				longNode(12)
			),
			id(null, "t2")
		));
	}
	
	@Test
	public void testLessOrEqual()
	{
		test("t1 <= 12", lessThanOrEqual(
			id(null, "t1"),
			longNode(12)
		));
		
		test("t1 <= 12 <= t2", lessThanOrEqual(
			lessThanOrEqual(
				id(null, "t1"),
				longNode(12)
			),
			id(null, "t2")
		));
	}
	
	@Test
	public void testGreaterThan()
	{
		test("t1 > 12", greaterThan(
			id(null, "t1"),
			longNode(12)
		));
		
		test("t1 > 12 > t2", greaterThan(
			greaterThan(
				id(null, "t1"),
				longNode(12)
			),
			id(null, "t2")
		));
	}
	
	@Test
	public void testGreaterOrEqual()
	{
		test("t1 >= 12", greaterThanOrEqual(
			id(null, "t1"),
			longNode(12)
		));
		
		test("t1 >= 12 >= t2", greaterThanOrEqual(
			greaterThanOrEqual(
				id(null, "t1"),
				longNode(12)
			),
			id(null, "t2")
		));
	}
	
	@Test
	public void testMixedConditions()
	{
		test("t1 >= 12 <= t2", lessThanOrEqual(
			greaterThanOrEqual(
				id(null, "t1"),
				longNode(12)
			),
			id(null, "t2")
		));
	}
	
	@Test
	public void testTernary()
	{
		test("test ? yes : no", ternary(
			id(null, "test"),
			id(null, "yes"),
			id(null, "no")
		));
		
		test("test ? yes", ternary(
			id(null, "test"),
			id(null, "yes"),
			null
		));
	}
	
	@Test
	public void testIndex()
	{
		test("test[0]", index(id(null, "test"), longNode(0)));
	}
	
	@Test
	public void testMultipleIndexes()
	{
		test("test[0]['red']", index(id(null, "test"), longNode(0), string("red")));
	}
	
	private IdentifierNode id(String ns, String id)
	{
		return new IdentifierNode(0, 0, ns, id);
	}
	
	private TernaryNode ternary(Node test, Node left, Node right)
	{
		return new TernaryNode(0, 0, test, left, right);
	}
	
	private Node keyword(KeywordNode.Type type)
	{
		return new KeywordNode(0, 0, type);
	}
	
	private Node chain(Node left, Node right)
	{
		return new ChainNode(0, 0, left, right);
	}
	
	private Node add(Node left, Node right)
	{
		return new AddNode(0, 0, left, right);
	}
	
	private Node subtract(Node left, Node right)
	{
		return new SubtractNode(0, 0, left, right);
	}
	
	private Node doubleNode(double v)
	{
		return new DoubleNode(0, 0, v);
	}
	
	private Node longNode(long v)
	{
		return new LongNode(0, 0, v);
	}
	
	private Node lessThan(Node left, Node right)
	{
		return new LessNode(0, 0, left, right);
	}
	
	private Node lessThanOrEqual(Node left, Node right)
	{
		return new LessOrEqualNode(0, 0, left, right);
	}
	
	private Node greaterThan(Node left, Node right)
	{
		return new GreaterNode(0, 0, left, right);
	}
	
	private Node greaterThanOrEqual(Node left, Node right)
	{
		return new GreaterOrEqualNode(0, 0, left, right);
	}
	
	private Node divide(Node left, Node right)
	{
		return new DivideNode(0, 0, left, right);
	}
	
	private Node multiply(Node left, Node right)
	{
		return new MultiplyNode(0, 0, left, right);
	}
	
	private Node or(Node left, Node right)
	{
		return new OrNode(0, 0, left, right);
	}
	
	private Node and(Node left, Node right)
	{
		return new AndNode(0, 0, left, right);
	}
	
	private Node string(String v)
	{
		return new StringNode(0, 0, v);
	}
	
	private Node invoke(IdentifierNode id, List<Node> params)
	{
		return new InvokeNode(0, 0, id, params);
	}
	
	private Node negate(Node other)
	{
		return new NegateNode(0, 0, other);
	}
	
	private Node sign(boolean negative, Node other)
	{
		return new SignNode(0, 0, negative, other);
	}
	
	private Node modulo(Node left, Node right)
	{
		return new ModuloNode(0, 0, left, right);
	}
	
	private Node equals(Node left, Node right)
	{
		return new EqualsNode(0, 0, left, right);
	}
	
	private Node notEquals(Node left, Node right)
	{
		return new NotEqualsNode(0, 0, left, right);
	}
	
	private Node index(Node left, Node... indexes)
	{
		return new IndexNode(0, 0, left, indexes);
	}
	
	private static void test(String expr, Node expectedResult)
	{
		Node root = ExpressionParser.parse(expr);
			
		Assert.assertEquals("Nodes should be equal", expectedResult, root);
	}
}
