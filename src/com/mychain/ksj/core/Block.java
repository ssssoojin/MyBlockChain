package com.mychain.ksj.core;

import java.util.ArrayList;
import java.util.Date;

import com.mychain.ksj.util.StringUtil;

public class Block {
	public String hash; //해시값
	public String previousHash; //이전 블럭의 해시값
	public String merkleRoot;
	public ArrayList<Transaction> transactions = new ArrayList<Transaction>(); //our data will be a simple message.
	public long timeStamp; //as number of milliseconds since 1/1/1970.
	public int nonce;
	
	//새로운 블럭을 생성  
	public Block(String previousHash ) {
		this.previousHash = previousHash;
		this.timeStamp = new Date().getTime();
		
		this.hash = calculateHash(); //생성시 먼저 hash 값을 하나 만들어 넣어둡니다.
	}
	
	//새로운 해시값을 생성합니다.
	public String calculateHash() {
		String calculatedhash = StringUtil.applySha256( 
				previousHash +
				Long.toString(timeStamp) +
				Integer.toString(nonce) + 
				merkleRoot
				);
		return calculatedhash;
	}
	
	//블럭안의 변수들 값의 해시값들이 특정 자릿수(the number of 0's)로 시작할때까지 시도
	public void mineBlock(int difficulty) {
		merkleRoot = StringUtil.getMerkleRoot(transactions);
		String target = StringUtil.getDificultyString(difficulty); //Create a string with difficulty * "0" 
		//생성된 hash가 target과 동일하면 채굴 성공입니다.
		//ex) difficulty가 3이면 target은 000이 되고, 생성된 hash가 000으로 시작하는 값이면 채굴 성공
		// 채굴된 모든 hash가 000으로 시작
		while(!hash.substring( 0, difficulty).equals(target)) {
			nonce ++;
			hash = calculateHash();
		}
		System.out.println("채굴 성공!!! : " + hash);
	}
	
	//Add transactions to this block
	public boolean addTransaction(Transaction transaction) {
		//process transaction and check if valid, unless block is genesis block then ignore.
		if(transaction == null) return false;		
		if((!"0".equals(previousHash))) {
			if((transaction.processTransaction() != true)) {
				System.out.println("Transaction failed to process. Discarded.");
				return false;
			}
		}

		transactions.add(transaction);
		System.out.println("Transaction Successfully added to Block");
		return true;
	}
	
}
