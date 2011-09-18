package se.l4.dust.jrebel;

import org.zeroturnaround.bundled.javassist.ClassPool;
import org.zeroturnaround.bundled.javassist.CtClass;
import org.zeroturnaround.bundled.javassist.CtMethod;
import org.zeroturnaround.javarebel.integration.support.JavassistClassBytecodeProcessor;

/**
 * Bytecode processor for CrayonBinder. As all contributions in Dust go
 * through the binder we can enhance it so that all modules are registered
 * for change checking.
 * 
 * @author Andreas Holstenson
 *
 */
public class CrayonBinderCBP
	extends JavassistClassBytecodeProcessor
{

	@Override
	public void process(ClassPool cp, ClassLoader loader, CtClass ctClass)
		throws Exception
	{
		CtMethod module = ctClass.getMethod("module", "(Ljava/lang/Object;)V");
		module.insertAfter(
			"se.l4.dust.jrebel.ModuleReloader.add($1);"
		);
	}

}
