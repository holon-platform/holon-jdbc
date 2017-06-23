/*
 * Copyright 2000-2016 Holon TDCN.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enumeration for common database platforms
 * 
 * @since 5.0.0
 */
public enum DatabasePlatform {

	/**
	 * Denote a not specified platform
	 */
	NONE(null, null, null),

	/**
	 * IBM DB2
	 */
	DB2("com.ibm.db2.jcc.DB2Driver", "com.ibm.db2.jcc.DB2XADataSource", "SELECT 1 FROM SYSIBM.SYSDUMMY1", "jdbc:db2:"),

	/**
	 * IBM DB2 AS400
	 */
	DB2_AS400("com.ibm.as400.access.AS400JDBCDriver", "com.ibm.as400.access.AS400JDBCXADataSource", "SELECT 1 FROM SYSIBM.SYSDUMMY1", "jdbc:db2:"),

	/**
	 * Apache Derby
	 */
	DERBY("org.apache.derby.jdbc.EmbeddedDriver", "org.apache.derby.jdbc.EmbeddedXADataSource", "SELECT 1 FROM SYSIBM.SYSDUMMY1", "jdbc:derby:"),

	/**
	 * H2
	 */
	H2("org.h2.Driver", "org.h2.jdbcx.JdbcDataSource", "SELECT 1", "jdbc:h2:"),

	/**
	 * HSQL
	 */
	HSQL("org.hsqldb.jdbc.JDBCDriver", "org.hsqldb.jdbc.pool.JDBCXADataSource", "SELECT COUNT(*) FROM INFORMATION_SCHEMA.SYSTEM_USERS", "jdbc:hsqldb:"),

	/**
	 * IBN Informix
	 */
	INFORMIX("com.informix.jdbc.IfxDriver", null, "select count(*) from systables", "jdbc:informix-sqli:"),

	/**
	 * MySQL
	 */
	MYSQL("com.mysql.jdbc.Driver", "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource", "SELECT 1", "jdbc:mysql:"),

	/**
	 * MariaDB
	 */
	MARIADB("org.mariadb.jdbc.Driver", "org.mariadb.jdbc.MariaDbDataSource", "SELECT 1", "jdbc:mariadb:"),

	/**
	 * SAP HANA
	 */
	HANA("com.sap.db.jdbc.Driver", null, "SELECT 'Hello' FROM DUMMY", "jdbc:sap:"),

	/**
	 * Oracle
	 */
	ORACLE("oracle.jdbc.OracleDriver", "oracle.jdbc.xa.client.OracleXADataSource", "SELECT 1 from DUAL", "jdbc:oracle:"),

	/**
	 * PostgreSQL
	 */
	POSTGRESQL("org.postgresql.Driver", "org.postgresql.xa.PGXADataSource", "SELECT 1", "jdbc:postgresql:"),

	/**
	 * Microsoft SQLServer
	 */
	SQL_SERVER("com.microsoft.sqlserver.jdbc.SQLServerDriver", "com.microsoft.sqlserver.jdbc.SQLServerXADataSource", "SELECT 1", "jdbc:sqlserver:"),

	/**
	 * SQLite
	 */
	SQLITE("org.sqlite.JDBC", null, null, "jdbc:sqlite:");

	private final String driverClassName;
	private final String xaDriverClassName;
	private final String validationQuery;
	private final List<String> connectionUrlPrefix;

	private DatabasePlatform(String driverClassName, String xaDriverClassName, String validationQuery,
			String... prefixes) {
		this.driverClassName = driverClassName;
		this.xaDriverClassName = xaDriverClassName;
		this.validationQuery = validationQuery;
		this.connectionUrlPrefix = (prefixes == null) ? Collections.emptyList() : Arrays.asList(prefixes);
	}

	/**
	 * JDBC driver class name
	 * @return Driver class name
	 */
	public String getDriverClassName() {
		return driverClassName;
	}

	/**
	 * XA JDBC driver class name
	 * @return the XA driver class name
	 */
	public String getXaDriverClassName() {
		return xaDriverClassName;
	}

	/**
	 * Get connection validation query
	 * @return Connection validation query, or <code>null</code> if not available
	 */
	public String getValidationQuery() {
		return validationQuery;
	}

	/**
	 * Try to guess DatabasePlatform from given JDBC connection url
	 * @param jdbcUrl JDBC connection url
	 * @return Matching DatabasePlatform, or <code>null</code> if unknown
	 */
	public static DatabasePlatform fromUrl(String jdbcUrl) {
		if (jdbcUrl != null) {
			String url = jdbcUrl.trim().toLowerCase();
			for (DatabasePlatform dp : values()) {
				if (dp.connectionUrlPrefix != null) {
					for (String prefix : dp.connectionUrlPrefix) {
						if (url.startsWith(prefix)) {
							return dp;
						}
					}
				}
			}
		}
		return null;
	}

}
