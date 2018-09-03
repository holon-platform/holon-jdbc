/*
 * Copyright 2016-2018 Axioma srl.
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
package com.holonplatform.jdbc.internal.transaction;

import java.util.Optional;

import com.holonplatform.jdbc.transaction.JdbcTransactionOptions;
import com.holonplatform.jdbc.transaction.TransactionIsolation;

/**
 * Default {@link JdbcTransactionOptions} implementation.
 *
 * @since 5.2.0
 */
public class DefaultJdbcTransactionOptions implements JdbcTransactionOptions {

	private TransactionIsolation transactionIsolation;

	/**
	 * Default constructor.
	 */
	public DefaultJdbcTransactionOptions() {
		this(null);
	}

	/**
	 * Constructor with isolation specification.
	 * @param transactionIsolation The transaction isolation to use.
	 */
	public DefaultJdbcTransactionOptions(TransactionIsolation transactionIsolation) {
		super();
		this.transactionIsolation = transactionIsolation;
	}

	/**
	 * Set the transaction isolation level.
	 * @param transactionIsolation the transaction isolation level to set
	 */
	public void setTransactionIsolation(TransactionIsolation transactionIsolation) {
		this.transactionIsolation = transactionIsolation;
	}

	/*
	 * (non-Javadoc)
	 * @see com.holonplatform.jdbc.transaction.JdbcTransactionOptions#getTransactionIsolation()
	 */
	@Override
	public Optional<TransactionIsolation> getTransactionIsolation() {
		return Optional.ofNullable(transactionIsolation);
	}

}
