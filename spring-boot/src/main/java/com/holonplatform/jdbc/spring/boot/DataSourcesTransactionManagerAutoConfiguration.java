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
package com.holonplatform.jdbc.spring.boot;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;

import com.holonplatform.jdbc.spring.boot.internal.DataSourcesTransactionManagerAutoConfigurationRegistrar;

/**
 * Spring boot auto-configuration to register a {@link PlatformTransactionManager} bound to every
 * {@link DataSource} bean registered using <code>holon.datasource.*</code> configuration
 * properties.
 * 
 * @since 5.0.0
 */
@AutoConfiguration
@ConditionalOnClass(PlatformTransactionManager.class)
@AutoConfigureBefore(DataSourceTransactionManagerAutoConfiguration.class)
@AutoConfigureAfter(DataSourcesAutoConfiguration.class)
// @AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
public class DataSourcesTransactionManagerAutoConfiguration {

	@ConditionalOnMissingBean(PlatformTransactionManager.class)
	@Configuration
	@Import(DataSourcesTransactionManagerAutoConfigurationRegistrar.class)
	static class TransactionManagementConfiguration {

	}

}
