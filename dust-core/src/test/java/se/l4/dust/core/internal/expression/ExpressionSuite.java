package se.l4.dust.core.internal.expression;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import se.l4.dust.core.internal.expression.ast.AstTest;
import se.l4.dust.core.internal.expression.compiler.CompilerTest;
import se.l4.dust.core.internal.expression.resolver.DebuggerTest;
import se.l4.dust.core.internal.expression.resolver.ResolverTest;

/**
 * Suite for running all expression tests.
 * 
 * @author Andreas Holstenson
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
	AstTest.class,
	ResolverTest.class,
	DebuggerTest.class,
	CompilerTest.class,
	
	CommonSourceTest.class,
	VarPropertySourceTest.class
})
public class ExpressionSuite
{

}
