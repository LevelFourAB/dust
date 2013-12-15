package se.l4.dust.js.closure;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import se.l4.dust.api.asset.AssetEncounter;
import se.l4.dust.api.asset.AssetProcessor;
import se.l4.dust.api.resource.MemoryResource;
import se.l4.dust.api.resource.Resource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.io.LimitInputStream;
import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.jscomp.JSModule;
import com.google.javascript.jscomp.JSSourceFile;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;

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
	private final boolean activeInDevelopment;
	private final LanguageMode languageMode;
	
	private static final List<String> DEFAULT_EXTERNS_NAMES = ImmutableList.of(
		"es3.js", "es5.js", "w3c_event.js", "w3c_event3.js",
		"gecko_event.js", "ie_event.js", "webkit_event.js",
		"w3c_device_sensor_event.js", "w3c_dom1.js", "w3c_dom2.js",
		"w3c_dom3.js", "gecko_dom.js", "ie_dom.js", "webkit_dom.js",
		"w3c_css.js", "gecko_css.js", "ie_css.js", "webkit_css.js",
		"google.js", "chrome.js", "deprecated.js", "fileapi.js",
		"flash.js", "gecko_xml.js", "html5.js", "ie_vml.js", "iphone.js",
		"webstorage.js", "w3c_anim_timing.js", "w3c_css3d.js",
		"w3c_elementtraversal.js", "w3c_geolocation.js",
		"w3c_indexeddb.js", "w3c_navigation_timing.js", "w3c_range.js",
		"w3c_selectors.js", "w3c_xml.js", "window.js",
		"webkit_notifications.js", "webgl.js"
	);

	public ClosureAssetProcessor(
			CompilationLevel level, 
			boolean multiThreaded, 
			boolean activeInDevelopment,
			LanguageMode languageMode)
	{
		this.level = level;
		this.multiThreaded = multiThreaded;
		this.activeInDevelopment = activeInDevelopment;
		this.languageMode = languageMode;
	}

	public void process(AssetEncounter encounter)
		throws IOException
	{
		if(! encounter.isProduction() && ! activeInDevelopment)
		{
			// Not running in production and not set to active
			return;
		}
		
		Resource cached = encounter.getCached("closure");
		if(cached != null)
		{
			encounter.replaceWith(cached);
			return;
		}
		
		Compiler.setLoggingLevel(Level.WARNING);
		
		Compiler compiler = new Compiler();
		
		if(! multiThreaded)
		{
			// Threading is disabled, servlet environment is usually single-threaded
			compiler.disableThreads();
		}
		
		CompilerOptions options = new CompilerOptions();
		level.setOptionsForCompilationLevel(options);
		
		if(languageMode != null)
		{
			options.setLanguageIn(languageMode);
			options.setLanguageOut(languageMode);
		}
		
		InputStream input = CommandLineRunner.class.getResourceAsStream("/externs.zip");

		ZipInputStream zip = new ZipInputStream(input);
		Map<String, SourceFile> externsMap = Maps.newHashMap();
		for(ZipEntry entry = null; (entry = zip.getNextEntry()) != null;)
		{
			BufferedInputStream entryStream = new BufferedInputStream(new LimitInputStream(zip, entry.getSize()));
			externsMap.put(entry.getName(), SourceFile.fromInputStream("externs.zip//" + entry.getName(), entryStream));
		}
		
		List<SourceFile> externs = new ArrayList<SourceFile>();
		for(String key : DEFAULT_EXTERNS_NAMES)
		{
			externs.add(externsMap.get(key));
		}
		
		JSModule module = new JSModule("result");
		module.add(SourceFile.fromInputStream(encounter.getPath(), encounter.getResource().openStream()));
		Result result = compiler.compileModules(externs, ImmutableList.of(module), options);
		if(false == result.success)
		{
			throw new IOException("Unable to convert; Please check log");
		}
		
		String source = compiler.toSource();
		
		MemoryResource mr = new MemoryResource("text/javascript", "UTF-8", source.getBytes("UTF-8"));
		encounter
			.cache("closure", mr)
			.replaceWith(mr);
	}

}
