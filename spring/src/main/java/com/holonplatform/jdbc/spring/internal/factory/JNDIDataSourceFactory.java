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
package com.holonplatform.jdbc.spring.internal.factory;

import jakarta.annotation.Priority;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

import com.holonplatform.core.config.ConfigPropertySet.ConfigurationException;
import com.holonplatform.core.internal.Logger;
import com.holonplatform.jdbc.DataSourceConfigProperties;
import com.holonplatform.jdbc.DataSourceFactory;
import com.holonplatform.jdbc.internal.DefaultDataSourceBuilderConfiguration;
import com.holonplatform.jdbc.internal.JdbcLogger;

/**
 * A {@link DataSourceFactory} to obtain the {@link DataSource} instance using JNDI.
 * 
 * @since 5.0.0
 */
@Priority(Integer.MAX_VALUE - 100)
public class JNDIDataSourceFactory implements DataSourceFactory {

	private static final long serialVersionUID = 8116894990294911480L;

	/**
	 * Type name
	 */
	public static final String TYPE_NAME = "JNDI";

	/**
	 * Logger
	 */
	private static final Logger LOGGER = JdbcLogger.create();

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.jdbc.DataSourceFactory#getDataSourceType()
	 */
	@Override
	public String getDataSourceType() {
		return TYPE_NAME;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.jdbc.DataSourceFactory#build(com.holonplatform.jdbc.DataSourceConfigProperties)
	 */
	@Override
	public DataSource build(DataSourceConfigProperties configurationProperties) throws ConfigurationException {

		final String dataContextId = configurationProperties.getDataContextId().orElse(null);

		final String jndiName = configurationProperties.getConfigPropertyValue(DataSourceConfigProperties.JNDI_NAME,
				null);
		if (jndiName == null || jndiName.trim().equals("")) {
			throw new ConfigurationException(
					DefaultDataSourceBuilderConfiguration.buildMissingJNDINameMessage(dataContextId));
		}

		try {

			DataSource dataSource = lookupJNDIDataSource(jndiName);

			LOGGER.debug(() -> "(Data context id: " + dataContextId + "): " + "DataSource initialized using JNDI name "
					+ jndiName);

			return dataSource;

		} catch (Exception e) {
			throw new ConfigurationException("Failed to load DataSource using JNDI name: " + jndiName, e);
		}
	}

	/**
	 * Lookup a DataSource with given JNDI name in context.
	 * @param jndiName JNDI name
	 * @return DataSource instance
	 * @throws NamingException Failed to obtain a DataSource with given name
	 */
	private static DataSource lookupJNDIDataSource(String jndiName) throws NamingException {
		JndiDataSourceLookup dataSourceLookup = new JndiDataSourceLookup();
		return dataSourceLookup.getDataSource(jndiName);
	}

}
