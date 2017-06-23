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
package com.holonplatform.jdbc.spring.internal;

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.holonplatform.core.internal.Logger;
import com.holonplatform.jdbc.DataSourceConfigProperties;
import com.holonplatform.jdbc.spring.EnableDataSource;
import com.holonplatform.jdbc.spring.SpringDataSourceConfigProperties;
import com.holonplatform.spring.EnvironmentConfigPropertyProvider;
import com.holonplatform.spring.internal.AbstractConfigPropertyRegistrar;
import com.holonplatform.spring.internal.BeanRegistryUtils;
import com.holonplatform.spring.internal.GenericDataContextBoundBeanDefinition;
import com.holonplatform.spring.internal.PrimaryMode;
import com.holonplatform.spring.internal.SpringLogger;

/**
 * Bean registration class to register a {@link DataSource} using {@link DataSourceConfigProperties} configuration
 * properties and {@link EnableDataSource} annotations.
 * 
 * <p>
 * NOTE: This class is intended for internal framework use only.
 * </p>
 * 
 * @since 5.0.0
 */
public class DataSourceRegistrar extends AbstractConfigPropertyRegistrar {

	/*
	 * Logger
	 */
	private static final Logger LOGGER = SpringLogger.create();

	/*
	 * (non-Javadoc)
	 * @see
	 * org.springframework.context.annotation.ImportBeanDefinitionRegistrar#registerBeanDefinitions(org.springframework.
	 * core.type.AnnotationMetadata, org.springframework.beans.factory.support.BeanDefinitionRegistry)
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {

		if (!annotationMetadata.isAnnotated(EnableDataSource.class.getName())) {
			// ignore call from sub classes
			return;
		}

		Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(EnableDataSource.class.getName());

		// Annotation values
		String dataContextId = BeanRegistryUtils.getAnnotationValue(attributes, "dataContextId", null);
		PrimaryMode primaryMode = BeanRegistryUtils.getAnnotationValue(attributes, "primary", PrimaryMode.AUTO);
		boolean registerTransactionManager = BeanRegistryUtils.getAnnotationValue(attributes,
				"enableTransactionManager", false);

		String dsBeanName = registerDataSource(getEnvironment(), registry, dataContextId, primaryMode);

		// Transaction manager

		if (registerTransactionManager) {
			registerDataSourceTransactionManager(registry, dsBeanName, dataContextId, primaryMode);
		}

	}

	/**
	 * Register a {@link DataSource} bean
	 * @param environment Environment as configuration properties source
	 * @param registry BeanDefinitionRegistry
	 * @param dataContextId Data context id
	 * @param primaryMode Primary mode
	 * @return Registered DataSource bean name
	 */
	public static String registerDataSource(Environment environment, BeanDefinitionRegistry registry,
			String dataContextId, PrimaryMode primaryMode) {
		return registerDataSource(
				SpringDataSourceConfigProperties.builder(dataContextId)
						.withPropertySource(EnvironmentConfigPropertyProvider.create(environment)).build(),
				registry, dataContextId, primaryMode);
	}

	/**
	 * Register a {@link DataSource} bean
	 * @param dataSourceConfigProperties DataSource configuration properties
	 * @param registry BeanDefinitionRegistry
	 * @param dataContextId Data context id
	 * @param primaryMode Primary mode
	 * @return Registered DataSource bean name
	 */
	public static String registerDataSource(SpringDataSourceConfigProperties dataSourceConfigProperties,
			BeanDefinitionRegistry registry, String dataContextId, PrimaryMode primaryMode) {

		boolean primary = PrimaryMode.TRUE == primaryMode;
		if (PrimaryMode.AUTO == primaryMode && dataSourceConfigProperties.isPrimary()) {
			primary = true;
		}

		GenericDataContextBoundBeanDefinition definition = new GenericDataContextBoundBeanDefinition();
		definition.setDataContextId(dataContextId);
		definition.setAutowireCandidate(true);
		definition.setPrimary(primary);
		definition.setBeanClass(DataSourceFactoryBean.class);

		ConstructorArgumentValues avs = new ConstructorArgumentValues();
		avs.addIndexedArgumentValue(0, dataSourceConfigProperties);
		definition.setConstructorArgumentValues(avs);

		if (dataContextId != null) {
			definition.addQualifier(new AutowireCandidateQualifier(Qualifier.class, dataContextId));
		}

		String dsBeanName = BeanRegistryUtils.buildBeanName(dataContextId,
				EnableDataSource.DEFAULT_DATASOURCE_BEAN_NAME);

		registry.registerBeanDefinition(dsBeanName, definition);

		StringBuilder log = new StringBuilder();
		if (dataContextId != null) {
			log.append("<Data context id: ");
			log.append(dataContextId);
			log.append("> ");
		}
		log.append("Registered DataSource bean with name \"");
		log.append(dsBeanName);
		log.append("\"");
		if (dataContextId != null) {
			log.append(" and qualifier \"");
			log.append(dataContextId);
			log.append("\"");
		}
		LOGGER.info(log.toString());

		// Initializer
		if (dataSourceConfigProperties.isInitialize()) {
			definition = new GenericDataContextBoundBeanDefinition();
			definition.setDataContextId(dataContextId);
			definition.setAutowireCandidate(false);
			definition.setBeanClass(DataContextDataSourceInitializer.class);
			definition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

			avs = new ConstructorArgumentValues();
			avs.addIndexedArgumentValue(0, dsBeanName);
			avs.addIndexedArgumentValue(1, dataSourceConfigProperties);
			definition.setConstructorArgumentValues(avs);

			registry.registerBeanDefinition(
					BeanRegistryUtils.buildBeanName(dataContextId, DataContextDataSourceInitializer.BEAN_NAME),
					definition);
		}

		return dsBeanName;
	}

	/**
	 * Register a {@link PlatformTransactionManager} bound to given {@link DataSource} bean name
	 * @param registry Bean definitions registry
	 * @param dataSourceBeanName DataSource bean name
	 * @param dataContextId Data context id
	 * @param primaryMode Primary mode
	 * @return Registered transaction manager bean name
	 */
	public static String registerDataSourceTransactionManager(BeanDefinitionRegistry registry,
			String dataSourceBeanName, String dataContextId, PrimaryMode primaryMode) {

		boolean primary = PrimaryMode.TRUE == primaryMode;
		if (PrimaryMode.AUTO == primaryMode && registry.containsBeanDefinition(dataSourceBeanName)) {
			BeanDefinition bd = registry.getBeanDefinition(dataSourceBeanName);
			if (bd.isPrimary()) {
				primary = true;
			}
		}

		GenericDataContextBoundBeanDefinition definition = new GenericDataContextBoundBeanDefinition();
		definition.setDataContextId(dataContextId);
		definition.setAutowireCandidate(true);
		definition.setPrimary(primary);
		definition.setBeanClass(DataSourceTransactionManager.class);

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("dataSource", new RuntimeBeanReference(dataSourceBeanName));
		definition.setPropertyValues(pvs);

		if (dataContextId != null) {
			definition.addQualifier(new AutowireCandidateQualifier(Qualifier.class, dataContextId));
		}

		String tmBeanName = BeanRegistryUtils.buildBeanName(dataContextId,
				EnableDataSource.DEFAULT_TRANSACTIONMANAGER_BEAN_NAME);

		registry.registerBeanDefinition(tmBeanName, definition);

		StringBuilder log = new StringBuilder();
		if (dataContextId != null) {
			log.append("<Data context id: ");
			log.append(dataContextId);
			log.append("> ");
		}
		log.append("Registered DataSourceTransactionManager bean with name \"");
		log.append(tmBeanName);
		log.append("\"");
		if (dataContextId != null) {
			log.append(" and qualifier \"");
			log.append(dataContextId);
			log.append("\"");
		}
		LOGGER.info(log.toString());

		return tmBeanName;
	}

}
