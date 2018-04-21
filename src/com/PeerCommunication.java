package com;

import com.FileProcessor.FileManagerExecutor;
import com.logger.EventLogger;
import com.messages.Handshake;
import com.messages.Message;
import com.messages.MessageUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.BitSet;
import java.util.Map;

public class PeerCommunication {
    RemotePeerInfo remote;
    Socket socket;
    Handshake handshake;
    BufferedOutputStream out;
    BufferedInputStream in;
    int recentReceievdPiece;

    
	EventLogger log = new EventLogger(Peer.getPeerInstance().get_peerID());

   public PeerCommunication (RemotePeerInfo remotePeerInfo) {
        this.remote = remotePeerInfo;
        initSocket();
    }

    private void initSocket() {
        try{
            this.socket = new Socket (this.remote.get_hostName(), this.remote.get_portNo());
            this.out = new BufferedOutputStream(this.socket.getOutputStream());
            this.in = new BufferedInputStream(this.socket.getInputStream());
            this.handshake = new Handshake(this.remote.get_peerID());

            this.handshake.sendHandshakeMsg(this.out);
            if(this.handshake.recieveHandshake(this.in)){
            	System.out.println("Recieved Handshake");
            }
            else{
            	//TODO logger
            }

        } catch (IOException e) {
            throw new RuntimeException("Could not open client socket", e);
        }
    }
    
    public void startMessageExchange () throws Exception {
        Message message = null;
        byte[] pieceIndexField = null;
    	if(!Peer.getPeerInstance().getBitSet().isEmpty()){
    		message = PeerCommunicationHelper.sendBitSetMsg(this.out);
    	}
    	while(!peerProcess.isCompleted()){
    		byte msgType = PeerCommunicationHelper.getMessageType(this.in);
    		byte[] msgPayloadReceived = PeerCommunicationHelper.getActualMessage(this.in);
    		if(msgType == (byte)7){
        		pieceIndexField = new byte[4];
        		for(int i=0;i<4;i++){
        			pieceIndexField[i] = msgPayloadReceived[i];
        		}
    		}
    		
    		switch(msgType){
    		//choke(If choked map maintained put in it or else nothing to do. 
    		case (byte)0:{
    				break;
    		}
    		//bitset(In this send interesetd or Not Interseted)
    		case (byte)5:{
    	    	BitSet bitset = MessageUtil.fromByteArraytoBitSet(msgPayloadReceived);
    			if(PeerCommunicationHelper.isInterseted(bitset,Peer.getPeerInstance().getBitSet())){
    	    		message = PeerCommunicationHelper.sendInterestedMsg(this.out);
    	    	}
    	    	else{
    	    		message = PeerCommunicationHelper.sendNotInterestedMsg(this.out);
    	    	}
    			break;
    		}
    		
    		//Interseted(put in interseted map and compare bitset and send have msg)
    		case (byte)2:{
        		Peer.getPeerInstance().peersInterested.putIfAbsent(this.remote.get_peerID(), this.remote);
        		int haveIndexField = PeerCommunicationHelper.compare(Peer.getPeerInstance().getBitSet(), this.remote.getBitfield());
        		PeerCommunicationHelper.sendHaveMsg(this.out, haveIndexField);
    			break;
    		}
    		
    		//Not Interested (remove from the interseted map)
    		case (byte)3:{
    			if(Peer.getPeerInstance().peersInterested.containsKey(this.remote.get_peerID()))
    	    		Peer.getPeerInstance().peersInterested.remove(this.remote.get_peerID());
    			break;
    		}
    		
    		//Have (
    		case (byte)4:{
    			//check is prefreferref neighbours and optimistically unchoked
    			//request
    			
    			break;
    			
    		}
    		
    		//Unchoke(send request msg or else Not interested)
    		case (byte)1:{
    			int pieceIndex = MessageUtil.byteArrayToInt(PeerCommunicationHelper.getPieceIndex(this.remote));
    			if(pieceIndex!=-1){
    				message = PeerCommunicationHelper.sendRequestMsg(out,this.remote);
    			}
    			if(pieceIndex == 1){
    				message = PeerCommunicationHelper.sendNotInterestedMsg(this.out);    			
    				}
    			  break;			
    		}
    		
    		//Request (send piece msg if preferred neighbour or optimistically unchoked
    		case (byte)6:{
    			//TODO write condition
    		    if(Peer.getPeerInstance().)
    			PeerCommunicationHelper.sendPieceMsg(this.out, MessageUtil.byteArrayToInt(msgPayloadReceived));
    			break;
    		}
    		//download the arrived piece.
            //update the bitset.
            //send have message to the preferred neighbour list
    		case (byte)7:{
    			//Try to do a check
    			FileManagerExecutor.acceptFilePart(MessageUtil.byteArrayToInt(pieceIndexField),this.in);
    			Peer.getPeerInstance().getBitSet().set(MessageUtil.byteArrayToInt(pieceIndexField));
    			PeerCommunicationHelper.sendRequestMsg(this.out, this.remote);
    			//compare bitsets again and send request
    			break;
    		}
    	}//switch end
    	}//while end
    }
}
