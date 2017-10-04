package se.l4.dust.core.internal.expression.ast;

import se.l4.dust.api.expression.ExpressionException;
import se.l4.dust.core.internal.expression.ExpressionParser;

/**
 * Simple class with main method for running a few invalid expressions. These
 * should all fail and produce somewhat understandable error messages.
 *
 * @author Andreas Holstenson
 *
 */
public class ParserFailures
{
	public static void main(String[] args)
	{
		test("a.12");
		test("a.b.12");
		test("a(k");
		test("a(k,");
		test("a(k,c).");
		test("-12 | k");
		test("&&");
		test("red ? ");
		test("red ? test : ");
		test("red ? : ");
		test("m.me())");
		test("m.me().12");
		test("'m'c");
		test("! /");
		test(".2e");
		test("2.kaka");
	}

	private static void test(String text)
	{
		try
		{
			ExpressionParser.parse(text);
			System.out.println("No error for: " + text);
		}
		catch(ExpressionException e)
		{
			System.out.println(e.getMessage());
		}

		System.out.println("==================");
	}
}
