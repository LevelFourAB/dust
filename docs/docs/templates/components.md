---
layout: page
title: Components

nav: docs
---

Templates may reference components that live in different <a href="{{ site.baseurl }}/docs/namespaces/">namespaces</a>. 

# Using components

Components are referenced from a template and it's possible to pass attributes and parameters to it.

Attributes are passed via the opening XML-element:
{% highlight xml %}
... xmlns:d="dust:common" ...

<d:if test="${test}">
{% endhighlight %}

Parameters are passed via XML-elements in the namespace `dust:parameters`:
{% highlight xml %}
... xmlns:p="dust:parameters" ...

<p:else>Hi!</p:else>
{% endhighlight %}

<a href="{{ site.baseurl }}/docs/namespaces/common/#components" class="btn btn-lg btn-outline">More about the standard components</a>

# Creating a component

Classes are regular classes that are annotated with `@Component`. Components are automatically discovered within registered namespaces.

Here is a tiny component that says hello to a user:

{% highlight java %}
@Component("hello")
public class HelloComponent {
	private String name;

	@PrepareRender
	public void prepare(@TemplateParam("name") String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
{% endhighlight %}

With a template named `HelloComponent.xml` in the same package:
{% highlight xml %}
<span xmlns:d="dust:common">
	Hello ${name}!

	<d:if test="${name == 'Cookie monster'}">
		Your name is awesome!
	</d:if>
</span>
{% endhighlight %}

Calling this component:

{% highlight xml %}
<namespace:hello name="your name here" />
{% endhighlight %}

# Annotations

## `@PrepareRender`

Methods annotated with `@PrepareRender` are called just before a component is rendered. If several methods are annotated Dust will select the best one to invoke. This is useful to allow for several ways to use a components, such as follows:

{% highlight java %}
@PrepareRender
public void prepare(@TemplateParam("names") List<String> name) { /* ... */ }

@PrepareRender
public void prepare(@TemplateParam("name") String name) { /* ... */ }
{% endhighlight %}

## `@TemplateParam(String)`

This annotation is used to receive a value from the calling template. This can either be used with `@PrepareRender` or on a setter method.
