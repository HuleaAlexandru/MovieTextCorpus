package MovieUnits;

import java.util.ArrayList;
import java.util.List;

public class Tuple {
	private List<String> values;
	
	public Tuple() {
		this.values = new ArrayList<String>();
	}
	
	public Tuple(String first, String second) {
		this.values = new ArrayList<String>();
		this.values.add(first);
		this.values.add(second);
	}
	
	public boolean addElement(String element) {
		return this.values.add(element);
	}
	
	public String getFirst() {
		return this.values.get(0);
	}
	
	public String getSecond() {
		return this.values.get(1);
	}
	
	public String getThird() {
		return this.values.get(2);
	}
	
	public List<String> getValues() {
		return this.values;
	}
	
	@Override
	public boolean equals(Object obj) {
		Tuple o = (Tuple)obj;
		if (this.values.size() != o.getValues().size()) {
			return false;
		}
		for (int i = 0; i < this.values.size(); i++) {
			if (!this.values.get(i).equals(o.getValues().get(i))) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		int result = 0;
		for (int i = 0; i < this.values.size(); i++) {
			result += this.values.get(i).hashCode();
		}
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < this.values.size(); i++) {
			result.append(this.values.get(i));
			result.append(" ");
		}
		return result.toString();
	}
}
