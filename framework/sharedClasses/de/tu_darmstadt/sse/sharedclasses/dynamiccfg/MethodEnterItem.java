package de.tu_darmstadt.sse.sharedclasses.dynamiccfg;

import android.os.Parcel;
import android.os.Parcelable;



public class MethodEnterItem extends AbstractDynamicCFGItem {
	
	
	private static final long serialVersionUID = -8382002494703671501L;
	
	public static final Parcelable.Creator<MethodEnterItem> CREATOR = new Parcelable.Creator<MethodEnterItem>() {

		@Override
		public MethodEnterItem createFromParcel(Parcel parcel) {
			MethodEnterItem mci = new MethodEnterItem();
			mci.readFromParcel(parcel);
			return mci;
		}

		@Override
		public MethodEnterItem[] newArray(int size) {
			return new MethodEnterItem[size];
		}

	};
	
	
	public MethodEnterItem() {
		super();
	}
	
	
	public MethodEnterItem(int lastExecutedStatement) {
		super(lastExecutedStatement);
	}
	
	@Override
	public String toString() {
		return "Method enter: " + getLastExecutedStatement();
	}
	
}
