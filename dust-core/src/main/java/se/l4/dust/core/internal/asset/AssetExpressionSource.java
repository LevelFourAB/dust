package se.l4.dust.core.internal.asset;

import se.l4.dust.api.Context;
import se.l4.dust.api.asset.Asset;
import se.l4.dust.api.asset.AssetManager;
import se.l4.dust.api.expression.DynamicMethod;
import se.l4.dust.api.expression.DynamicProperty;
import se.l4.dust.api.expression.ExpressionEncounter;
import se.l4.dust.api.expression.ExpressionSource;
import se.l4.dust.api.template.RenderingContext;

import com.google.inject.Inject;

/**
 * Property source for binding assets for use in templates.
 * 
 * @author andreas
 *
 */
public class AssetExpressionSource
	implements ExpressionSource
{
	private final AssetMethod assetMethod;
	
	@Inject
	public AssetExpressionSource(AssetManager assets, String namespace)
	{
		assetMethod = new AssetMethod(assets, namespace);
	}
	
	@Override
	public DynamicMethod getMethod(ExpressionEncounter encounter, String name, Class... parameters)
	{
		if("asset".equals(name) && encounter.isRoot())
		{
			if(parameters.length != 1)
			{
				encounter.error("The " + name + " method takes one argument");
			}
			
			return assetMethod;
		}
		
		return null;
	}
	
	@Override
	public DynamicProperty getProperty(ExpressionEncounter encounter, String name)
	{
		return null;
	}
	
	private class AssetMethod
		implements DynamicMethod
	{
		private final AssetManager assets;
		private final String namespace;

		public AssetMethod(AssetManager assets, String namespace)
		{
			this.assets = assets;
			this.namespace = namespace;
		}

		@Override
		public Object invoke(Context context, Object instance, Object... parameters)
		{
			Asset asset = assets.locate(context, namespace, String.valueOf(parameters[0]));
			return ((RenderingContext) context).resolveURI(asset);
		}

		@Override
		public Class<?> getType()
		{
			return String.class;
		}

		@Override
		public boolean needsContext()
		{
			return false;
		}
	}
}
