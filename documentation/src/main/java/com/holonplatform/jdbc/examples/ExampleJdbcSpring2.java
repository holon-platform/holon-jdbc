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
package com.holonplatform.jdbc.examples;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.holonplatform.jdbc.spring.EnableDataSource;

public class ExampleJdbcSpring2 {

	// tag::config[]
	@Configuration
	@PropertySource("datasource.properties")
	static class Config {

		@Configuration
		@EnableDataSource(dataContextId = "one") // <1>
		static class Config1 {
		}

		@Configuration
		@EnableDataSource(dataContextId = "two") // <2>
		static class Config2 {
		}

	}

	@Autowired
	@Qualifier("one") // <3>
	private DataSource dataSource1;

	@Autowired
	@Qualifier("two")
	private DataSource dataSource2;
	// end::config[]

}
