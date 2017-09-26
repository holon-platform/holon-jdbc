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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.holonplatform.jdbc.internal.BasicDataSource;
import com.holonplatform.jdbc.spring.EnableDataSource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestEnableDataSourceBasic.Config.class)
public class TestEnableDataSourceBasic {

	@Configuration
	@PropertySource("test.properties")
	@EnableDataSource(dataContextId = "basic")
	protected static class Config {
	}

	@Autowired
	private DataSource dataSource;

	@Test
	public void testDataSource() {

		assertNotNull(dataSource);

		assertTrue(dataSource instanceof BasicDataSource);

	}

}
