package org.ssssssss.magicapi.modules;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.ssssssss.script.annotation.Comment;

/**
 * 事务模块
 */
public class Transaction {

	private final DataSourceTransactionManager dataSourceTransactionManager;

	private final TransactionStatus transactionStatus;

	private static final TransactionDefinition TRANSACTION_DEFINITION = new DefaultTransactionDefinition();

	public Transaction(DataSourceTransactionManager dataSourceTransactionManager) {
		this.dataSourceTransactionManager = dataSourceTransactionManager;
		this.transactionStatus = dataSourceTransactionManager.getTransaction(TRANSACTION_DEFINITION);
	}

	/**
	 * 回滚事务
	 */
	@Comment("回滚事务")
	public void rollback(){
		this.dataSourceTransactionManager.rollback(this.transactionStatus);
	}

	/**
	 * 提交事务
	 */
	@Comment("提交事务")
	public void commit(){
		this.dataSourceTransactionManager.commit(this.transactionStatus);
	}
}
