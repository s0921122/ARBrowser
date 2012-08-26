package org.takanolab.ar.search;

public class Document {

	String name = "";
	String description = "";
	
	public void setName (String name) {
		this.name = name;
	};
	public void setDescription (String description) {
		this.description = description;
	};
	public String getName () {
		return this.name;
	} ;
	public String getDescription () {
		return this.description;
	};
}
