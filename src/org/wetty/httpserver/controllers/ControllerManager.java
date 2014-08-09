/*
 * Class to handle controllers
 * Should find view
 */

package org.wetty.httpserver.controllers;

import java.util.ArrayList;

import org.wetty.httpserver.views.ViewBuilder;

public class ControllerManager {
	
	//TODO:
	//private Object model;
	//private Object view;
	
	private ArrayList controllers = new ArrayList();
	
	public ControllerManager() {		
		controllers.add("/hello");
		controllers.add("/redirect");
		controllers.add("/status");		
	}
	
	public Object getView(ViewBuilder v, String url) {
		
		if (controllers.contains(url)) {
			return v.process(url);
		} else {
			return v.error404();
		}
		
	}
}
