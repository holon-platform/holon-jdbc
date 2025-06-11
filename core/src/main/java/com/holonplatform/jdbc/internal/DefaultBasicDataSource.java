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

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;

import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.jdbc.BasicDataSource;

/**
 * Default {@link BasicDataSource} implementation.
 * 
 * @since 5.0.0
 */
public class DefaultBasicDataSource implements BasicDataSource {

	/*
	 * Logger
	 */
	private static final Logger LOGGER = JdbcLogger.create();

	/*
	 * Configuration property: JDBC connection url
	 */
	private String url;

	/*
	 * Configuration property: connection username
	 */
	private String username;

	/*
	 * Configuration property: connection password
	 */
	private String password;

	/*
	 * Connection properties
	 */
	private Properties connectionProperties;

	/*
	 * Optional Driver class
	 */
	private Driver driver;

	/**
	 * Set JDBC URL to use for connections
	 * @param url Connection URL
	 * @see java.sql.Driver#connect(String, java.util.Properties)
	 */
	public void setUrl(String url) {
		ObjectUtils.argumentNotNull(url, "JDBC url must be not null");
		if (url.trim().equals("")) {
			throw new IllegalArgumentException("JDBC url must not be empty");
		}
		this.url = url.trim();
	}

	/**
	 * JDBC URL to use to use for connections
	 * @return JDBC connection URL
	 */
	public String getUrl() {
		return this.url;
	}

	/**
	 * Set JDBC connection username
	 * @param username JDBC connection username
	 * @see java.sql.Driver#connect(String, java.util.Properties)
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * JDBC connection username
	 * @return JDBC connection username
	 */
	public String getUsername() {
		return this.username;
	}

	/**
	 * Set JDBC connection password
	 * @param password JDBC connection password
	 * @see java.sql.Driver#connect(String, java.util.Properties)
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * JDBC connection password
	 * @return JDBC connection password
	 */
	public String getPassword() {
		return this.password;
	}

	/**
	 * Set JDBC Driver class name to use
	 * @param driverClassName JDBC Driver class name
	 */
	@SuppressWarnings("unchecked")
	public void setDriverClassName(String driverClassName) {
		ObjectUtils.argumentNotNull(driverClassName, "Driver class must be not null");
		try {
			Class<?> driverClass = Class.forName(driverClassName.trim(), true, getClass().getClassLoader());

			if (!Driver.class.isAssignableFrom(driverClass)) {
				throw new IllegalStateException("Class: " + driverClassName + " is not a valid JDBC Driver class");
			}

			setDriverClass((Class<? extends Driver>) driverClass);

		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Failed to load JDBC driver class: " + driverClassName, e);
		}
	}

	/**
	 * Set JDBC Driver class to use
	 * @param driverClass JDBC Driver class
	 */
	public void setDriverClass(Class<? extends Driver> driverClass) {
		ObjectUtils.argumentNotNull(driverClass, "Driver class must be not null");
		try {
			this.driver = driverClass.getDeclaredConstructor().newInstance();
		} catch (InstantiationException e) {
			throw new IllegalStateException("Failed to load JDBC driver class: " + driverClass.getName(), e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Failed to load JDBC driver class: " + driverClass.getName(), e);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("Failed to load JDBC driver class: " + driverClass.getName(), e);
		} catch (InvocationTargetException e) {
			throw new IllegalStateException("Failed to load JDBC driver class: " + driverClass.getName(), e);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException("Failed to load JDBC driver class: " + driverClass.getName(), e);
		} catch (SecurityException e) {
			throw new IllegalStateException("Failed to load JDBC driver class: " + driverClass.getName(), e);
		}
		LOGGER.debug(() -> "Loaded JDBC driver: " + driverClass.getName());
	}

	/**
	 * Optional JDBC Driver
	 * @return JDBC Driver
	 */
	protected Driver getDriver() {
		return driver;
	}

	/**
	 * Specify arbitrary connection properties as key/value pairs to be passed to the Driver.
	 * @param connectionProperties Connection properties
	 * @see java.sql.Driver#connect(String, java.util.Properties)
	 */
	public void setConnectionProperties(Properties connectionProperties) {
		this.connectionProperties = connectionProperties;
	}

	/**
	 * Connection properties to be passed to the Driver
	 * @return Connection properties
	 */
	public Properties getConnectionProperties() {
		return this.connectionProperties;
	}

	/**
	 * LogWriter methods are not supported.
	 */
	@Override
	public PrintWriter getLogWriter() throws SQLException {
		throw new UnsupportedOperationException("getLogWriter");
	}

	/**
	 * LogWriter methods are not supported.
	 */
	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		throw new UnsupportedOperationException("setLogWriter");
	}

	/**
	 * Setting a login timeout is not supported.
	 */
	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		throw new UnsupportedOperationException("setLoginTimeout");
	}

	/**
	 * Returns 0, indicating the default system timeout is to be used.
	 * @return 0 (default timeout)
	 */
	@Override
	public int getLoginTimeout() throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see jakarta.sql.CommonDataSource#getParentLogger()
	 */
	@Override
	public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return java.util.logging.Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if (iface.isInstance(this)) {
			return (T) this;
		}
		throw new SQLException(
				"DataSource of type " + getClass().getName() + " cannot be unwrapped as " + iface.getName());
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return iface.isInstance(this);
	}

	/*
	 * (non-Javadoc)
	 * @see jakarta.sql.DataSource#getConnection()
	 */
	@Override
	public Connection getConnection() throws SQLException {
		return obtainConnection(getUsername(), getPassword());
	}

	/*
	 * (non-Javadoc)
	 * @see jakarta.sql.DataSource#getConnection(java.lang.String, java.lang.String)
	 */
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return obtainConnection(username, password);
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
	}

	/**
	 * Get a connection from Driver, using connection properties and including the given username and
	 * password, if any.
	 * @param username Connection username
	 * @param password Connection password
	 * @return obtained Connection
	 * @throws SQLException Failed to obtain a connection
	 * @see java.sql.Driver#connect(String, java.util.Properties)
	 */
	protected Connection obtainConnection(String username, String password) throws SQLException {
		Properties properties = new Properties();
		Properties connectionProperties = getConnectionProperties();
		if (connectionProperties != null) {
			properties.putAll(connectionProperties);
		}
		if (username != null) {
			properties.setProperty("user", username);
		}
		if (password != null) {
			properties.setProperty("password", password);
		}
		return obtainConnection(properties);
	}

	/**
	 * Obtain a Connection from Driver using the given properties
	 * @param properties Connection properties
	 * @return obtained Connection
	 * @throws SQLException Failed to obtain a connection
	 */
	protected Connection obtainConnection(Properties properties) throws SQLException {
		String url = getUrl();
		LOGGER.debug(() -> "Creating new DriverManager connection to [" + url + "]");
		if (getDriver() != null) {
			return getConnectionFromDriver(url, getDriver(), properties);
		}
		return getConnectionFromDriverManager(url, properties);
	}

	/**
	 * Get a Connection from JDBC DriverManager
	 * @param url Connection URL
	 * @param properties Connection properties
	 * @return obtained Connection
	 * @throws SQLException Failed to obtain a connection
	 * @see java.sql.DriverManager#getConnection(String, java.util.Properties)
	 */
	protected Connection getConnectionFromDriverManager(String url, Properties properties) throws SQLException {
		return DriverManager.getConnection(url, properties);
	}

	/**
	 * Get a Connection from JDBC Driver class
	 * @param url Connection URL
	 * @param driver Driver class
	 * @param properties Connection properties
	 * @return obtained Connection Failed to obtain a connection
	 * @throws SQLException Failed to obtain a connection
	 * @see Driver#connect(String, Properties)
	 */
	protected Connection getConnectionFromDriver(String url, Driver driver, Properties properties) throws SQLException {
		return driver.connect(url, properties);
	}

	/**
	 * Default {@link Builder} implementation.
	 */
	public static class DefaultBuilder implements Builder {

		private final DefaultBasicDataSource dataSource = new DefaultBasicDataSource();

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.jdbc.BasicDataSource.Builder#url(java.lang.String)
		 */
		@Override
		public Builder url(String url) {
			dataSource.setUrl(url);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.jdbc.BasicDataSource.Builder#username(java.lang.String)
		 */
		@Override
		public Builder username(String username) {
			dataSource.setUsername(username);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.jdbc.BasicDataSource.Builder#password(java.lang.String)
		 */
		@Override
		public Builder password(String password) {
			dataSource.setPassword(password);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.jdbc.BasicDataSource.Builder#driverClassName(java.lang.String)
		 */
		@Override
		public Builder driverClassName(String driverClassName) {
			dataSource.setDriverClassName(driverClassName);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.jdbc.BasicDataSource.Builder#driverClass(java.lang.Class)
		 */
		@Override
		public Builder driverClass(Class<? extends Driver> driverClass) {
			dataSource.setDriverClass(driverClass);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.jdbc.BasicDataSource.Builder#connectionProperties(java.util.Properties)
		 */
		@Override
		public Builder connectionProperties(Properties connectionProperties) {
			dataSource.setConnectionProperties(connectionProperties);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.jdbc.BasicDataSource.Builder#build()
		 */
		@Override
		public BasicDataSource build() {
			return dataSource;
		}

	}

}
