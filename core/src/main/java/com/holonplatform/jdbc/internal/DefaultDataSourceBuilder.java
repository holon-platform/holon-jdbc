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

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.sql.DataSource;

import com.holonplatform.core.config.ConfigPropertySet.ConfigurationException;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.jdbc.DataSourceBuilder;
import com.holonplatform.jdbc.DataSourceConfigProperties;
import com.holonplatform.jdbc.DataSourceFactory;
import com.holonplatform.jdbc.DataSourcePostProcessor;

/**
 * Default {@link DataSourceBuilder} implementation.
 * 
 * @since 5.0.0
 */
public class DefaultDataSourceBuilder implements DataSourceBuilder {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = JdbcLogger.create();

	/**
	 * ClassLoader
	 */
	private final WeakReference<ClassLoader> classLoader;

	/**
	 * {@link DataSourceFactory}s bound to type names.
	 */
	private final ConcurrentMap<String, DataSourceFactory> factories = new ConcurrentHashMap<>(4, 0.9f, 1);

	/**
	 * {@link DataSourcePostProcessor}s.
	 */
	private final List<DataSourcePostProcessor> postProcessors = new LinkedList<>();

	/**
	 * Constructor
	 * @param classLoader ClassLoader to use to load default factories and post processors (not null)
	 */
	public DefaultDataSourceBuilder(ClassLoader classLoader) {
		super();
		ObjectUtils.argumentNotNull(classLoader, "ClassLoader must be not null");
		this.classLoader = new WeakReference<>(classLoader);
		// ensure defaults inited
		DefaultDataSourceBuilderConfiguration.ensureInited(classLoader);
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.jdbc.DataSourceBuilder#registerFactory(com.holonplatform.jdbc.DataSourceFactory)
	 */
	@Override
	public void registerFactory(DataSourceFactory factory) {
		ObjectUtils.argumentNotNull(factory, "DataSourceFactory must be not null");
		ObjectUtils.argumentNotNull(factory.getDataSourceType(), "DataSource type name must be not null");
		factories.put(factory.getDataSourceType(), factory);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.holonplatform.jdbc.DataSourceBuilder#registerPostProcessor(com.holonplatform.jdbc.DataSourcePostProcessor)
	 */
	@Override
	public void registerPostProcessor(DataSourcePostProcessor postProcessor) {
		ObjectUtils.argumentNotNull(postProcessor, "DataSourcePostProcessor must be not null");
		if (!postProcessors.contains(postProcessor)) {
			postProcessors.add(postProcessor);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.jdbc.DataSourceBuilder#build(com.holonplatform.jdbc.DataSourceConfigProperties)
	 */
	@Override
	public DataSource build(final DataSourceConfigProperties configurationProperties) throws ConfigurationException {
		ObjectUtils.argumentNotNull(configurationProperties, "DataSource configuration properties must be not null");

		final ClassLoader cl = classLoader.get();

		// type
		String type = configurationProperties.getConfigPropertyValue(DataSourceConfigProperties.TYPE, null);

		// check JNDI
		final String jndiName = configurationProperties.getConfigPropertyValue(DataSourceConfigProperties.JNDI_NAME,
				null);

		if (jndiName != null && !jndiName.trim().equals("")) {
			// check type
			if (type != null && !type.trim().equals("") && !DataSourceBuilder.TYPE_JNDI.equals(jndiName)) {
				throw new ConfigurationException("Invalid DataSource type property: when ["
						+ DataSourceConfigProperties.JNDI_NAME.getKey() + "] property is specified only the type ["
						+ DataSourceBuilder.TYPE_JNDI + "] is admitted, or it must be omitted");
			}
			type = DataSourceBuilder.TYPE_JNDI;
		}

		// type
		if (type == null && cl != null) {
			// get default
			type = DefaultDataSourceBuilderConfiguration.getDefaultType(cl).orElse(null);
		}

		if (type == null) {
			throw new ConfigurationException(
					"Missing DataSource type property value and no default DataSource type available"
							+ configurationProperties.getDataContextId().map(d -> " [Data context id: " + d + "]")
									.orElse(""));
		}

		final String dataSourceType = type;

		LOGGER.debug(() -> "Building DataSource using type [" + dataSourceType + "]");

		// get a suitable factory
		DataSourceFactory factory = factories.get(type);
		if (factory == null && cl != null) {
			factory = DefaultDataSourceBuilderConfiguration.getDataSourceFactory(cl, dataSourceType).orElse(null);
		}

		if (factory == null) {
			throw new ConfigurationException("No DataSourceFactory bound to DataSource type [" + dataSourceType
					+ "] is available"
					+ configurationProperties.getDataContextId().map(d -> " [Data context id: " + d + "]").orElse(""));
		}

		// build DataSource
		final DataSource dataSource = factory.build(configurationProperties);

		// post processors
		getPostProcessors(cl)
				.forEach(p -> p.postProcessDataSource(dataSource, dataSourceType, configurationProperties));

		// done
		return dataSource;
	}

	/**
	 * Get all registered and default {@link DataSourcePostProcessor}.
	 * @param classLoader ClassLoader
	 * @return Post processors list
	 */
	private List<DataSourcePostProcessor> getPostProcessors(ClassLoader classLoader) {
		List<DataSourcePostProcessor> pp = new LinkedList<>();
		// registered
		pp.addAll(postProcessors);
		// defaults
		if (classLoader != null) {
			pp.addAll(DefaultDataSourceBuilderConfiguration.getDataSourcePostProcessors(classLoader));
		}
		return pp;
	}

}
