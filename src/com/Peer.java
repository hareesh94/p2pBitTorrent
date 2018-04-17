package com;

import java.io.File;
import java.util.BitSet;
import java.util.Map;

public class Peer {
    public Map<Integer, RemotePeerInfo> getPeersToConnectTo() {
        return peersToConnectTo;
    }

    public Map<Integer, RemotePeerInfo> getPeersToExpectConnectionsFrom() {
        return peersToExpectConnectionsFrom;
    }

    Map<Integer, RemotePeerInfo> peersToConnectTo;
    Map<Integer, RemotePeerInfo> peersToExpectConnectionsFrom;

    private int _peerID;
    private String _hostName;
    private int _port;
    private int _hasFile;
    private BitSet _bitField;

    private int _excessPieceSize;
    private int _pieceCount;

    public static Peer getPeerInstance() {
        if (peer == null) {
            synchronized (Peer.class) {
                if (peer == null) peer = new Peer();
            }
        }
        return peer;
    }

    int get_peerID() {
        return _peerID;
    }

    void set_peerID(int _peerID) {
        this._peerID = _peerID;
    }

    String get_hostName() {
        return _hostName;
    }

    public int get_port() {
        return _port;
    }

    void set_hostName(String _hostName) {
        this._hostName = _hostName;
    }

    void set_port(int _port) {
        this._port = _port;
    }

    int get_hasFile() {
        return _hasFile;
    }

    public boolean get_bitField(int i) {
        return _bitField.get(i);
    }

    void set_hasFile(int _hasFile) {
        this._hasFile = _hasFile;
    }

    public int get_pieceCount() {
        return _pieceCount;
    }

    void set_bitField(int i) {
        this._bitField.set(i);
    }

    void set_pieceCount(int _pieceCount) {
        this._pieceCount = _pieceCount;
    }

    int get_excessPieceSize() {
        return _excessPieceSize;
    }

    void set_excessPieceSize(int _excessPieceSize) {
        this._excessPieceSize = _excessPieceSize;
    }

    int setBitset(int n) {
        int i = 0;
        for (; i < n; i++) {
            peer.set_bitField(i);
        }

        return i;
    }

    private void createDirectory(int _peerID) {
        File dir = new File(Constants.DEST_FILE + "/peer_" + _peerID);
        boolean success = false;
        try {
            success = dir.mkdir();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (success) {
            File file = new File(Constants.DEST_FILE + "/peer_" + _peerID + "/file.dat");
        } else {
            //Log failure to create corresponding directory
        }
    }

    private static volatile Peer peer;

    private Peer() {
        _bitField = new BitSet();
    }

    void setPieceSize() {
        int n = 0;
        int f = Constants.getFileSize();
        int p = Constants.getPieceSize();

        if (f % p == 0) {
            n = f / p;
        } else {
            int temp = (f - p * (f / p));
            peer.set_excessPieceSize(temp);
            System.out.println(peer.get_excessPieceSize());
            n = f / p;
            ++n;
        }

        peer.set_pieceCount(n);

        int temp = setBitset(n);
        n = peer.get_excessPieceSize();
        setBitset(temp + n);
    }
}