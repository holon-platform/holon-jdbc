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

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.sql.DataSource;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.holonplatform.core.internal.Logger;
import com.holonplatform.core.tenancy.TenantResolver;
import com.holonplatform.jdbc.MultiTenantDataSource;
import com.holonplatform.jdbc.TenantDataSourceProvider;

/**
 * Default {@link MultiTenantDataSource} implementation.
 * <p>
 * An internal cache is used to store and reuse tenant DataSource instances.
 * </p>
 * 
 * @since 4.3.0
 */
public class DefaultMultiTenantDataSource implements MultiTenantDataSource {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = JdbcLogger.create();

	/**
	 * Default tenant id representing not tenant available
	 */
	private static final String NO_TENANT = DefaultMultiTenantDataSource.class.getName() + ".NO_TENANT";

	/**
	 * Tenant resolver
	 */
	private TenantResolver tenantResolver;

	/**
	 * Tenant DataSource provider
	 */
	private TenantDataSourceProvider tenantDataSourceProvider;

	/**
	 * Tenant DataSources cache
	 */
	protected final ConcurrentMap<String, DataSource> tenantDataSources;

	/**
	 * Constructor
	 */
	public DefaultMultiTenantDataSource() {
		super();
		this.tenantDataSources = new ConcurrentHashMap<>();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.jdbc.tenancy.MultiTenantDataSource#reset()
	 */
	@Override
	public void reset() {
		tenantDataSources.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.jdbc.tenancy.MultiTenantDataSource#reset(java.lang.String)
	 */
	@Override
	public void reset(String tenantId) {
		tenantDataSources.remove((tenantId == null) ? NO_TENANT : tenantId);
	}

	/**
	 * Set the {@link TenantResolver} to use to obtain the current tenant id.
	 * <p>
	 * If not setted, the default context resource {@link TenantResolver#getCurrent()} is used if available.
	 * </p>
	 * @param tenantResolver the TenantResolver to set
	 */
	public void setTenantResolver(TenantResolver tenantResolver) {
		this.tenantResolver = tenantResolver;
	}

	/**
	 * Set the {@link TenantDataSourceProvider} to use to obtain configured DataSource instances according to current
	 * tenant id.
	 * <p>
	 * If not setted, the default context resource {@link TenantDataSourceProvider#getCurrent()} is used if available.
	 * </p>
	 * @param tenantDataSourceProvider the TenantDataSourceProvider to set
	 */
	public void setTenantDataSourceProvider(TenantDataSourceProvider tenantDataSourceProvider) {
		this.tenantDataSourceProvider = tenantDataSourceProvider;
	}

	/**
	 * Gets the {@link TenantResolver} to use to obtain the current tenant id.
	 * @return the TenantResolver. If not explicitly setted, {@link TenantResolver#getCurrent()} is returned if
	 *         available
	 */
	protected Optional<TenantResolver> getTenantResolver() {
		return (tenantResolver != null) ? Optional.of(tenantResolver) : TenantResolver.getCurrent();
	}

	/**
	 * Gets the {@link TenantDataSourceProvider} to use to obtain configured DataSource instances according to current
	 * tenant id.
	 * @return the TenantDataSourceProvider
	 */
	protected Optional<TenantDataSourceProvider> getTenantDataSourceProvider() {
		return (tenantDataSourceProvider != null) ? Optional.of(tenantDataSourceProvider)
				: TenantDataSourceProvider.getCurrent();
	}

	/**
	 * Determine concrete DataSource to use relying on {@link TenantResolver#getTenantId()} to resolve current tenant id
	 * and {@link TenantDataSourceProvider#getDataSource(String)} to obtain DataSource instance to use
	 * @return Concrete DataSource
	 * @throws SQLException Error resolving tenant id or building DataSource
	 */
	protected DataSource determineCurrentDataSource() throws SQLException {
		try {

			// resolve current tenant id
			TenantResolver resolver = getTenantResolver()
					.orElseThrow(() -> new SQLException("Failed to resolve tenant DataSource: Missing TenantResolver"));

			String tenantId = resolver.getTenantId().orElse(NO_TENANT);

			LOGGER.debug(() -> "Try to resolve DataSource for tenant id: " + tenantId);

			// get the tenant DataSource provider
			TenantDataSourceProvider provider = getTenantDataSourceProvider().orElseThrow(
					() -> new SQLException("Failed to resolve tenant DataSource: Missing TenantDataSourceProvider"));

			// obtain the DataSource from cache or from provider
			DataSource dataSource = tenantDataSources.computeIfAbsent(tenantId,
					(id) -> provider.getDataSource(NO_TENANT.equals(id) ? null : id));
			if (dataSource == null) {
				throw new SQLException("Failed to resolve tenant DataSource - TenantDataSourceProvider returned "
						+ "a null DataSource for tenant id: " + tenantId);
			}

			LOGGER.debug(
					() -> "Resolved DataSource for tenant id: " + tenantId + " - DataSource instance: " + dataSource);

			return dataSource;

		} catch (Exception e) {
			throw new SQLException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see jakarta.sql.DataSource#getConnection()
	 */
	@Override
	public Connection getConnection() throws SQLException {
		return determineCurrentDataSource().getConnection();
	}

	/*
	 * (non-Javadoc)
	 * @see jakarta.sql.DataSource#getConnection(java.lang.String, java.lang.String)
	 */
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return determineCurrentDataSource().getConnection(username, password);
	}

	/*
	 * (non-Javadoc)
	 * @see jakarta.sql.CommonDataSource#getLogWriter()
	 */
	@Override
	public PrintWriter getLogWriter() throws SQLException {
		throw new SQLFeatureNotSupportedException("getLogWriter");
	}

	/*
	 * (non-Javadoc)
	 * @see jakarta.sql.CommonDataSource#setLogWriter(java.io.PrintWriter)
	 */
	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		throw new SQLFeatureNotSupportedException("setLogWriter");
	}

	/*
	 * (non-Javadoc)
	 * @see jakarta.sql.CommonDataSource#setLoginTimeout(int)
	 */
	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		throw new SQLFeatureNotSupportedException("setLoginTimeout");
	}

	/*
	 * (non-Javadoc)
	 * @see jakarta.sql.CommonDataSource#getLoginTimeout()
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
	@SuppressWarnings("unchecked")
	@Override
	public <I> I unwrap(Class<I> iface) throws SQLException {
		if (iface.isInstance(this)) {
			return (I) this;
		}
		return determineCurrentDataSource().unwrap(iface);
	}

	/*
	 * (non-Javadoc)
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return (iface.isInstance(this) || determineCurrentDataSource().isWrapperFor(iface));
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		if (!tenantDataSources.isEmpty()) {
			LinkedList<Throwable> exceptions = new LinkedList<>();

			for (DataSource dataSource : tenantDataSources.values()) {
				if (dataSource instanceof Closeable) {
					try {
						((Closeable) dataSource).close();
					} catch (Exception e) {
						exceptions.add(e);
					}
				}
			}

			if (!exceptions.isEmpty()) {
				if (exceptions.size() == 1) {
					throw new IOException(exceptions.getFirst());
				}
				StringBuilder sb = new StringBuilder();
				sb.append("Multiple exceptions detected when closing tenant DataSources: ");
				for (Throwable exception : exceptions) {
					sb.append(ExceptionUtils.getRootCauseMessage(exception));
					sb.append(";");
				}
				throw new IOException(sb.toString());
			}
		}
	}

	// Builder

	/**
	 * Default {@link MultiTenantDataSource} builder.
	 */
	public static class DefaultBuilder implements Builder {

		private final DefaultMultiTenantDataSource instance = new DefaultMultiTenantDataSource();

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.jdbc.tenancy.MultiTenantDataSource.Builder#resolver(com.holonplatform.core.tenancy.
		 * TenantResolver)
		 */
		@Override
		public Builder resolver(TenantResolver resolver) {
			this.instance.setTenantResolver(resolver);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.jdbc.tenancy.MultiTenantDataSource.Builder#provider(com.holonplatform.jdbc.tenancy.
		 * TenantDataSourceProvider)
		 */
		@Override
		public Builder provider(TenantDataSourceProvider provider) {
			this.instance.setTenantDataSourceProvider(provider);
			return this;
		}

		/*
		 * (non-Javadoc)
		 * @see com.holonplatform.jdbc.tenancy.MultiTenantDataSource.Builder#build()
		 */
		@Override
		public MultiTenantDataSource build() {
			return instance;
		}

	}

}
