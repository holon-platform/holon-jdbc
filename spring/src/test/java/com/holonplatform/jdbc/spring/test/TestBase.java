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

import java.util.Properties;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.FactoryBean;

import com.holonplatform.core.internal.utils.TestUtils;
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

		Assert.assertTrue(cfg.getConfigPropertyValue(SpringDataSourceConfigProperties.INITIALIZE, false));

	}

	@Test
	public void testFactoryBean() {
		TestUtils.expectedException(BeanInitializationException.class, new Callable<DataSource>() {

			@Override
			public DataSource call() throws Exception {
				FactoryBean<DataSource> fb = new DataSourceFactoryBean(
						SpringDataSourceConfigProperties.builder().withPropertySource(new Properties()).build());
				return fb.getObject();
			}
		});
	}

}
