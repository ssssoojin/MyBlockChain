package com.mychain.ksj.core;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

import com.mychain.ksj.main.MyBlockChain;
import com.mychain.ksj.util.StringUtil;



public class Transaction {
	
	public String transactionId; //Contains a hash of transaction*
	public PublicKey sender; // senders address/public key. 계좌를 보내는 사람의 public key
	public PublicKey reciepient; // Recipients address/public key. 계좌를 받는 사람의 public key
	public float value;
	
	//암호화된 시그니쳐, 암호화된 서명을 통해 주소의 실제 주인이 트댄잭션을 통해 돈을 보내는지 확인가능, 
	//데이터변화감지(제 3자가 보내는 금액을 바꾸는것을 방지하는 역할)
	//기능 1. 실제 코인의 오너만이 그 코인을 사용할 수 있게 허락해주는 역할
	//기능 2. 새로운 블럭이 생성되기 전 이미 접수된 트랜잭션에 대해서 다른 사람들이 수정하지 못하도록 방지
	public byte[] signature; // this is to prevent anybody else from spending funds in our wallet.
	
	public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
	
	private static int sequence = 0; //A rough count of how many transactions have been generated 
	
	// Constructor: 
	public Transaction(PublicKey from, PublicKey to, float value,  ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.reciepient = to;
		this.value = value;
		this.inputs = inputs;
	}
	
	public boolean processTransaction() {
		
		if(verifySignature() == false) {
			System.out.println("#Transaction Signature failed to verify");
			return false;
		}
				
		//Gathers transaction inputs (Making sure they are unspent):
		for(TransactionInput i : inputs) {
			i.UTXO = MyBlockChain.UTXOs.get(i.transactionOutputId);
		}

		//Checks if transaction is valid:
		if(getInputsValue() < MyBlockChain.minimumTransaction) {
			System.out.println("Transaction Inputs too small: " + getInputsValue());
			System.out.println("Please enter the amount greater than " + MyBlockChain.minimumTransaction);
			return false;
		}
		
		//Generate transaction outputs:
		float leftOver = getInputsValue() - value; //get value of inputs then the left over change:
		transactionId = calulateHash();
		outputs.add(new TransactionOutput( this.reciepient, value,transactionId)); //send value to recipient
		outputs.add(new TransactionOutput( this.sender, leftOver,transactionId)); //send the left over 'change' back to sender		
				
		//Add outputs to Unspent list
		for(TransactionOutput o : outputs) {
			MyBlockChain.UTXOs.put(o.id , o);
		}
		
		//Remove transaction inputs from UTXO lists as spent:
		for(TransactionInput i : inputs) {
			if(i.UTXO == null) continue; //if Transaction can't be found skip it 
			MyBlockChain.UTXOs.remove(i.UTXO.id);
		}
		
		return true;
	}
	
	public float getInputsValue() {
		float total = 0;
		for(TransactionInput i : inputs) {
			if(i.UTXO == null) continue; //if Transaction can't be found skip it, This behavior may not be optimal.
			total += i.UTXO.value;
		}
		return total;
	}
	
	public void generateSignature(PrivateKey privateKey) {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value)	;
		signature = StringUtil.applyECDSASig(privateKey,data);		
	}
	
	public boolean verifySignature() {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value)	;
		return StringUtil.verifyECDSASig(sender, data, signature);
	}
	
	public float getOutputsValue() {
		float total = 0;
		for(TransactionOutput o : outputs) {
			total += o.value;
		}
		return total;
	}
	
	private String calulateHash() {
		sequence++; //increase the sequence to avoid 2 identical transactions having the same hash
		return StringUtil.applySha256(
				StringUtil.getStringFromKey(sender) +
				StringUtil.getStringFromKey(reciepient) +
				Float.toString(value) + sequence
				);
	}
}