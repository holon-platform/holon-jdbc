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

import java.util.Optional;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.holonplatform.core.datastore.transaction.Transaction;
import com.holonplatform.core.datastore.transaction.TransactionConfiguration;
import com.holonplatform.core.internal.utils.ObjectUtils;
import com.holonplatform.jdbc.transaction.JdbcTransactionOptions;

/**
 * Base {@link Transaction} implementation which uses a Spring
 * {@link PlatformTransactionManager} to manage the actual transaction.
 * 
 * {@link #startTransaction()} and {@link #endTransaction()} methods should be
 * used to handle transaction lifecycle.
 *
 * @since 5.1.0
 */
public abstract class SpringManagedTransaction implements Transaction {

	private static final String MSG_TX_NOT_ACTIVE = "The transaction is not active";

	/**
	 * Transaction manager
	 */
	private final PlatformTransactionManager transactionManager;

	/**
	 * Transaction configuration
	 */
	private final TransactionConfiguration configuration;

	/**
	 * Current transaction reference
	 */
	private TransactionStatus transactionStatus;

	/**
	 * Constructor.
	 * @param transactionManager The {@link PlatformTransactionManager} to use (not
	 *                           null)
	 * @param configuration      The transaction configuration (not null)
	 */
	public SpringManagedTransaction(PlatformTransactionManager transactionManager,
			TransactionConfiguration configuration) {
		super();
		ObjectUtils.argumentNotNull(transactionManager, "PlatformTransactionManager must be not null");
		ObjectUtils.argumentNotNull(configuration, "TransactionConfiguration must be not null");
		this.transactionManager = transactionManager;
		this.configuration = configuration;
	}

	/**
	 * Get the {@link PlatformTransactionManager}.
	 * @return the transaction manager
	 */
	protected PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	/**
	 * Get the transaction configuration.
	 * @return the transaction configuration
	 */
	public TransactionConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * Get the current {@link TransactionStatus}, if available.
	 * @return the current transaction status if the transaction has been started,
	 *         an empty Optional otherwise
	 */
	protected Optional<TransactionStatus> getTransactionStatus() {
		return Optional.ofNullable(transactionStatus);
	}

	/**
	 * Start a transaction
	 * @throws TransactionException If an error occurred
	 */
	protected void startTransaction() throws TransactionException {
		if (getTransactionStatus().isPresent()) {
			throw new TransactionException("The transaction has already been started");
		}

		// transaction definition
		DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
		getIsolationLevel(getConfiguration()).ifPresent(i -> definition.setIsolationLevel(i));

		// start transaction
		transactionStatus = getTransactionManager().getTransaction(definition);
	}

	/**
	 * Finalize the transaction.
	 * @throws TransactionException If an error occurred
	 */
	protected void endTransaction() throws TransactionException {
		getTransactionStatus().ifPresent(tx -> {
			if (!tx.isCompleted()) {
				try {
					if (isRollbackOnly()) {
						getTransactionManager().rollback(tx);
					} else {
						if (getConfiguration().isAutoCommit()) {
							getTransactionManager().commit(tx);
						}
					}
				} catch (Exception e) {
					throw new TransactionException("Failed to finalize transaction", e);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.holonplatform.core.datastore.transaction.Transaction#commit()
	 */
	@Override
	public boolean commit() throws TransactionException {
		final TransactionStatus tx = getTransactionStatus()
				.orElseThrow(() -> new IllegalTransactionStatusException(MSG_TX_NOT_ACTIVE));
		if (tx.isCompleted()) {
			throw new IllegalTransactionStatusException(
					"Cannot commit the transaction: the transaction is already completed");
		}
		try {
			// check rollback only
			if (isRollbackOnly()) {
				getTransactionManager().rollback(tx);
				return false;
			} else {
				getTransactionManager().commit(tx);
				return true;
			}
		} catch (Exception e) {
			throw new TransactionException("Failed to commit the transaction", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.holonplatform.core.datastore.transaction.Transaction#rollback()
	 */
	@Override
	public void rollback() throws TransactionException {
		final TransactionStatus tx = getTransactionStatus()
				.orElseThrow(() -> new IllegalTransactionStatusException(MSG_TX_NOT_ACTIVE));
		if (tx.isCompleted()) {
			throw new IllegalTransactionStatusException(
					"Cannot commit the transaction: the transaction is already completed");
		}
		try {
			getTransactionManager().rollback(tx);
		} catch (Exception e) {
			throw new TransactionException("Failed to rollback the transaction", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.holonplatform.core.internal.datastore.transaction.AbstractTransaction#
	 * isCompleted()
	 */
	@Override
	public boolean isCompleted() {
		return getTransactionStatus().map(tx -> tx.isCompleted()).orElse(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.holonplatform.core.datastore.transaction.Transaction#setRollbackOnly()
	 */
	@Override
	public void setRollbackOnly() {
		getTransactionStatus().orElseThrow(() -> new IllegalTransactionStatusException(MSG_TX_NOT_ACTIVE))
				.setRollbackOnly();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.holonplatform.core.datastore.transaction.Transaction#isRollbackOnly()
	 */
	@Override
	public boolean isRollbackOnly() {
		return getTransactionStatus().orElseThrow(() -> new IllegalTransactionStatusException(MSG_TX_NOT_ACTIVE))
				.isRollbackOnly();
	}

	/**
	 * Get the transaction isolation level according to given
	 * {@link TransactionConfiguration}.
	 * @param transactionConfiguration transaction configuration
	 * @return Optional isolation level
	 */
	private static Optional<Integer> getIsolationLevel(TransactionConfiguration transactionConfiguration) {
		if (transactionConfiguration == null) {
			return Optional.empty();
		}
		return transactionConfiguration.getTransactionOptions().filter(o -> o instanceof JdbcTransactionOptions)
				.map(o -> (JdbcTransactionOptions) o).flatMap(o -> o.getTransactionIsolation()).map(i -> {
					switch (i) {
					case NONE:
						return TransactionDefinition.ISOLATION_DEFAULT;
					case READ_COMMITTED:
						return TransactionDefinition.ISOLATION_READ_COMMITTED;
					case READ_UNCOMMITTED:
						return TransactionDefinition.ISOLATION_READ_UNCOMMITTED;
					case REPEATABLE_READ:
						return TransactionDefinition.ISOLATION_REPEATABLE_READ;
					case SERIALIZABLE:
						return TransactionDefinition.ISOLATION_SERIALIZABLE;
					default:
						break;
					}
					return null;
				});
	}

}
