package org.ssssssss.magicapi.modules.db;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.ssssssss.script.annotation.Comment;

/**
 * 事务模块
 *
 * @author mxd
 */
public class Transaction {

	private static final TransactionDefinition TRANSACTION_DEFINITION = new DefaultTransactionDefinition();
	private final DataSourceTransactionManager dataSourceTransactionManager;
	private final TransactionStatus transactionStatus;

	public Transaction(DataSourceTransactionManager dataSourceTransactionManager) {
		this.dataSourceTransactionManager = dataSourceTransactionManager;
		this.transactionStatus = dataSourceTransactionManager.getTransaction(TRANSACTION_DEFINITION);
	}

	/**
	 * 回滚事务
	 */
	@Comment("回滚事务")
	public void rollback() {
		this.dataSourceTransactionManager.rollback(this.transactionStatus);
	}

	/**
	 * 提交事务
	 */
	@Comment("提交事务")
	public void commit() {
		this.dataSourceTransactionManager.commit(this.transactionStatus);
	}
}
