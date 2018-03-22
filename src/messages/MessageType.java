package messages;

public enum MessageType {
    CHOKE((byte)0),
    UNCHOKE((byte)1),
    INTERESTED((byte)2),
    NOT_INTERESTED((byte)3),
    HAVE((byte)4),
    BITFIELD((byte)5),
    REQUEST((byte)6),
    PIECE((byte)7);

    byte value;

    public byte getValue() {
        return value;
    }

    public void setValue(byte value) {
        this.value = value;
    }

    private MessageType(byte n){
        this.value = n;
    }
}

