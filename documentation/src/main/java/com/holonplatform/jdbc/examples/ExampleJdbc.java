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

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import javax.sql.DataSource;

import com.holonplatform.jdbc.DataSourceBuilder;
import com.holonplatform.jdbc.DataSourceConfigProperties;
import com.holonplatform.jdbc.MultiTenantDataSource;
import com.holonplatform.jdbc.internal.BasicDataSource;

@SuppressWarnings("unused")
public class ExampleJdbc {

	public void configuration() throws IOException {
		// tag::configuration[]
		DataSourceConfigProperties config = DataSourceConfigProperties.builder().withDefaultPropertySources().build(); // <1>

		config = DataSourceConfigProperties.builder().withSystemPropertySource().build(); // <2>

		Properties props = new Properties();
		props.put("holon.datasource.url", "jdbc:h2:mem:testdb");
		config = DataSourceConfigProperties.builder().withPropertySource(props).build(); // <3>

		config = DataSourceConfigProperties.builder().withPropertySource("datasource.properties").build(); // <4>

		config = DataSourceConfigProperties.builder()
				.withPropertySource(
						Thread.currentThread().getContextClassLoader().getResourceAsStream("datasource.properties"))
				.build(); // <5>
		// end::configuration[]
	}

	public void configuration2() throws IOException {
		// tag::configuration2[]
		DataSourceConfigProperties config1 = DataSourceConfigProperties.builder("one")
				.withPropertySource("datasource.properties").build();

		DataSourceConfigProperties config2 = DataSourceConfigProperties.builder("two")
				.withPropertySource("datasource.properties").build();
		// end::configuration2[]
	}

	public void builder() throws IOException {
		// tag::builder[]
		DataSourceConfigProperties config = DataSourceConfigProperties.builder()
				.withPropertySource("datasource.properties").build(); // <1>

		DataSource dataSource = DataSourceBuilder.create().build(config); // <2>
		// end::builder[]
	}

	public void builder2() throws IOException {
		// tag::builder2[]
		DataSourceConfigProperties config = DataSourceConfigProperties.builder()
				.withPropertySource("datasource2.properties").build(); // <1>

		DataSource dataSource = DataSourceBuilder.create().build(config); // <2>
		// end::builder2[]
	}

	@SuppressWarnings("resource")
	public void multiTenant() throws IOException {
		// tag::multitenant[]
		MultiTenantDataSource dataSource = MultiTenantDataSource.builder().resolver(() -> Optional.of("test")) // <1>
				.provider(tenantId -> new BasicDataSource()) // <2>
				.build();
		// end::multitenant[]
	}

}
