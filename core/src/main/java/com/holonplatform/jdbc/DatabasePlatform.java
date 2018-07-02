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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.holonplatform.core.internal.utils.ClassUtils;
import com.holonplatform.core.internal.utils.ObjectUtils;

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
	DB2_AS400("com.ibm.as400.access.AS400JDBCDriver", "com.ibm.as400.access.AS400JDBCXADataSource",
			"SELECT 1 FROM SYSIBM.SYSDUMMY1", "jdbc:as400:"),

	/**
	 * Apache Derby
	 */
	DERBY("org.apache.derby.jdbc.EmbeddedDriver", "org.apache.derby.jdbc.EmbeddedXADataSource",
			"SELECT 1 FROM SYSIBM.SYSDUMMY1", "jdbc:derby:"),

	/**
	 * H2
	 */
	H2("org.h2.Driver", "org.h2.jdbcx.JdbcDataSource", "SELECT 1", "jdbc:h2:"),

	/**
	 * HSQL
	 */
	HSQL("org.hsqldb.jdbc.JDBCDriver", "org.hsqldb.jdbc.pool.JDBCXADataSource",
			"SELECT COUNT(*) FROM INFORMATION_SCHEMA.SYSTEM_USERS", "jdbc:hsqldb:"),

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
	ORACLE("oracle.jdbc.OracleDriver", "oracle.jdbc.xa.client.OracleXADataSource", "SELECT 1 from DUAL",
			"jdbc:oracle:"),

	/**
	 * PostgreSQL
	 */
	POSTGRESQL("org.postgresql.Driver", "org.postgresql.xa.PGXADataSource", "SELECT 1", "jdbc:postgresql:"),

	/**
	 * Microsoft SQLServer
	 */
	SQL_SERVER("com.microsoft.sqlserver.jdbc.SQLServerDriver", "com.microsoft.sqlserver.jdbc.SQLServerXADataSource",
			"SELECT 1", "jdbc:sqlserver:"),

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
		if (this == DatabasePlatform.MYSQL && isMySQLConnectorJ8Present(ClassUtils.getDefaultClassLoader())) {
			return "com.mysql.cj.jdbc.Driver";
		}
		return driverClassName;
	}

	/**
	 * XA JDBC driver class name
	 * @return the XA driver class name
	 */
	public String getXaDriverClassName() {
		if (this == DatabasePlatform.MYSQL && isMySQLConnectorJ8Present(ClassUtils.getDefaultClassLoader())) {
			return "com.mysql.cj.jdbc.MysqlXADataSource";
		}
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
			final String url = jdbcUrl.trim().toLowerCase();
			// other platforms
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

	/**
	 * MySQL Connector/J 8+ presence in classpath for classloader
	 */
	private static final Map<ClassLoader, Boolean> MYSQL_CONNECTORJ_8_PRESENT = new WeakHashMap<>();

	/**
	 * Checks whether MySQL Connector/J 8+ is available from classpath
	 * @param classLoader ClassLoader to use (not null)
	 * @return <code>true</code> if present
	 */
	private static boolean isMySQLConnectorJ8Present(ClassLoader classLoader) {
		ObjectUtils.argumentNotNull(classLoader, "ClassLoader must be not null");
		return MYSQL_CONNECTORJ_8_PRESENT.computeIfAbsent(classLoader,
				cl -> ClassUtils.isPresent("com.mysql.cj.jdbc.Driver", cl));
	}

}
