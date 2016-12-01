//SyncString.java

/**Abstraction of a synchronized string.*/
public class SyncString {

    private String shared;
    /**put s as shared string.*/
    public synchronized  void put(String s) { shared = s; }
    /**Get the shared string.*/
    public synchronized String get() { return shared; }

}  //end class SyncString
