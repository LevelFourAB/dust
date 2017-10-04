package se.l4.dust.core.internal.expression.model;

import se.l4.dust.api.template.Expose;

/**
 * Person representation for tests.
 *
 * @author Andreas Holstenson
 *
 */
public class Person
{
	private String name;
	private int age;
	@Expose
	private boolean verified;
	public String role;

	public Person()
	{
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getAge()
	{
		return age;
	}

	public void setAge(int age)
	{
		this.age = age;
	}

	public String getSuffixedName(String suffix)
	{
		return name + suffix;
	}
}
