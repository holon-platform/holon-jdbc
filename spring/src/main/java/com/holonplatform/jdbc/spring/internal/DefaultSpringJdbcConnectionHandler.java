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
package com.holonplatform.jdbc.spring.internal;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceUtils;

import com.holonplatform.core.internal.Logger;
import com.holonplatform.jdbc.internal.JdbcLogger;
import com.holonplatform.jdbc.spring.SpringJdbcConnectionHandler;

/**
 * Default {@link SpringJdbcConnectionHandler}, which uses {@link DataSourceUtils} to obtain and release a
 * Spring-managed transactional connection.
 *
 * @since 5.1.0
 */
public class DefaultSpringJdbcConnectionHandler implements SpringJdbcConnectionHandler {

	private static final Logger LOGGER = JdbcLogger.create();

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.jdbc.JdbcConnectionHandler#getConnection(javax.sql.DataSource,
	 * com.holonplatform.jdbc.JdbcConnectionHandler.ConnectionType)
	 */
	@Override
	public Connection getConnection(DataSource dataSource, ConnectionType connectionType) throws SQLException {
		Connection connection = DataSourceUtils.doGetConnection(dataSource);
		LOGGER.debug(() -> "Obtained a Spring-managed connection: [" + connection + "]");
		return connection;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.jdbc.JdbcConnectionHandler#releaseConnection(java.sql.Connection, javax.sql.DataSource,
	 * com.holonplatform.jdbc.JdbcConnectionHandler.ConnectionType)
	 */
	@Override
	public void releaseConnection(Connection connection, DataSource dataSource, ConnectionType connectionType)
			throws SQLException {
		DataSourceUtils.doReleaseConnection(connection, dataSource);
		LOGGER.debug(() -> "Spring-managed connection released: [" + connection + "]");

	}

}
