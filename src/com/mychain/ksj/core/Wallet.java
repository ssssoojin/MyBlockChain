package com.mychain.ksj.core;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.mychain.ksj.main.MyBlockChain;

public class Wallet {
	//privateKey는 다른 사람에 의해서 손상되고 싶지 않은 데이터에 싸인을 하는 역할
	//publicKey는 이 서명을 확인하는 역할
	public PrivateKey privateKey; //트렌젝션에 사인하는 기능 수행, 주인외에 코인사용못하게-->비밀스럽게 유지해야함
	public PublicKey publicKey; //우리의 주소 역할, 트렌젝션에 보내서 유효성확인 + 다른사람이 건드렸는지 확인
		
	public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
	public Wallet() {
		generateKeyPair();
	}
	
	//KeyPair생성하기 위해 Elliptic-curve-cryptography사용-->import java.security.KeyPairGenerator
	public void generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			
			// Key generator 초기화 및 키생성하기
			keyGen.initialize(ecSpec, random); //256 
	        KeyPair keyPair = keyGen.generateKeyPair();
	        
	        // 공개키와 개인키 저장
	        privateKey = keyPair.getPrivate();
	        publicKey = keyPair.getPublic();
	        
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public float getBalance() {
		float total = 0;	
        for (Map.Entry<String, TransactionOutput> item: MyBlockChain.UTXOs.entrySet()){
        	TransactionOutput UTXO = item.getValue();
            if(UTXO.isMine(publicKey)) { //if output belongs to me ( if coins belong to me )
            	UTXOs.put(UTXO.id,UTXO); //add it to our list of unspent transactions.
            	total += UTXO.value ; 
            }
        }  
		return total;
	}
	
	public Transaction sendFunds(PublicKey _recipient,float value ) {
		if(getBalance() < value) {
			System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
			return null;
		}
		ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
		
		float total = 0;
		for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()){
			TransactionOutput UTXO = item.getValue();
			total += UTXO.value;
			inputs.add(new TransactionInput(UTXO.id));
			if(total > value) break;
		}
		
		Transaction newTransaction = new Transaction(publicKey, _recipient , value, inputs);
		newTransaction.generateSignature(privateKey);
		
		for(TransactionInput input: inputs){
			UTXOs.remove(input.transactionOutputId);
		}
		
		return newTransaction;
	}
}
