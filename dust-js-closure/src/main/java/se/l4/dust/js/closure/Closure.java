package se.l4.dust.js.closure;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;

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
	private boolean activeInDevelopment;
	private LanguageMode languageMode;
	
	public Closure()
	{
		level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
	}
	
	/**
	 * Set that optimization should be performed even when running in
	 * development mode.
	 * 
	 * @return
	 */
	public Closure activeInDevelopment()
	{
		activeInDevelopment = true;
		
		return this;
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
	
	public Closure setLanguageMode(LanguageMode mode)
	{
		languageMode = mode;
		
		return this;
	}
	
	/**
	 * Build the asset processor.
	 * 
	 * @return
	 */
	public AssetProcessor build()
	{
		return new ClosureAssetProcessor(level, multiThreaded, activeInDevelopment, languageMode);
	}
}
