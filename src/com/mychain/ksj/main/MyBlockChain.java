package com.mychain.ksj.main;

import java.security.Security;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import com.mychain.ksj.core.Block;
import com.mychain.ksj.core.Transaction;
import com.mychain.ksj.core.TransactionInput;
import com.mychain.ksj.core.TransactionOutput;
import com.mychain.ksj.core.Wallet;

public class MyBlockChain {
	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	
	//사용되지 않은 트랜잭션의 추가 컬랙션
	public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
	
	public static int difficulty = 3;
	public static float minimumTransaction = 0.1f;
	public static Wallet walletA;
	public static Wallet walletB;
	public static Transaction bitTransaction;

	public static void main(String[] args) {	
		
		
		//add our blocks to the blockchain ArrayList:
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncey castle as a Security Provider
		
		//Create wallets:
		walletA = new Wallet();
		walletB = new Wallet();		
		Wallet coinbase = new Wallet();
		
		walletA.generateKeyPair();
		walletB.generateKeyPair();
		coinbase.generateKeyPair();
		
		
		//create bit transaction, which sends 100 Coin to walletA: 
		bitTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
		bitTransaction.generateSignature(coinbase.privateKey);	 //manually sign the bit transaction	
		bitTransaction.transactionId = "0"; //manually set the transaction id
		bitTransaction.outputs.add(new TransactionOutput(bitTransaction.reciepient, bitTransaction.value, bitTransaction.transactionId)); //manually add the Transactions Output
		UTXOs.put(bitTransaction.outputs.get(0).id, bitTransaction.outputs.get(0)); //its important to store our first transaction in the UTXOs list.
		
		System.out.println("Creating and Mining bit block... ");
		Block bit = new Block("0");
		bit.addTransaction(bitTransaction);
		addBlock(bit);
		
		//testing
		Block block1 = new Block(bit.hash);
		System.out.println("\nWalletA 잔고 : " + walletA.getBalance());
		System.out.println("\nWalletA이 WalletB에게 40 보냄...");
		block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
		addBlock(block1);
		System.out.println("\nWalletA 잔고: " + walletA.getBalance());
		System.out.println("WalletB' 잔고: " + walletB.getBalance());	
		Block block2 = new Block(block1.hash);
		System.out.println("\nWalletA Attempting to send more funds (1000) than it has...");
		block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
		addBlock(block2);
		System.out.println("\nWalletA 잔고: " + walletA.getBalance());
		System.out.println("WalletB 잔고: " + walletB.getBalance());
		
		Block block3 = new Block(block2.hash);
		System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
		block3.addTransaction(walletB.sendFunds( walletA.publicKey, 20));
		System.out.println("\nWalletA 잔고: " + walletA.getBalance());
		System.out.println("WalletB 잔고: " + walletB.getBalance());
		
		isChainValid();
		
	}
	
	public static Boolean isChainValid() {
		Block currentBlock; 
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //a temporary working list of unspent transactions at a given block state.
		tempUTXOs.put(bitTransaction.outputs.get(0).id, bitTransaction.outputs.get(0));
		
		//전체 블럭을 체크합니다.
		for(int i=1; i < blockchain.size(); i++) {
			
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			
			//현재 블럭의 hash가 맞는지 체크합니다.
			if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
				System.out.println("#Current Hashes not equal");
				return false;
			}
			
			//이전 블럭의 hash값과 동일한지 체크합니다.
			if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
				System.out.println("#Previous Hashes not equal");
				return false;
			}
			//check if hash is solved
			if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
				System.out.println("#This block hasn't been mined");
				return false;
			}
			
			//loop thru blockchains transactions:
			TransactionOutput tempOutput;
			for(int t=0; t <currentBlock.transactions.size(); t++) {
				Transaction currentTransaction = currentBlock.transactions.get(t);
				
				if(!currentTransaction.verifySignature()) {
					System.out.println("#Signature on Transaction(" + t + ") is Invalid");
					return false; 
				}
				if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
					System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
					return false; 
				}
				
				for(TransactionInput input: currentTransaction.inputs) {	
					tempOutput = tempUTXOs.get(input.transactionOutputId);
					
					if(tempOutput == null) {
						System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
						return false;
					}
					
					if(input.UTXO.value != tempOutput.value) {
						System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
						return false;
					}
					
					tempUTXOs.remove(input.transactionOutputId);
				}
				
				for(TransactionOutput output: currentTransaction.outputs) {
					tempUTXOs.put(output.id, output);
				}
				
				if( currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
					System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
					return false;
				}
				if( currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
					System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
					return false;
				}
				
			}
			
		}
		System.out.println("Blockchain is valid");
		return true;
	}
	
	public static void addBlock(Block newBlock) {
		newBlock.mineBlock(difficulty);
		blockchain.add(newBlock);
	}
	
}

