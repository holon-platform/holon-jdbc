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

import javax.sql.DataSource;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.holonplatform.jdbc.DataSourceBuilder;
import com.holonplatform.jdbc.DataSourceConfigProperties;
import com.holonplatform.jdbc.spring.EnableDataSource;

/**
 * Factory bean for automatic {@link DataSource} factory bean registration using {@link EnableDataSource} annotation.
 * 
 * @since 5.0.0
 */
public class DataSourceFactoryBean implements FactoryBean<DataSource>, BeanClassLoaderAware, InitializingBean {

	/**
	 * ClassLoader
	 */
	private ClassLoader beanClassLoader;

	/**
	 * Configuration properties
	 */
	private final DataSourceConfigProperties configuration;

	/**
	 * Builder
	 */
	private DataSourceBuilder dataSourceBuilder;

	/**
	 * Constructor
	 * @param configuration Configuration properties
	 */
	public DataSourceFactoryBean(DataSourceConfigProperties configuration) {
		super();
		this.configuration = configuration;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO post processor for init scripts
		this.dataSourceBuilder = DataSourceBuilder.create(beanClassLoader);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.BeanClassLoaderAware#setBeanClassLoader(java.lang.ClassLoader)
	 */
	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	@Override
	public DataSource getObject() throws Exception {
		if (dataSourceBuilder == null) {
			throw new BeanInitializationException("DataSourceBuilder not initialized");
		}
		return dataSourceBuilder.build(configuration);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	@Override
	public Class<?> getObjectType() {
		return DataSource.class;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	@Override
	public boolean isSingleton() {
		return true;
	}

}
