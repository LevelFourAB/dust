package se.l4.dust.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import se.l4.dust.api.asset.Asset;

/**
 * Annotation that can be placed on a field to indicate that an asset should
 * be injected into the field. The type of the field should be {@link Asset}.
 * 
 * <p>
 * The {@link #namespace()} is the URI of the namespace in which the asset
 * is located. {@link #path()} is the actual name of the asset. If the asset
 * can not be found an exception will be thrown during the creation of the
 * object containing the annotated field.
 * 
 * @author Andreas Holstenson
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface InjectAsset
{
	/**
	 * Namespace of asset.
	 * 
	 * @return
	 */
	String namespace();
	
	/**
	 * Path of asset.
	 * 
	 * @return
	 */
	String path();
}
