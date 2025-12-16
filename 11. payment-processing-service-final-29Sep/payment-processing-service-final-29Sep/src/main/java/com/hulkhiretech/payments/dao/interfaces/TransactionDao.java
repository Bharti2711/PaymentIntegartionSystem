package com.hulkhiretech.payments.dao.interfaces;

import com.hulkhiretech.payments.entity.TransactionEntity;

public interface TransactionDao {
	
	public Integer insertTransaction(TransactionEntity txn);
	
	public TransactionEntity getTransactionByTxnReference(String txnReference);
	
	public TransactionEntity getTransactionByProviderReference(String providerReference, int providerId);
	
	public Integer updateTransactionStatusDetailsByTxnReference(TransactionEntity txn);

}
