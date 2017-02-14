package de.tu_darmstadt.sse.sharedclasses.dynamiccfg;

import android.os.Parcel;
import android.os.Parcelable;



public class MethodReturnItem extends AbstractDynamicCFGItem {
	
	
	private static final long serialVersionUID = -6458334957088315646L;
	
	public static final Parcelable.Creator<MethodReturnItem> CREATOR = new Parcelable.Creator<MethodReturnItem>() {

		@Override
		public MethodReturnItem createFromParcel(Parcel parcel) {
			MethodReturnItem mci = new MethodReturnItem();
			mci.readFromParcel(parcel);
			return mci;
		}

		@Override
		public MethodReturnItem[] newArray(int size) {
			return new MethodReturnItem[size];
		}

	};
	
	
	public MethodReturnItem() {
		super();
	}
	
	
	public MethodReturnItem(int lastExecutedStatement) {
		super(lastExecutedStatement);
	}
	
	@Override
	public String toString() {
		return "Method return: " + getLastExecutedStatement();
	}
	
}
