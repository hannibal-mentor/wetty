package org.wetty.httpserver.utils;

import io.netty.util.AttributeKey;

import org.wetty.httpserver.controllers.ControllerManager;
import org.wetty.httpserver.utils.statistics.SimpleStatistics;
import org.wetty.httpserver.utils.statistics.Statistics;
import org.wetty.httpserver.views.HTMLViewBuilder;
import org.wetty.httpserver.views.ViewBuilder;

public abstract class Constants {
	public static final class Names {
		public static final AttributeKey<Class<? extends Statistics>> STATISTICS_CLASS = AttributeKey.valueOf(
	    		SimpleStatistics.class.getName());
		public static final AttributeKey<Class<? extends ControllerManager>> CONTROLLERMANAGER_CLASS = AttributeKey.valueOf(
	    		ControllerManager.class.getName());
		public static final AttributeKey<Class<? extends ViewBuilder>> VIEWBUILDER_CLASS = AttributeKey.valueOf(
	    		HTMLViewBuilder.class.getName());
	}
};

