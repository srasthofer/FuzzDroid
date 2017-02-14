package de.tu_darmstadt.sse.sharedclasses.dynamiccfg;

import android.os.Parcel;
import android.os.Parcelable;



public class MethodCallItem extends AbstractDynamicCFGItem {
	
	
	private static final long serialVersionUID = -8382002494703671501L;
	
	public static final Parcelable.Creator<MethodCallItem> CREATOR = new Parcelable.Creator<MethodCallItem>() {

		@Override
		public MethodCallItem createFromParcel(Parcel parcel) {
			MethodCallItem mci = new MethodCallItem();
			mci.readFromParcel(parcel);
			return mci;
		}

		@Override
		public MethodCallItem[] newArray(int size) {
			return new MethodCallItem[size];
		}

	};
	
	
	public MethodCallItem() {
		super();
	}
	
	
	public MethodCallItem(int lastExecutedStatement) {
		super(lastExecutedStatement);
	}
	
	@Override
	public String toString() {
		return "Method call: " + getLastExecutedStatement();
	}
	
}
