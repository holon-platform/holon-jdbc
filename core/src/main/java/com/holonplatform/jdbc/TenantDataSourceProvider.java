package com.holonplatform.jdbc;

import java.util.Optional;

import javax.sql.DataSource;

import com.holonplatform.core.Context;

/**
 * Concrete tenant {@link DataSource} instance provider.
 * <p>
 * This interface is used by {@link MultiTenantDataSource} to obtain the concrete {@link DataSource} to use with the
 * current tenant id.
 * </p>
 * 
 * @since 4.3.0
 */
@FunctionalInterface
public interface TenantDataSourceProvider {

	/**
	 * Default {@link Context} resource reference
	 */
	public static final String CONTEXT_KEY = TenantDataSourceProvider.class.getName();

	/**
	 * Provides the concrete {@link DataSource} to use with given <code>tenantId</code>
	 * @param tenantId Tenant id
	 * @return the DataSource reference
	 */
	DataSource getDataSource(String tenantId);

	/**
	 * Convenience method to obtain the current {@link TenantDataSourceProvider} made available as {@link Context}
	 * resource, using default {@link ClassLoader}.
	 * <p>
	 * See {@link Context#resource(String, Class)} for details about context resources availability conditions.
	 * </p>
	 * @return Optional TenantDataSourceProvider, empty if not available as context resource
	 */
	static Optional<TenantDataSourceProvider> getCurrent() {
		return Context.get().resource(CONTEXT_KEY, TenantDataSourceProvider.class);
	}

}
