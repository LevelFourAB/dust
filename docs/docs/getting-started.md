---
layout: page
title: Getting Started

nav: docs
---

## Getting the code

### Via Maven

{% highlight xml %}
<dependency>
	<groupId>se.l4.dust</groupId>
	<artifactId>dust-jaxrs-resteasy</artifactId>
	<version>0.4-SNAPSHOT</version>
</dependency>
{% endhighlight %}

### From source

Clone the repository using Git:

	git clone https://github.com/LevelFourAB/dust.git dust

Then build via Maven:

	cd dust
	mvn install

## Configuring via web.xml

### Bootstrapping

Dust uses <a href="https://code.google.com/p/google-guice/">Guice</a> for dependency injection and <a href="https://github.com/LevelFourAB/crayon">Crayon</a> for configuration. Dust needs access to an Injector, which is provided via bootstraping.

The easiest way is to extend AppBootstrap:

{% highlight java %}
package se.l4.dust.example;

// ...

public class MyAppBoostrap extends AppBoostrap {
	protected void initialize(Configurator configurator) {
		configurator.add(MyAppModule.class);
	}
}
{% endhighlight %}

You can also extend AbstractBootstrap and provide an existing Injector.

### Updating web.xml

To start routing requests to Dust update your web.xml with a new filter and your new bootstrap listener:

{% highlight xml %}
<filter>
	<filter-name>dust</filter-name>
	<filter-class>se.l4.dust.servlet.DustFilter</filter-class>
</filter>

<filter-mapping>
    <filter-name>dust</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>

<listener>
    <listener-class>se.l4.dust.example.MyAppBootstrap</listener-class>
</listener>
{% endhighlight %}

## Defining a namespace

Namespaces are defined via Guice-modules. Dust will automatically scan for components and JAX-RS resources within a namespace.

{% highlight java %}
package se.l4.dust.example;

// ...

public static class MyAppModule extends CrayonModule {
	@Override
	protected void configure() {
		install(new ResteasyModule());
	}

	@NamespaceBinding
	public void contributeNamespace(Namespaces namespaces) {
		namespaces.bind("dust:example").add();
	}
}
{% endhighlight %}

## Creating a page

A simple JAX-RS resource can be defined like this: 

{% highlight java %}
package se.l4.dust.example.pages;

// ...

@Path("/")
@Template
public class IndexPage {
	@GET
	public IndexPage get() {
		return this;
	}

	public String getName() {
		return "Cookie Monster"
	}
}
{% endhighlight %}

With a template named IndexPage.xml placed in the same package:

{% highlight xml %}
<!DOCTYPE html>
<html>
	<head>
		<title>Hello ${name}!</title>
	</head>
	<body>
		<h1>Hello ${name}!</h1>
	</body>
</html>
{% endhighlight %}

## A first component

Components are a combination of a class and a template, where the template can access the Java-class when rendering.

{% highlight java %}
package se.l4.dust.example.components;

// ...

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

And the template named HelloComponent.xml:

{% highlight xml %}
<h1>Hello ${name}</h1>
{% endhighlight %}

Now update IndexPage.xml:

{% highlight xml %}
<!DOCTYPE html>
<html>
	<head>
		<title>Hello ${name}!</title>
	</head>
	<body xmlns:e="dust:example">
		<e:hello name="${name}" />
	</body>
</html>
{% endhighlight %}

