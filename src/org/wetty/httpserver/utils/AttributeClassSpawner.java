package org.wetty.httpserver.utils;

import static org.wetty.httpserver.utils.Constants.Names.CONTROLLERMANAGER_CLASS;
import static org.wetty.httpserver.utils.Constants.Names.STATISTICS_CLASS;
import static org.wetty.httpserver.utils.Constants.Names.VIEWBUILDER_CLASS;
import io.netty.channel.Channel;
import io.netty.util.Attribute;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.wetty.httpserver.controllers.ControllerManager;
import org.wetty.httpserver.utils.statistics.Statistics;
import org.wetty.httpserver.views.ViewBuilder;

public final class AttributeClassSpawner {

	//using reflection, didn't figure out how to simplify yet
		public static Statistics createStatisticsClass(Channel channel) {
			Attribute<Class<? extends Statistics>> statisticsClass = channel.attr(STATISTICS_CLASS);
			Constructor<?> ctor = null;
			try {
				ctor = statisticsClass.get().getConstructor();
			} catch (NoSuchMethodException e) {
				e.printStackTrace(); //let's just leave it
			} catch (SecurityException e) {
				e.printStackTrace(); //let's just leave it
			}
			Statistics statistics = null;
			
			if (ctor != null) {
				try {
					statistics = (Statistics) ctor.newInstance();
				} catch (InstantiationException e) {
					e.printStackTrace(); //let's just leave it
				} catch (IllegalAccessException e) {
					e.printStackTrace(); //let's just leave it
				} catch (IllegalArgumentException e) {
					e.printStackTrace(); //let's just leave it
				} catch (InvocationTargetException e) {
					e.printStackTrace(); //let's just leave it
				}	
			}
			return statistics;
		}

		public static ControllerManager createControllerManagerClass(Channel channel) {
			Attribute<Class<? extends ControllerManager>> controllerManagerClass = channel.attr(CONTROLLERMANAGER_CLASS);
			Constructor<?> ctor = null;
			try {
				ctor = controllerManagerClass.get().getConstructor(ViewBuilder.class);
			} catch (NoSuchMethodException e) {
				e.printStackTrace(); //let's just leave it
			} catch (SecurityException e) {
				e.printStackTrace(); //let's just leave it
			}
			ControllerManager controllerManager = null;
			
			if (ctor != null) {
				try {
					controllerManager = (ControllerManager) ctor.newInstance(createViewBuilderClass(channel));
				} catch (InstantiationException e) {
					e.printStackTrace(); //let's just leave it
				} catch (IllegalAccessException e) {
					e.printStackTrace(); //let's just leave it
				} catch (IllegalArgumentException e) {
					e.printStackTrace(); //let's just leave it
				} catch (InvocationTargetException e) {
					e.printStackTrace(); //let's just leave it
				}	
			}
			return controllerManager;
		}

		public static ViewBuilder createViewBuilderClass(Channel channel) {
			Attribute<Class<? extends ViewBuilder>> viewBuilderClass = channel.attr(VIEWBUILDER_CLASS);
			Constructor<?> ctor = null;
			try {
				ctor = viewBuilderClass.get().getConstructor();
			} catch (NoSuchMethodException e) {
				e.printStackTrace(); //let's just leave it
			} catch (SecurityException e) {
				e.printStackTrace(); //let's just leave it
			}
			ViewBuilder viewBuilder = null;
			
			if (ctor != null) {
				try {
					viewBuilder = (ViewBuilder) ctor.newInstance();
				} catch (InstantiationException e) {
					e.printStackTrace(); //let's just leave it
				} catch (IllegalAccessException e) {
					e.printStackTrace(); //let's just leave it
				} catch (IllegalArgumentException e) {
					e.printStackTrace(); //let's just leave it
				} catch (InvocationTargetException e) {
					e.printStackTrace(); //let's just leave it
				}	
			}
			return viewBuilder;
		}
	
}
