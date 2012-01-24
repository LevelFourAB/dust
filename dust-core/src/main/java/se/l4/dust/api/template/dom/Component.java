package se.l4.dust.api.template.dom;

/**
 * Holder element for components.
 * 
 * @author Andreas Holstenson
 *
 */
public class Component
	extends Element
{
	private final Class<?> component;

	public Component(String name, Class<?> component, String... attributes)
	{
		super(name, attributes);
		this.component = component;
	}
	
	public Class<?> getComponent()
	{
		return component;
	}
	
	@Override
	public String toString()
	{
		return "Component[name=" + getName() + ", component=" + component + "]";
	}
}
