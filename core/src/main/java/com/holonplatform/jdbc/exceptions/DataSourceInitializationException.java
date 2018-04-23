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
package com.holonplatform.jdbc.exceptions;

import javax.sql.DataSource;

/**
 * Exception related to {@link DataSource} initialization errors.
 *
 * @since 5.1.0
 */
public class DataSourceInitializationException extends RuntimeException {

	private static final long serialVersionUID = -8988434693060111410L;

	/**
	 * Constructor with error message.
	 * @param message Error message
	 */
	public DataSourceInitializationException(String message) {
		super(message);
	}

	/**
	 * Constructor with error message and nested exception.
	 * @param message Error message
	 * @param cause Nested exception
	 */
	public DataSourceInitializationException(String message, Throwable cause) {
		super(message, cause);
	}

}
