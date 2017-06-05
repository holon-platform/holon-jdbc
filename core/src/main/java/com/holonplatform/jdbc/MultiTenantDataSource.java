package com.holonplatform.jdbc;

import java.io.Closeable;
import java.sql.Connection;

import javax.sql.DataSource;

import com.holonplatform.core.tenancy.TenantResolver;
import com.holonplatform.jdbc.internal.DefaultMultiTenantDataSource;

/**
 * {@link DataSource} with multi-tenancy support.
 * <p>
 * This datasource act as a wrapper for standard {@link DataSource} interface and provides a specific DataSource
 * implementation for the current tenant when a {@link Connection} is requested.
 * </p>
 * <p>
 * Standard implementation relies on two strategy interfaces for internal operations:
 * <ul>
 * <li>A {@link TenantResolver} to obtain the current tenant id</li>
 * <li>A {@link TenantDataSourceProvider} to obtain the concrete {@link DataSource} associated with a tenant id</li>
 * </ul>
 * 
 * @since 4.3.0
 */
public interface MultiTenantDataSource extends DataSource, Closeable {

	/**
	 * Reset tenant {@link DataSource}s internal cache.
	 */
	void reset();

	/**
	 * Reset internal cached {@link DataSource} for given <code>tenantId</code>, if present.
	 * @param tenantId Tenant id to reset
	 */
	void reset(String tenantId);

	/**
	 * Builder to create a MultiTenantDataSource
	 * @return Builder
	 */
	static Builder builder() {
		return new DefaultMultiTenantDataSource.DefaultBuilder();
	}

	// Builder

	public interface Builder {

		/**
		 * Set the {@link TenantResolver} to use to obtain the current tenant id.
		 * <p>
		 * If not setted, the default context resource {@link TenantResolver#getCurrent()} is used if available.
		 * </p>
		 * @param resolver the TenantResolver to set
		 * @return this
		 */
		Builder resolver(TenantResolver resolver);

		/**
		 * Set the {@link TenantDataSourceProvider} to use to obtain configured DataSource instances according to
		 * current tenant id.
		 * <p>
		 * If not setted, the default context resource {@link TenantDataSourceProvider#getCurrent()} is used if
		 * available.
		 * </p>
		 * @param provider the TenantDataSourceProvider to set
		 * @return this
		 */
		Builder provider(TenantDataSourceProvider provider);

		/**
		 * Build {@link MultiTenantDataSource}
		 * @return MultiTenantDataSource
		 */
		MultiTenantDataSource build();

	}

}
