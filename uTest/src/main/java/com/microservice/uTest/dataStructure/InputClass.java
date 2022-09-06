package com.microservice.uTest.dataStructure;

public class InputClass {
	public String name;
	public String type;
	public String min;
	public String max;
	public String defaultValue;
	public boolean valid;
	public boolean required = true;
	
	public InputClass(String name, String type, String min, String max, boolean required) {
		super();
		this.name = name;
		this.type = type;
		this.min = min;
		this.max = max;
		this.required = required;
	}
	
	public InputClass(String name, String type, boolean valid, boolean required) {
		super();
		this.name = name;
		this.type = type;
		this.valid = valid;
		this.required = required;
	}
	
	public InputClass(String name, String type, String defaultValue, boolean valid, boolean required) {
		super();
		this.name = name;
		this.type = type;
		this.valid = valid;
		this.defaultValue = defaultValue;
		this.required = required;
	}
	
	
	public InputClass(String name, String type, String min, String max, String defaultValue, boolean valid) {
		super();
		this.name = name;
		this.type = type;
		this.min = min;
		this.max = max;
		this.defaultValue = defaultValue;
		this.valid = valid;
	}
	
	public InputClass(String name, String type, String min, String max, String defaultValue, boolean valid, boolean required) {
		super();
		this.name = name;
		this.type = type;
		this.min = min;
		this.max = max;
		this.defaultValue = defaultValue;
		this.valid = valid;
		this.required = required;
	}

}
