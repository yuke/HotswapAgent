package org.hotswap.agent.plugin.spring;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hotswap.agent.logging.AgentLogger;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;

/**
 * Support for Spring MVC mapping caches.
 */
public class ResetRequestMappingCaches {
	
	private static AgentLogger LOGGER = AgentLogger.getLogger(ResetRequestMappingCaches.class);
	
	private static Class<?> getHandlerMethodMappingClassOrNull() {
		try {
			//This is probably a bad idea as Class.forName has lots of issues but this was easiest for now.
			return Class.forName("org.springframework.web.servlet.handler.AbstractHandlerMethodMapping");
		} catch (ClassNotFoundException e) {
			LOGGER.trace("HandlerMethodMapping class not found");
			return null;
		}
	}
	
	public static void reset(DefaultListableBeanFactory beanFactory) {
		
		Class<?> c = getHandlerMethodMappingClassOrNull();
        if (c == null)
            return;

		Map<String, ?> mappings =
				BeanFactoryUtils.beansOfTypeIncludingAncestors(beanFactory, c, true, false);
		if (mappings.isEmpty()) {
			LOGGER.trace("Spring: no HandlerMappings found");
		}
		try {
			for (Entry<String, ?> e : mappings.entrySet()) {
				AbstractHandlerMethodMapping<Object> am = (AbstractHandlerMethodMapping<Object>) e.getValue();
				LOGGER.info("Spring: clearing HandlerMapping for {}", am.getClass());
				Map<Object,?> unmodifiableHandlerMethods = (Map<Object,?>) am.getHandlerMethods();
				Set<Object> keys = unmodifiableHandlerMethods.keySet();
				unmodifiableHandlerMethods = null;
				for (Object key: keys) {
					am.unregisterMapping(key);
				}
				if (am instanceof InitializingBean) {
					((InitializingBean) am).afterPropertiesSet();
				}
			}
		} catch (Exception e) {
			LOGGER.error("Failed to clear HandlerMappings", e);
		}
		
	}

}
