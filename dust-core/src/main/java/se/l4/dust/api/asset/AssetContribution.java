package se.l4.dust.api.asset;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * Annotation used for adding combined assets or registering asset conversions.
 *
 * <p>
 * Example:
 * <pre>
 * {@literal @Assets}
 * public void contributeStylesheet(AssetManager manager) {
 * 	manager.addAsset(NAMESPACE, "css/screen.css")
 *			.add("css/reset.css")
 *			.add("css/style.less")
 *			.process(CssCompressProcessor.class)
 *			.create();
 * }
 * </pre>
 *
 * @author Andreas Holstenson
 *
 */
@BindingAnnotation
@Target({ ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface AssetContribution
{
	/**
	 * Name of the method, can be used if ordering is required.
	 *
	 * @return
	 */
	String name() default "";
}
