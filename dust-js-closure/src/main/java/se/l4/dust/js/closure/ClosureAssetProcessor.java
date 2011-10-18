package se.l4.dust.js.closure;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSSourceFile;
import com.google.javascript.jscomp.Result;

import se.l4.dust.api.asset.AssetProcessor;
import se.l4.dust.api.resource.MemoryResource;
import se.l4.dust.api.resource.Resource;

/**
 * The actual processor used for Closure compilation, do not use directly,
 * instead use {@link Closure}.
 * 
 * @author Andreas Holstenson
 *
 */
public class ClosureAssetProcessor
	implements AssetProcessor
{
	private final CompilationLevel level;
	private final boolean multiThreaded;

	public ClosureAssetProcessor(
			CompilationLevel level, 
			boolean multiThreaded)
	{
		this.level = level;
		this.multiThreaded = multiThreaded;
	}

	public Resource process(String namespace, 
			String path, 
			Resource in,
			Object... arguments)
		throws IOException
	{
		Compiler.setLoggingLevel(Level.WARNING);
		
		Compiler compiler = new Compiler();
		
		if(! multiThreaded)
		{
			// Threading is disabled, servlet environment is usually single-threaded
			compiler.disableThreads();
		}
		
		CompilerOptions options = new CompilerOptions();
		level.setOptionsForCompilationLevel(options);
		
		JSSourceFile[] sources = new JSSourceFile[1];
		InputStream[] streams = new InputStream[1];
		
		streams[0] = in.openStream();
		sources[0] = JSSourceFile.fromInputStream(path, streams[0]);
		
		JSSourceFile extern = JSSourceFile.fromCode("result.js", "");
		Result result = compiler.compile(extern, sources, options);
		if(false == result.success)
		{
			throw new IOException("Unable to convert; Please check log");
		}
		
		String source = compiler.toSource();
		
		streams[0].close();
		
		return new MemoryResource("text/javascript", "UTF-8", source.getBytes("UTF-8"));
	}

}
