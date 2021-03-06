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
package com.holonplatform.jdbc.spring.test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.FactoryBean;

import com.holonplatform.jdbc.DataSourceConfigProperties;
import com.holonplatform.jdbc.spring.SpringDataSourceConfigProperties;
import com.holonplatform.jdbc.spring.internal.DataSourceFactoryBean;

public class TestBase {

	@Test
	public void testProperties() {

		final Properties props = new Properties();
		props.put(DataSourceConfigProperties.DEFAULT_NAME + "." + SpringDataSourceConfigProperties.INITIALIZE.getKey(),
				"true");

		SpringDataSourceConfigProperties cfg = SpringDataSourceConfigProperties.builder().withPropertySource(props)
				.build();

		assertTrue(cfg.getConfigPropertyValue(SpringDataSourceConfigProperties.INITIALIZE, false));

	}

	@Test
	public void testFactoryBean() {
		assertThrows(BeanInitializationException.class, () -> {
			FactoryBean<DataSource> fb = new DataSourceFactoryBean(
					SpringDataSourceConfigProperties.builder().withPropertySource(new Properties()).build());
			fb.getObject();
		});
	}

}
