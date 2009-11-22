package se.l4.dust.api;

public interface DocumentLinker
{
	void addLink(String link, String... extraArguments);
	
	void addScript(String link, String... extraArguments);
}
