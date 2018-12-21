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
package com.holonplatform.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.holonplatform.jdbc.internal.DefaultJdbcConnectionHandler;

/**
 * JDBC {@link Connection} handler.
 * <p>
 * The connection handler is in charge of getting a {@link Connection} from a {@link DataSource} and to release (close)
 * the connection itself.
 * </p>
 * <p>
 * The {@link ConnectionType} is provided, allowing to discern if the connection will be used for connection consumer
 * initialization ({@link ConnectionType#INIT}) or for the normal JDBC operations.
 * </p>
 * 
 * @since 5.1.0
 */
public interface JdbcConnectionHandler {

	/**
	 * Connection type
	 */
	public enum ConnectionType {

	/**
	 * The connection is used to initialize the underlying connection consumer
	 */
	INIT,

	/**
	 * The connection is used to perform normal JDBC calls by the underlying connection consumer
	 */
	DEFAULT;

	}

	/**
	 * Get a {@link Connection} using given {@link DataSource}.
	 * @param dataSource The {@link DataSource} from which to obtain connections
	 * @param connectionType Connection type to discern if the connection will be used for initialization or for normal
	 *        JDBC operations
	 * @return A connection, which must be not <code>null</code>
	 * @throws SQLException If an error occurred and the connection cannot be provided
	 */
	Connection getConnection(DataSource dataSource, ConnectionType connectionType) throws SQLException;

	/**
	 * Release given <code>connection</code>, performing any connection finalization operation (for example to close the
	 * connection).
	 * @param connection The connection to release
	 * @param dataSource The {@link DataSource} from which the connection was obtained
	 * @param connectionType Connection type to discern if the connection was used for initialization or for normal JDBC
	 *        operations
	 * @throws SQLException If an error occurred and the connection cannot be released
	 */
	void releaseConnection(Connection connection, DataSource dataSource, ConnectionType connectionType)
			throws SQLException;

	/**
	 * Create a default {@link JdbcConnectionHandler}, which uses {@link DataSource#getConnection()} to obtain a
	 * connection and {@link Connection#close()} to release the connection.
	 * @return A default {@link JdbcConnectionHandler} implementation
	 */
	static JdbcConnectionHandler create() {
		return new DefaultJdbcConnectionHandler();
	}

}
