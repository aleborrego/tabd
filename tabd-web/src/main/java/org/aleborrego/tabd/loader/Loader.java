package org.aleborrego.tabd.loader;

public interface Loader {
	
	public void load(String... arguments) throws LoaderException;

}
