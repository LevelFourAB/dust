package se.l4.dust.jrebel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.zeroturnaround.javarebel.ClassEventListener;
import org.zeroturnaround.javarebel.Logger;
import org.zeroturnaround.javarebel.LoggerFactory;
import org.zeroturnaround.javarebel.Reloader;
import org.zeroturnaround.javarebel.ReloaderFactory;

/**
 * Reloader that will attempt to dynamically reload Crayon modules on a fixed
 * schedule.
 * 
 * @author Andreas Holstenson
 *
 */
public class ModuleReloader
{
	private static final ModuleReloader INSTANCE = new ModuleReloader();

	private final Logger logger;
	
	private final ScheduledExecutorService service;
	private final Set<Object> instances;

	private final Reloader reloader;
	private final ClassEventListener classReloader;
	
	private ModuleReloader()
	{
		logger = LoggerFactory.getInstance();
		
		service = Executors.newScheduledThreadPool(1, new ThreadFactory()
		{
			@Override
			public Thread newThread(Runnable r)
			{
				Thread t = new Thread(r, "dust-jrebel");
				t.setDaemon(true);
				return t;
			}
		});
		
		instances = new CopyOnWriteArraySet<Object>();
		
		service.scheduleWithFixedDelay(new Runnable()
		{
			@Override
			public void run()
			{
				/*
				 * Looping and calling toString will trigger class reloading
				 * by JRebel.
				 */
				for(Object o : instances)
				{
					o.toString();
				}
			}
		}, 5, 1, TimeUnit.SECONDS);
		
		reloader = ReloaderFactory.getInstance();
		classReloader = new ClassEventListener()
		{
			@Override
			public void onClassEvent(int arg0, Class arg1)
			{
				reload();
			}

			@Override
			public int priority()
			{
				return 0;
			}
		};
	}
	
	/**
	 * Perform actual reloading of the modules. This uses reflection to call
	 * into Dust to trigger rerunning of contributions.
	 */
	private void reload()
	{
		logger.echo("Dust: Running contributions");
		try
		{
			Class<?> c = Class.forName("se.l4.dust.core.internal.InternalContributions");
			Method method = c.getMethod("rerun");
			method.invoke(null);
		}
		catch(Exception e)
		{
			logger.errorEcho(e);
		}
	}

	/**
	 * Add a new module to this reloader.
	 * 
	 * @param instance
	 */
	public static void add(Object instance)
	{
		INSTANCE.add0(instance);
	}

	private void add0(Object instance)
	{
		for(Method m : instance.getClass().getMethods())
		{
			for(Annotation a : m.getAnnotations())
			{
				if(a.annotationType().getName().startsWith("se.l4.dust.api"))
				{
					// API annotation, include in monitoring
					if(instances.add(instance))
					{
						logger.echo("Dust: Monitoring " + instance.getClass() + " for changes");
					
						reloader.addClassReloadListener(instance.getClass(), classReloader);
					}
					break;
				}
			}
		}
	}
}
