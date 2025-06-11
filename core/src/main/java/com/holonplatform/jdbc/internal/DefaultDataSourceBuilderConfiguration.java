/*
 * Copyright 2016-2017 Axioma srl.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.holonplatform.jdbc.internal;

import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.WeakHashMap;

import jakarta.annotation.Priority;

import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.utils.ClassUtils;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.jdbc.DataSourceBuilder;
import com.holonplatform.jdbc.DataSourceConfigProperties;
import com.holonplatform.jdbc.DataSourceFactory;
import com.holonplatform.jdbc.DataSourcePostProcessor;

/**
 * Class to load and provide default {@link DataSourceFactory}s and {@link DataSourcePostProcessor}s obtained using
 * standard Java extensions loader from <code>MET-INF/services</code>.
 * 
 * @since 5.0.0
 */
public final class DefaultDataSourceBuilderConfiguration implements Serializable {

	private static final long serialVersionUID = -1022841607822021462L;

	/**
	 * Logger
	 */
	private static final Logger LOGGER = JdbcLogger.create();

	/**
	 * Default factories by ClassLoader
	 */
	private static final Map<ClassLoader, Map<String, DataSourceFactory>> FACTORIES = new WeakHashMap<>();

	/**
	 * Default post processors by ClassLoader
	 */
	private static final Map<ClassLoader, List<DataSourcePostProcessor>> POST_PROCESSORS = new WeakHashMap<>();

	/**
	 * Default type name by ClassLoader
	 */
	private static final Map<ClassLoader, String> DEFAULT_TYPE = new WeakHashMap<>();

	/**
	 * {@link Priority} based comparator.
	 */
	private static final Comparator<Object> PRIORITY_COMPARATOR = Comparator.comparingInt(
			p -> p.getClass().isAnnotationPresent(Priority.class) ? p.getClass().getAnnotation(Priority.class).value()
					: DataSourceBuilder.DEFAULT_PRIORITY);

	/*
	 * Empty private constructor: this class is intended only to provide constants ad utility methods.
	 */
	private DefaultDataSourceBuilderConfiguration() {
	}

	/**
	 * Get the default DataSource type name for given ClassLoader.
	 * @param classLoader ClassLoader (not null)
	 * @return Optional default DataSource type name
	 */
	public static Optional<String> getDefaultType(ClassLoader classLoader) {
		ObjectUtils.argumentNotNull(classLoader, "ClassLoader must be not null");
		ensureInited(classLoader);
		return Optional.ofNullable(DEFAULT_TYPE.get(classLoader));
	}

	/**
	 * Get the default {@link DataSourceFactory} bound to given <code>type</code> and ClassLoader, if available.
	 * @param classLoader ClassLoader to use (not null)
	 * @param type Type name (not null)
	 * @return Optional factory bound to given type
	 */
	public static Optional<DataSourceFactory> getDataSourceFactory(ClassLoader classLoader, String type) {
		ObjectUtils.argumentNotNull(classLoader, "ClassLoader must be not null");
		ObjectUtils.argumentNotNull(type, "DataSource type must be not null");
		ensureInited(classLoader);
		return Optional.ofNullable(FACTORIES.get(classLoader).get(type));
	}

	/**
	 * Get the default {@link DataSourcePostProcessor}s for given ClassLoader.
	 * @param classLoader ClassLoader to use (not null)
	 * @return Post processors list
	 */
	public static List<DataSourcePostProcessor> getDataSourcePostProcessors(ClassLoader classLoader) {
		ObjectUtils.argumentNotNull(classLoader, "ClassLoader must be not null");
		ensureInited(classLoader);
		return POST_PROCESSORS.get(classLoader);
	}

	/**
	 * Ensure the default factories and post processors are inited for given classloader.
	 * @param classLoader ClassLoader to use
	 */
	public static synchronized void ensureInited(final ClassLoader classLoader) {
		ensureDefaultTypeInited(classLoader);
		ensureFactoriesInited(classLoader);
		ensurePostProcessorsInited(classLoader);
	}

	/**
	 * Ensure the default DataSource type is inited for given classloader.
	 * @param classLoader ClassLoader to use
	 */
	private static synchronized void ensureDefaultTypeInited(final ClassLoader classLoader) {
		if (!DEFAULT_TYPE.containsKey(classLoader)) {
			// check HikariCP
			if (ClassUtils.isPresent("com.zaxxer.hikari.HikariDataSource", classLoader)) {
				DEFAULT_TYPE.put(classLoader, DataSourceBuilder.TYPE_HIKARICP);
				LOGGER.debug(() -> "Default DataSource type for classloader [" + classLoader + "] is: "
						+ DataSourceBuilder.TYPE_HIKARICP);
			}
			// check Tomcat jdbc
			else if (ClassUtils.isPresent("org.apache.tomcat.jdbc.pool.DataSource", classLoader)) {
				DEFAULT_TYPE.put(classLoader, DataSourceBuilder.TYPE_TOMCAT);
				LOGGER.debug(() -> "Default DataSource type for classloader [" + classLoader + "] is: "
						+ DataSourceBuilder.TYPE_TOMCAT);
			}
			// defaults to basic
			else {
				DEFAULT_TYPE.put(classLoader, DataSourceBuilder.TYPE_BASIC);
				LOGGER.debug(() -> "Default DataSource type for classloader [" + classLoader + "] is: "
						+ DataSourceBuilder.TYPE_BASIC);
			}
		}
	}

	/**
	 * Ensure the default factories are inited for given classloader.
	 * @param classLoader ClassLoader to use
	 */
	private static synchronized void ensureFactoriesInited(final ClassLoader classLoader) {
		if (!FACTORIES.containsKey(classLoader)) {

			LOGGER.debug(() -> "Load DataSourceFactory for classloader [" + classLoader
					+ "] using ServiceLoader with service name: " + DataSourceFactory.class.getName());

			final List<DataSourceFactory> results = new LinkedList<>();
			// load from META-INF/services
			Iterable<DataSourceFactory> loaded = AccessController
					.doPrivileged(new PrivilegedAction<Iterable<DataSourceFactory>>() {
						@Override
						public Iterable<DataSourceFactory> run() {
							return ServiceLoader.load(DataSourceFactory.class, classLoader);
						}
					});
			loaded.forEach(l -> results.add(l));
			Collections.sort(results, PRIORITY_COMPARATOR);

			final Map<String, DataSourceFactory> fs = new HashMap<>(8);

			for (DataSourceFactory result : results) {
				final String type = result.getDataSourceType();
				if (type != null) {
					if (!fs.containsKey(type)) {
						fs.put(type, result);
						LOGGER.debug(() -> "Registered DataSourceFactory [" + result + "] for classloader ["
								+ classLoader + "]");
					}
				} else {
					LOGGER.warn("Invalid DataSourceFactory [" + result
							+ "]: missing type name - the factory will be ignored");
				}
			}

			FACTORIES.put(classLoader, fs);
		}
	}

	/**
	 * Ensure the default post processors are inited for given classloader.
	 * @param classLoader ClassLoader to use
	 */
	private static synchronized void ensurePostProcessorsInited(final ClassLoader classLoader) {
		if (!POST_PROCESSORS.containsKey(classLoader)) {

			LOGGER.debug(() -> "Load DataSourcePostProcessor for classloader [" + classLoader
					+ "] using ServiceLoader with service name: " + DataSourcePostProcessor.class.getName());

			final List<DataSourcePostProcessor> results = new LinkedList<>();
			// load from META-INF/services
			Iterable<DataSourcePostProcessor> loaded = AccessController
					.doPrivileged(new PrivilegedAction<Iterable<DataSourcePostProcessor>>() {
						@Override
						public Iterable<DataSourcePostProcessor> run() {
							return ServiceLoader.load(DataSourcePostProcessor.class, classLoader);
						}
					});
			loaded.forEach(l -> {
				results.add(l);
				LOGGER.debug(
						() -> "Registered DataSourcePostProcessor [" + l + "] for classloader [" + classLoader + "]");
			});
			Collections.sort(results, PRIORITY_COMPARATOR);

			POST_PROCESSORS.put(classLoader, results);
		}
	}

	/**
	 * Build the missing JDBC url error message.
	 * @param type DataSource type
	 * @param dataContextId Optional data context id
	 * @return the error message
	 */
	public static String buildMissingJdbcUrlMessage(String type, String dataContextId) {
		StringBuilder sb = new StringBuilder();
		if (dataContextId != null) {
			sb.append("(Data context id: ");
			sb.append(dataContextId);
			sb.append(") ");
		}
		sb.append("Failed to initialize DataSource of type [");
		sb.append(type);
		sb.append("]: Missing JDBC connection URL. Check the DataSource configuration property [");
		sb.append(DataSourceConfigProperties.DEFAULT_NAME);
		sb.append(".");
		if (dataContextId != null) {
			sb.append(dataContextId);
			sb.append(".");
		}
		sb.append(DataSourceConfigProperties.URL.getKey());
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Build the missing JDBC driver class name error message.
	 * @param type DataSource type
	 * @param dataContextId Optional data context id
	 * @return the error message
	 */
	public static String buildMissingDriverClassMessage(String type, String dataContextId) {
		StringBuilder sb = new StringBuilder();
		if (dataContextId != null) {
			sb.append("(Data context id: ");
			sb.append(dataContextId);
			sb.append(") ");
		}
		sb.append("Failed to initialize DataSource of type [");
		sb.append(type);
		sb.append(
				"]: Cannot auto-detect JDBC driver class to use. The driver class name must be specified using configuration property [");
		sb.append(DataSourceConfigProperties.DEFAULT_NAME);
		sb.append(".");
		if (dataContextId != null) {
			sb.append(dataContextId);
			sb.append(".");
		}
		sb.append(DataSourceConfigProperties.DRIVER_CLASS_NAME.getKey());
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Build the missing JNDI name error message.
	 * @param dataContextId Optional data context id
	 * @return the error message
	 */
	public static String buildMissingJNDINameMessage(String dataContextId) {
		StringBuilder sb = new StringBuilder();
		if (dataContextId != null) {
			sb.append("(Data context id: ");
			sb.append(dataContextId);
			sb.append(") ");
		}
		sb.append(
				"Failed to initialize JNDI DataSource: Missing JNDI name. Check the DataSource configuration property [");
		sb.append(DataSourceConfigProperties.DEFAULT_NAME);
		sb.append(".");
		if (dataContextId != null) {
			sb.append(dataContextId);
			sb.append(".");
		}
		sb.append(DataSourceConfigProperties.JNDI_NAME.getKey());
		sb.append("]");
		return sb.toString();
	}

}
