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
package com.holonplatform.jdbc.spring.boot.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.holonplatform.jdbc.internal.DefaultBasicDataSource;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("test3")
public class TestSkipDefaultAutoConfig {

	@Configuration
	@EnableAutoConfiguration
	protected static class Config {

		@Bean
		public DataSource dataSource() {
			DefaultBasicDataSource ds = new DefaultBasicDataSource();
			ds.setUrl("jdbc:h2:mem:testdbx");
			ds.setUsername("sa");
			return ds;
		}

	}

	@Autowired
	private DataSource dataSource;

	@Test
	public void testDataSource() throws SQLException {
		assertNotNull(dataSource);

		assertTrue(dataSource instanceof DefaultBasicDataSource);

		try (Connection c = dataSource.getConnection()) {
			assertNotNull(c);
		}
	}

}
