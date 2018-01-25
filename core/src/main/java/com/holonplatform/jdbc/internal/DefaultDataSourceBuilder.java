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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.sql.DataSource;

import com.holonplatform.core.config.ConfigPropertySet;
import com.holonplatform.core.config.ConfigPropertySet.ConfigurationException;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.utils.ClassUtils;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.jdbc.DataSourceBuilder;
import com.holonplatform.jdbc.DataSourceConfigProperties;
import com.holonplatform.jdbc.DataSourceFactory;
import com.holonplatform.jdbc.DataSourcePostProcessor;
import com.holonplatform.jdbc.DataSourceType;
import com.holonplatform.jdbc.DatabasePlatform;
import com.holonplatform.jdbc.exceptions.DataSourceInitializationException;

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

	// Direct builder

	/**
	 * Default {@link Builder}.
	 */
	public static class DefaultBuilder implements Builder {

		private final ConfigPropertySet.Builder<DataSourceConfigProperties> config = DataSourceConfigProperties
				.builder();

		private List<String> sqlScripts = new LinkedList<>();

		private List<String> sqlScriptResources = new LinkedList<>();

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.jdbc.DataSourceBuilder.Builder#type(java.lang.String)
		 */
		@Override
		public Builder type(String typeName) {
			ObjectUtils.argumentNotNull(typeName, "DataSource type must be not null");
			config.withProperty(DataSourceConfigProperties.TYPE, typeName);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.jdbc.DataSourceBuilder.Builder#type(com.holonplatform.jdbc.DataSourceType)
		 */
		@Override
		public Builder type(DataSourceType type) {
			ObjectUtils.argumentNotNull(type, "DataSource type must be not null");
			config.withProperty(DataSourceConfigProperties.TYPE, type.getType());
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.jdbc.DataSourceBuilder.Builder#name(java.lang.String)
		 */
		@Override
		public Builder name(String name) {
			config.withProperty(DataSourceConfigProperties.NAME, name);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.jdbc.DataSourceBuilder.Builder#driverClassName(java.lang.String)
		 */
		@Override
		public Builder driverClassName(String driverClassName) {
			config.withProperty(DataSourceConfigProperties.DRIVER_CLASS_NAME, driverClassName);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.jdbc.DataSourceBuilder.Builder#url(java.lang.String)
		 */
		@Override
		public Builder url(String url) {
			config.withProperty(DataSourceConfigProperties.URL, url);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.jdbc.DataSourceBuilder.Builder#username(java.lang.String)
		 */
		@Override
		public Builder username(String username) {
			config.withProperty(DataSourceConfigProperties.USERNAME, username);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.jdbc.DataSourceBuilder.Builder#password(java.lang.String)
		 */
		@Override
		public Builder password(String password) {
			config.withProperty(DataSourceConfigProperties.PASSWORD, password);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.jdbc.DataSourceBuilder.Builder#database(com.holonplatform.jdbc.DatabasePlatform)
		 */
		@Override
		public Builder database(DatabasePlatform databasePlatform) {
			config.withProperty(DataSourceConfigProperties.PLATFORM, databasePlatform);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.jdbc.DataSourceBuilder.Builder#autoCommit(boolean)
		 */
		@Override
		public Builder autoCommit(boolean autoCommit) {
			config.withProperty(DataSourceConfigProperties.AUTOCOMMIT, autoCommit);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.jdbc.DataSourceBuilder.Builder#minPoolSize(int)
		 */
		@Override
		public Builder minPoolSize(int minPoolSize) {
			config.withProperty(DataSourceConfigProperties.MIN_POOL_SIZE, minPoolSize);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.jdbc.DataSourceBuilder.Builder#maxPoolSize(int)
		 */
		@Override
		public Builder maxPoolSize(int maxPoolSize) {
			config.withProperty(DataSourceConfigProperties.MAX_POOL_SIZE, maxPoolSize);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.jdbc.DataSourceBuilder.Builder#validationQuery(java.lang.String)
		 */
		@Override
		public Builder validationQuery(String validationQuery) {
			config.withProperty(DataSourceConfigProperties.VALIDATION_QUERY, validationQuery);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.jdbc.DataSourceBuilder.Builder#withInitScript(java.lang.String)
		 */
		@Override
		public Builder withInitScript(String sqlScript) {
			ObjectUtils.argumentNotNull(sqlScript, "SQL script must be not null");
			this.sqlScripts.add(sqlScript);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.jdbc.DataSourceBuilder.Builder#withInitScriptResource(java.lang.String)
		 */
		@Override
		public Builder withInitScriptResource(String sqlScriptResourceName) {
			ObjectUtils.argumentNotNull(sqlScriptResourceName, "SQL script resource name must be not null");
			this.sqlScriptResources.add(sqlScriptResourceName);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.jdbc.DataSourceBuilder.Builder#build()
		 */
		@Override
		public DataSource build() {
			DataSourceConfigProperties cfg = config.build();
			final DataSource dataSource = new DefaultDataSourceBuilder(ClassUtils.getDefaultClassLoader()).build(cfg);

			// check init scripts
			for (String sqlScript : sqlScripts) {
				try (Connection connection = dataSource.getConnection()) {
					SQLScriptUtils.executeSqlScript(connection, sqlScript);
				} catch (SQLException | IOException e) {
					throw new DataSourceInitializationException(
							"Failed to initialize DataSource using provided SQL scripts", e);
				}
			}

			for (String sqlScriptResource : sqlScriptResources) {
				try (InputStream is = ClassUtils.getDefaultClassLoader().getResourceAsStream(sqlScriptResource)) {
					if (is == null) {
						throw new IOException("SQL script not found: " + sqlScriptResource);
					}
					String sql = resourceStreamToString(is);
					if (sql != null) {
						try (Connection connection = dataSource.getConnection()) {
							SQLScriptUtils.executeSqlScript(connection, sql);
						}
					}
				} catch (IOException | SQLException e) {
					throw new DataSourceInitializationException(
							"Failed to initialize DataSource using provided SQL scripts", e);
				}
			}

			return dataSource;
		}

	}

	/**
	 * Get given InputStream as a UTF-8 encoded String.
	 * @param is InputStream (not null)
	 * @return The UTF-8 encoded String
	 * @throws IOException If a read error occurred
	 */
	private static String resourceStreamToString(InputStream is) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = is.read(buffer)) != -1) {
			result.write(buffer, 0, length);
		}
		return result.toString("UTF-8");
	}

}
