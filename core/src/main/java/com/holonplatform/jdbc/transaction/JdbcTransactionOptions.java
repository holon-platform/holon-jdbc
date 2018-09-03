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
package com.holonplatform.jdbc.transaction;

import java.util.Optional;

import com.holonplatform.core.datastore.transaction.TransactionOptions;
import com.holonplatform.jdbc.internal.transaction.DefaultJdbcTransactionOptions;

/**
 * JDBC {@link TransactionOptions}.
 *
 * @since 5.2.0
 */
public interface JdbcTransactionOptions extends TransactionOptions {

	/**
	 * Get the transaction isolation level to use.
	 * @return Optional transaction isolation level
	 */
	Optional<TransactionIsolation> getTransactionIsolation();

	/**
	 * Create a new {@link JdbcTransactionOptions} specifying the transaction isolation level.
	 * @param isolation The transaction isolation level
	 * @return A new {@link JdbcTransactionOptions} instance with given transaction isolation level
	 */
	static JdbcTransactionOptions using(TransactionIsolation isolation) {
		return new DefaultJdbcTransactionOptions(isolation);
	}

}
