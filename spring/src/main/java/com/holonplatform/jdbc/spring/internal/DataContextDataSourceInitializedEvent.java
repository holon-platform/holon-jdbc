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
package com.holonplatform.jdbc.spring.internal;

import javax.sql.DataSource;

import org.springframework.context.ApplicationEvent;

/**
 * {@link ApplicationEvent} used internally to trigger {@link DataSource} initialization.
 * 
 * @since 5.0.0
 */
@SuppressWarnings("serial")
public class DataContextDataSourceInitializedEvent extends ApplicationEvent {

	private final String dataContextId;

	/**
	 * Create a new {@link DataContextDataSourceInitializedEvent}.
	 * @param source the source {@link DataSource}
	 * @param dataContextId Data context id of the DataSource
	 */
	public DataContextDataSourceInitializedEvent(DataSource source, String dataContextId) {
		super(source);
		this.dataContextId = dataContextId;
	}

	/**
	 * Gets the data context id of the DataSource
	 * @return the data context id
	 */
	public String getDataContextId() {
		return dataContextId;
	}

}
