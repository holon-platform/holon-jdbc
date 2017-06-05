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
package com.holonplatform.jdbc.spring.boot.internal;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import com.holonplatform.jdbc.spring.internal.DataSourceRegistrar;
import com.holonplatform.spring.internal.DataContextBoundBeanDefinition;
import com.holonplatform.spring.internal.PrimaryMode;

/**
 * DataSource transaction manager registrar.
 * 
 * @since 5.0.0
 */
public class DataSourcesTransactionManagerAutoConfigurationRegistrar
		implements ImportBeanDefinitionRegistrar, BeanFactoryAware {

	private BeanFactory beanFactory;

	/*
	 * (non-Javadoc)
	 * @see
	 * org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.springframework.context.annotation.ImportBeanDefinitionRegistrar#registerBeanDefinitions(org.springframework.
	 * core.type.AnnotationMetadata, org.springframework.beans.factory.support.BeanDefinitionRegistry)
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

		if (beanFactory instanceof ListableBeanFactory) {

			String[] dataSourceBeanNames = ((ListableBeanFactory) beanFactory).getBeanNamesForType(DataSource.class,
					false, true);
			if (dataSourceBeanNames != null) {
				for (String dataSourceBeanName : dataSourceBeanNames) {
					BeanDefinition bd = registry.getBeanDefinition(dataSourceBeanName);
					if (bd instanceof DataContextBoundBeanDefinition) {
						DataSourceRegistrar.registerDataSourceTransactionManager(registry, dataSourceBeanName,
								((DataContextBoundBeanDefinition) bd).getDataContextId().orElse(null),
								PrimaryMode.AUTO);
					}
				}
			}

		}

	}

}
