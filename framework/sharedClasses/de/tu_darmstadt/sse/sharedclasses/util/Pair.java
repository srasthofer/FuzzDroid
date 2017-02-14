package de.tu_darmstadt.sse.sharedclasses.util;
import java.io.Serializable;

public class Pair<F, S> implements Serializable, Cloneable {
	private static final long serialVersionUID = 7408444626787884925L;
	
	private F first; 
    private S second;
    
	protected int hashCode = 0;

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public void setFirst(F first) {
        this.first = first;
		hashCode = 0;
    }

    public void setSecond(S second) {
        this.second = second;
		hashCode = 0;
    }
    
	public void setPair(F no1, S no2) {
		first = no1;
		second = no2;
		hashCode = 0;
	}

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }
    
    @Override
	public int hashCode() {
		if (hashCode != 0)
			return hashCode;
		
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		hashCode = result;
		
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		Pair other = (Pair) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		return true;
	}

	public String toString() {
		return "Pair " + first + "," + second;
	}
	
}
