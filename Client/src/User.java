//User.java


/**Abstraction of an user.*/ 
public interface User {

    /** @return true if Nickname and the object are the same.*/
    boolean equals(Object o);
    /**Return the user name.*/
    String getName();
    /**Return the user age.*/
    int getAge();
    /**Ritorna the user sex.
     * @return true se l'user is male, false otherwise.*/
    boolean getSex();
    /**Return a string for an object User.*/
    String toString();

} //end interface User
