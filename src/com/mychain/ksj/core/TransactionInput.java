package com.mychain.ksj.core;

public class TransactionInput {
	public String transactionOutputId;	//Reference to TransactionOutputs -> transactionId
	public TransactionOutput UTXO; 		//Contains the Unspent transaction output
	
	//TransactionOutput들 중에서 사용되지 않은 것들을 기록
	public TransactionInput(String transactionOutputId) {
		this.transactionOutputId = transactionOutputId;
	}
}