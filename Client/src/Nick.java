//Nick.java

import java.io.*;

/**This is the abstraction af a generic nickname.*/ 
public class Nick implements User, Serializable {

    private String name;
    private int age = 0;
    private boolean sex = true;    //true=m, false=f

    /**Create a Nick object identical to a given Nick object.*/
    public Nick(Object n) {
	if(n instanceof Nick) {
	    Nick temp = (Nick)n;
	    name = new String(temp.getName());
	    age = temp.getAge();
	    sex = temp.getSex();
	}
    }
    /**Create a Nick object with a _name, _age, _sex.*/
    public Nick(String _name, int _age, boolean _sex) { 
	name = new String(_name); 
	if(_age >= age) { age = _age; }
	sex = _sex;
    }
    /**Default constructor.*/
    public Nick() { this("Unknow", 15, true); }
    /**{@inheritDoc}.*/
    public boolean equals(Object o) { 
	if(o instanceof Nick) {
	    Nick n = (Nick)o;
	    return getName().equals(n.getName()); 
	} else return false;
    } 
    /**{@inheritDoc}.*/
    public String getName() { return name; }
    /**{@inheritDoc}.*/
    public int getAge() { return age; }
    /**{@inheritDoc}.*/
    public boolean getSex() { return sex; }
    /**{@inheritDoc}.*/
    public String toString() { return name; }

} //end class Nick
