package se.l4.dust.js.closure;

import com.google.javascript.jscomp.CompilationLevel;

import se.l4.dust.api.asset.AssetProcessor;

/**
 * Builder for a {@link AssetProcessor} that will use the Closure compiler
 * for JavaScript optimization.
 * 
 * @author Andreas Holstenson
 *
 */
public class Closure
{
	private CompilationLevel level;
	private boolean multiThreaded;
	
	public Closure()
	{
		level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
	}
	
	/**
	 * Set which level to use during optimization.
	 * 
	 * @param level
	 * @return
	 */
	public Closure setLevel(CompilationLevel level)
	{
		this.level = level;
		
		return this;
	}
	
	/**
	 * Define that advanced optimization should be used.
	 * 
	 * @return
	 */
	public Closure advancedOptimization()
	{
		level = CompilationLevel.ADVANCED_OPTIMIZATIONS;
		
		return this;
	}
	
	/**
	 * Set that the optimization should use multiple threads. This can be
	 * turned on if your environment has support for multiple threads.
	 *  
	 * @return
	 */
	public Closure multiThreaded()
	{
		multiThreaded = true;
		
		return this;
	}
	
	/**
	 * Build the asset processor.
	 * 
	 * @return
	 */
	public AssetProcessor build()
	{
		return new ClosureAssetProcessor(level, multiThreaded);
	}
}
