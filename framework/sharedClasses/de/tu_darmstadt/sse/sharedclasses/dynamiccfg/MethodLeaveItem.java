package de.tu_darmstadt.sse.sharedclasses.dynamiccfg;

import android.os.Parcel;
import android.os.Parcelable;



public class MethodLeaveItem extends AbstractDynamicCFGItem {
	
	
	private static final long serialVersionUID = -8382002494703671501L;
	
	public static final Parcelable.Creator<MethodLeaveItem> CREATOR = new Parcelable.Creator<MethodLeaveItem>() {

		@Override
		public MethodLeaveItem createFromParcel(Parcel parcel) {
			MethodLeaveItem mci = new MethodLeaveItem();
			mci.readFromParcel(parcel);
			return mci;
		}

		@Override
		public MethodLeaveItem[] newArray(int size) {
			return new MethodLeaveItem[size];
		}

	};
	
	
	public MethodLeaveItem() {
		super();
	}
	
	
	public MethodLeaveItem(int lastExecutedStatement) {
		super(lastExecutedStatement);
	}
	
	@Override
	public String toString() {
		return "Method leave: " + getLastExecutedStatement();
	}

}
