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

import javax.sql.DataSource;

/**
 * {@link DataSource} types enumeration.
 *
 * @since 5.1.0
 */
public enum DataSourceType {

	/**
	 * Basic DataSource
	 */
	BASIC(DataSourceBuilder.TYPE_BASIC),

	/**
	 * HikariCP DataSource
	 */
	HIKARICP(DataSourceBuilder.TYPE_HIKARICP),

	/**
	 * DBCP 2 DataSource
	 */
	DBCP(DataSourceBuilder.TYPE_DBCP),

	/**
	 * Tomcat pooling DataSource
	 */
	TOMCAT(DataSourceBuilder.TYPE_TOMCAT);

	private final String type;

	private DataSourceType(String type) {
		this.type = type;
	}

	/**
	 * Get the type name.
	 * @return the type name
	 */
	public String getType() {
		return type;
	}

}
