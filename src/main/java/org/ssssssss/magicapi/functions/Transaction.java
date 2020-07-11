package org.ssssssss.magicapi.functions;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.ssssssss.script.annotation.UnableCall;

public class Transaction {

	@UnableCall
	private DataSourceTransactionManager dataSourceTransactionManager;

	@UnableCall
	private TransactionStatus transactionStatus;

	@UnableCall
	private static final TransactionDefinition TRANSACTION_DEFINITION = new DefaultTransactionDefinition();

	public Transaction(DataSourceTransactionManager dataSourceTransactionManager) {
		this.dataSourceTransactionManager = dataSourceTransactionManager;
		this.transactionStatus = dataSourceTransactionManager.getTransaction(TRANSACTION_DEFINITION);
	}

	/**
	 * 回滚
	 */
	public void rollback(){
		this.dataSourceTransactionManager.rollback(this.transactionStatus);
	}

	/**
	 * 提交
	 */
	public void commit(){
		this.dataSourceTransactionManager.commit(this.transactionStatus);
	}
}
