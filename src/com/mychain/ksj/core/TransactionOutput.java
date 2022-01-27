package com.mychain.ksj.core;

import java.security.PublicKey;

import com.mychain.ksj.util.StringUtil;


public class TransactionOutput {
	public String id;
	public PublicKey reciepient; //also known as the new owner of these coins.
	public float value; //the amount of coins they own
	public String parentTransactionId; //the id of the transaction this output was created in
	
	//Constructor
	//트랜젹션으로부터 각 구성원들에게 보내진 최종 금액을 보여줌
	//이 정보는 새로운 트랜잭션의 인풋으로 기록되고, 당신이 보낼 코인이 남아있다는 것을 확인하는용도로 쓰임
	public TransactionOutput(PublicKey reciepient, float value, String parentTransactionId) {
		this.reciepient = reciepient;
		this.value = value;
		this.parentTransactionId = parentTransactionId;
		this.id = StringUtil.applySha256(StringUtil.getStringFromKey(reciepient)+Float.toString(value)+parentTransactionId);
	}
	
	//Check if coin belongs to you
	public boolean isMine(PublicKey publicKey) {
		return (publicKey == reciepient);
	}
	
}