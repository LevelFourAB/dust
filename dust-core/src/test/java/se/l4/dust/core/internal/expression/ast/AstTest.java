package se.l4.dust.core.internal.expression.ast;

import java.util.Arrays;
import java.util.Collections;

import org.antlr.runtime.RecognitionException;
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
		test("true", new KeywordNode(KeywordNode.Type.TRUE));
	}
	
	@Test
	public void testFalse()
	{
		test("false", new KeywordNode(KeywordNode.Type.FALSE));
	}
	
	@Test
	public void testNull()
	{
		test("null", new KeywordNode(KeywordNode.Type.NULL));
	}
	
	@Test
	public void testThis()
	{
		test("this", new KeywordNode(KeywordNode.Type.THIS));
	}
	
	@Test
	public void testString()
	{
		test("'string'", new StringNode("string"));
	}
	
	@Test
	public void testStringWithEscape()
	{
		test("'s\\\\tring'", new StringNode("s\\tring"));
		test("'s\\u1020tring'", new StringNode("s\u1020tring"));
		test("'s\\ntring'", new StringNode("s\ntring"));
		test("'s\\rtring'", new StringNode("s\rtring"));
		test("'s\\ftring'", new StringNode("s\ftring"));
		test("'s\\btring'", new StringNode("s\btring"));
	}
	
	@Test
	public void testLong()
	{
		test("1200", new LongNode(1200));
		test("0", new LongNode(0));
		test("-2", new LongNode(-2));
		test("+450", new LongNode(450));
	}
	
	@Test
	public void testDouble()
	{
		test("1200.0", new DoubleNode(1200));
		test(".0", new DoubleNode(0));
		test(".2", new DoubleNode(0.2));
		test(".2e2", new DoubleNode(20));
		test("-200.0", new DoubleNode(-200));
		test("20.23", new DoubleNode(20.23));
		test("+20.23", new DoubleNode(20.23));
	}
	
	@Test
	public void testSingleIdentifier()
	{
		test("id", new IdentifierNode(null, "id"));
	}
	
	@Test
	public void testNamespacedIdentifier()
	{
		test("ns:id", new IdentifierNode("ns", "id"));
	}
	
	@Test
	public void testMethod()
	{
		test("method()", new InvokeNode(new IdentifierNode(null, "method"), Collections.<Node>emptyList()));
	}
	
	@Test
	public void testNamespacedMethod()
	{
		test("ns:method()", new InvokeNode(new IdentifierNode("ns", "method"), Collections.<Node>emptyList()));
	}
	
	@Test
	public void testMethodWithParams()
	{
		test("method(p1, p2)", new InvokeNode(
			new IdentifierNode(null, "method"), 
			Arrays.<Node>asList(
				new IdentifierNode(null, "p1"),
				new IdentifierNode(null, "p2")
			)
		));
	}
	
	@Test
	public void testMethodWithParams2()
	{
		test("method('string', 12)", new InvokeNode(
			new IdentifierNode(null, "method"), 
			Arrays.<Node>asList(
				new StringNode("string"),
				new LongNode(12)
			)
		));
	}
	
	@Test
	public void testSimpleChain()
	{
		test("prop1.prop2", new ChainNode(
			new IdentifierNode(null, "prop1"), 
			new IdentifierNode(null, "prop2"))
		);
	}
	
	@Test
	public void testNegate()
	{
		test("! id", new NegateNode(new IdentifierNode(null, "id")));
		test("! 12", new NegateNode(new LongNode(12)));
		test("! method()", new NegateNode(new InvokeNode(
			new IdentifierNode(null, "method"), 
			Collections.<Node>emptyList()))
		);
	}
	
	@Test
	public void testSignPlus()
	{
		test("+id", new SignNode(false, new IdentifierNode(null, "id")));
		test("+ns:id", new SignNode(false, new IdentifierNode("ns", "id")));
		
		test("+method()", new SignNode(false, new InvokeNode(
			new IdentifierNode(null, "method"),
			Collections.<Node>emptyList()
		)));
	}
	
	@Test
	public void testSignNegative()
	{
		test("-id", new SignNode(true, new IdentifierNode(null, "id")));
		test("-ns:id", new SignNode(true, new IdentifierNode("ns", "id")));
		
		test("-method()", new SignNode(true, new InvokeNode(
			new IdentifierNode(null, "method"),
			Collections.<Node>emptyList()
		)));
	}
	
	@Test
	public void testSignMultiple()
	{
		test("-+id", new SignNode(true, new SignNode(false, new IdentifierNode(null, "id"))));
		test("-+ns:id", new SignNode(true, new SignNode(false, new IdentifierNode("ns", "id"))));
		
		test("-+method()", new SignNode(true, new SignNode(false, new InvokeNode(
			new IdentifierNode(null, "method"),
			Collections.<Node>emptyList()
		))));
	}
	
	@Test
	public void testSubtraction()
	{
		test("12 - 20", new SubtractNode(
			new LongNode(12),
			new LongNode(20)
		));
		
		test("23.0-20", new SubtractNode(
			new DoubleNode(23),
			new LongNode(20)
		));
		
		test("id - 20", new SubtractNode(
			new IdentifierNode(null, "id"),
			new LongNode(20)
		));
	}
	
	@Test
	public void testAddition()
	{
		test("12 + 20", new AddNode(
			new LongNode(12),
			new LongNode(20)
		));
		
		test("23.0+20", new AddNode(
			new DoubleNode(23),
			new LongNode(20)
		));
		
		test("id + 20", new AddNode(
			new IdentifierNode(null, "id"),
			new LongNode(20)
		));
	}
	
	@Test
	public void testMultiplication()
	{
		test("12 * 20", new MultiplyNode(
			new LongNode(12),
			new LongNode(20)
		));
		
		test("23.0*20", new MultiplyNode(
			new DoubleNode(23),
			new LongNode(20)
		));
		
		test("id * 20", new MultiplyNode(
			new IdentifierNode(null, "id"),
			new LongNode(20)
		));
	}
	
	@Test
	public void testDivide()
	{
		test("12 / 20", new DivideNode(
			new LongNode(12),
			new LongNode(20)
		));
		
		test("23.0/20", new DivideNode(
			new DoubleNode(23),
			new LongNode(20)
		));
		
		test("id / 20", new DivideNode(
			new IdentifierNode(null, "id"),
			new LongNode(20)
		));
	}
	
	@Test
	public void testModulo()
	{
		test("12 % 20", new ModuloNode(
			new LongNode(12),
			new LongNode(20)
		));
		
		test("23.0%20", new ModuloNode(
			new DoubleNode(23),
			new LongNode(20)
		));
		
		test("id % 20", new ModuloNode(
			new IdentifierNode(null, "id"),
			new LongNode(20)
		));
	}
	
	@Test
	public void testCalculationOrder()
	{
		test("12 * 20 + 4", new AddNode(
			new MultiplyNode(new LongNode(12), new LongNode(20)),
			new LongNode(4)
		));
		
		test("12 * 20 + 4 / 4", new AddNode(
			new MultiplyNode(new LongNode(12), new LongNode(20)),
			new DivideNode(new LongNode(4), new LongNode(4))
		));
		
		test("12 * 20 - 4", new SubtractNode(
			new MultiplyNode(new LongNode(12), new LongNode(20)),
			new LongNode(4)
		));
		
		test("12 * 20 - 4 / 4", new SubtractNode(
			new MultiplyNode(new LongNode(12), new LongNode(20)),
			new DivideNode(new LongNode(4), new LongNode(4))
		));
		
		test("12 * (20 + 4)", new MultiplyNode(
			new LongNode(12),
			new AddNode(new LongNode(20), new LongNode(4))
		));
		
		test("2 - 20 + 4", new AddNode(
			new SubtractNode(new LongNode(2), new LongNode(20)),
			new LongNode(4)
		));
		
		test("2 + 20 + 4", new AddNode(
			new AddNode(new LongNode(2), new LongNode(20)),
			new LongNode(4)
		));
	}
	
	@Test
	public void testOr()
	{
		test("t1 || t2", new OrNode(
			new IdentifierNode(null, "t1"),
			new IdentifierNode(null, "t2")
		));
		
		test("t1 || t2 || t3", new OrNode(
			new OrNode(new IdentifierNode(null, "t1"), new IdentifierNode(null, "t2")),
			new IdentifierNode(null, "t3")
		));
		
		test("12 || t2", new OrNode(
			new LongNode(12),
			new IdentifierNode(null, "t2")
		));
		
		test("t1 or t2", new OrNode(
			new IdentifierNode(null, "t1"),
			new IdentifierNode(null, "t2")
		));
	}
	
	@Test
	public void testAnd()
	{
		test("t1 && t2", new AndNode(
			new IdentifierNode(null, "t1"),
			new IdentifierNode(null, "t2")
		));
		
		test("t1 && t2 && t3", new AndNode(
			new AndNode(new IdentifierNode(null, "t1"), new IdentifierNode(null, "t2")),
			new IdentifierNode(null, "t3")
		));
		
		test("12 && t2", new AndNode(
			new LongNode(12),
			new IdentifierNode(null, "t2")
		));
		
		test("t1 and t2", new AndNode(
			new IdentifierNode(null, "t1"),
			new IdentifierNode(null, "t2")
		));
	}
	
	@Test
	public void testBooleanOrder()
	{
		test("t1 && t2 || t3", new OrNode(
			new AndNode(new IdentifierNode(null, "t1"), new IdentifierNode(null, "t2")),
			new IdentifierNode(null, "t3")
		));
		
		test("(t1 || t2) && t3)", new AndNode(
			new OrNode(new IdentifierNode(null, "t1"), new IdentifierNode(null, "t2")),
			new IdentifierNode(null, "t3")
		));
		
		test("t1 || t2 && t3", new OrNode(
			new IdentifierNode(null, "t1"),
			new AndNode(new IdentifierNode(null, "t2"), new IdentifierNode(null, "t3"))
		));
		
		test("t1 || (t2 && t3)", new OrNode(
			new IdentifierNode(null, "t1"),
			new AndNode(new IdentifierNode(null, "t2"), new IdentifierNode(null, "t3"))
		));
	}
	
	@Test
	public void testEquals()
	{
		test("t1 == 12", new EqualsNode(
			new IdentifierNode(null, "t1"),
			new LongNode(12)
		));
		
		test("t1 == 12 == t2", new EqualsNode(
			new EqualsNode(
				new IdentifierNode(null, "t1"),
				new LongNode(12)
			),
			new IdentifierNode(null, "t2")
		));
	}
	
	@Test
	public void testNotEquals()
	{
		test("t1 != 12", new NotEqualsNode(
			new IdentifierNode(null, "t1"),
			new LongNode(12)
		));
		
		test("t1 != 12 != t2", new NotEqualsNode(
			new NotEqualsNode(
				new IdentifierNode(null, "t1"),
				new LongNode(12)
			),
			new IdentifierNode(null, "t2")
		));
	}
	
	@Test
	public void testLessThan()
	{
		test("t1 < 12", new LessNode(
			new IdentifierNode(null, "t1"),
			new LongNode(12)
		));
		
		test("t1 < 12 < t2", new LessNode(
			new LessNode(
				new IdentifierNode(null, "t1"),
				new LongNode(12)
			),
			new IdentifierNode(null, "t2")
		));
	}
	
	@Test
	public void testLessOrEqual()
	{
		test("t1 <= 12", new LessOrEqualNode(
			new IdentifierNode(null, "t1"),
			new LongNode(12)
		));
		
		test("t1 <= 12 <= t2", new LessOrEqualNode(
			new LessOrEqualNode(
				new IdentifierNode(null, "t1"),
				new LongNode(12)
			),
			new IdentifierNode(null, "t2")
		));
	}
	
	@Test
	public void testGreaterThan()
	{
		test("t1 > 12", new GreaterNode(
			new IdentifierNode(null, "t1"),
			new LongNode(12)
		));
		
		test("t1 > 12 > t2", new GreaterNode(
			new GreaterNode(
				new IdentifierNode(null, "t1"),
				new LongNode(12)
			),
			new IdentifierNode(null, "t2")
		));
	}
	
	@Test
	public void testGreaterOrEqual()
	{
		test("t1 >= 12", new GreaterOrEqualNode(
			new IdentifierNode(null, "t1"),
			new LongNode(12)
		));
		
		test("t1 >= 12 >= t2", new GreaterOrEqualNode(
			new GreaterOrEqualNode(
				new IdentifierNode(null, "t1"),
				new LongNode(12)
			),
			new IdentifierNode(null, "t2")
		));
	}
	
	@Test
	public void testMixedConditions()
	{
		test("t1 >= 12 <= t2", new LessOrEqualNode(
			new GreaterOrEqualNode(
				new IdentifierNode(null, "t1"),
				new LongNode(12)
			),
			new IdentifierNode(null, "t2")
		));
	}
	
	@Test
	public void testTernary()
	{
		test("test ? yes : no", new TernaryNode(
			new IdentifierNode(null, "test"),
			new IdentifierNode(null, "yes"),
			new IdentifierNode(null, "no")
		));
		
		test("test ? yes", new TernaryNode(
			new IdentifierNode(null, "test"),
			new IdentifierNode(null, "yes"),
			null
		));
	}
	
	private static void test(String expr, Node expectedResult)
	{
		try
		{
			Node root = ExpressionParser.parse(expr);
			
			Assert.assertEquals("Nodes should be equal", expectedResult, root);
		}
		catch(RecognitionException e)
		{
			Assert.fail("Unable to parse the expression: " + e.getMessage());
		}
	}
}
