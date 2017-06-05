/*
 * Copyright 2000-2016 Holon TDCN.
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
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import com.holonplatform.jdbc.spring.EnableDataSource;

public class ExampleJdbcSpring1 {

	// tag::multiple1[]
	@EnableDataSource(dataContextId = "one")
	@Configuration
	class Config1 {
	}

	@EnableDataSource(dataContextId = "two")
	@Configuration
	class Config2 {
	}

	@PropertySource("datasource.properties")
	@Import({ Config1.class, Config2.class })
	@Configuration
	class OverallConfig {

	}

	class MyBean {

		@Autowired
		@Qualifier("one")
		private DataSource dataSource1;

		@Autowired
		@Qualifier("two")
		private DataSource dataSource2;

	}
	// end::multiple1[]

}
