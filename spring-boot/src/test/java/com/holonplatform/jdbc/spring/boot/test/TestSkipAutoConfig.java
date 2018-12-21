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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import com.holonplatform.jdbc.internal.DefaultBasicDataSource;
import com.zaxxer.hikari.HikariDataSource;

@SpringBootTest
@ActiveProfiles("test4")
public class TestSkipAutoConfig {

	@Configuration
	@EnableAutoConfiguration
	protected static class Config {

		@Bean(name = "dataSource_one")
		@Qualifier("custom")
		public DataSource dataSource() {
			DefaultBasicDataSource ds = new DefaultBasicDataSource();
			ds.setUrl("jdbc:h2:mem:testdb1");
			ds.setUsername("sa");
			return ds;
		}

	}

	@Autowired
	@Qualifier("custom")
	private DataSource dataSource1;

	@Autowired
	@Qualifier("two")
	private DataSource dataSource2;

	@Test
	public void testDataSource() throws SQLException {
		assertNotNull(dataSource1);
		assertNotNull(dataSource2);

		assertTrue(dataSource1 instanceof DefaultBasicDataSource);
		assertTrue(dataSource2 instanceof HikariDataSource);

		try (Connection c = dataSource1.getConnection()) {
			assertNotNull(c);
		}
		try (Connection c = dataSource2.getConnection()) {
			assertNotNull(c);
		}
	}

}
