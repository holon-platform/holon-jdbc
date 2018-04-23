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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import com.holonplatform.core.internal.utils.ClassUtils;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.jdbc.exceptions.DataSourceInitializationException;

/**
 * Helper class to initialize a {@link DataSource} using SQL init scripts.
 *
 * @since 5.1.0
 */
public final class DataSourceInitializer {

	private DataSourceInitializer() {
	}

	/**
	 * Init given {@link DataSource} using provided SQL scripts.
	 * @param dataSource The DataSource to init
	 * @param sqlScripts SQL scripts
	 * @throws DataSourceInitializationException If an error occurred
	 */
	public static void initDataSourceFromSQL(DataSource dataSource, String... sqlScripts)
			throws DataSourceInitializationException {
		if (sqlScripts != null) {
			initDataSourceFromSQL(dataSource, Arrays.asList(sqlScripts));
		}
	}

	/**
	 * Init given {@link DataSource} using provided SQL scripts.
	 * @param dataSource The DataSource to init
	 * @param sqlScripts SQL scripts
	 * @throws DataSourceInitializationException If an error occurred
	 */
	public static void initDataSourceFromSQL(DataSource dataSource, List<String> sqlScripts)
			throws DataSourceInitializationException {
		ObjectUtils.argumentNotNull(dataSource, "DataSource must be not null");
		if (sqlScripts != null && !sqlScripts.isEmpty()) {
			for (String sqlScript : sqlScripts) {
				try (Connection connection = dataSource.getConnection()) {
					SQLScriptUtils.executeSqlScript(connection, sqlScript);
				} catch (SQLException | IOException e) {
					throw new DataSourceInitializationException(
							"Failed to initialize DataSource using provided SQL scripts", e);
				}
			}
		}
	}

	/**
	 * Init given {@link DataSource} using provided SQL scripts resource names.
	 * @param dataSource The DataSource to init
	 * @param scriptResourceNames SQL scripts resource names
	 * @throws DataSourceInitializationException If an error occurred
	 */
	public static void initDataSourceFromSQLResources(DataSource dataSource, String... scriptResourceNames)
			throws DataSourceInitializationException {
		if (scriptResourceNames != null) {
			initDataSourceFromSQLResources(dataSource, Arrays.asList(scriptResourceNames));
		}
	}

	/**
	 * Init given {@link DataSource} using provided SQL scripts resource names.
	 * @param dataSource The DataSource to init
	 * @param scriptResourceNames SQL scripts resource names
	 * @throws DataSourceInitializationException If an error occurred
	 */
	public static void initDataSourceFromSQLResources(DataSource dataSource, List<String> scriptResourceNames)
			throws DataSourceInitializationException {
		ObjectUtils.argumentNotNull(dataSource, "DataSource must be not null");
		if (scriptResourceNames != null && !scriptResourceNames.isEmpty()) {
			for (String sqlScriptResource : scriptResourceNames) {
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
