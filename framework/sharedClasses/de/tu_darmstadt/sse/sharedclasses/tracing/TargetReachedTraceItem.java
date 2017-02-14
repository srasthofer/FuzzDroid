package de.tu_darmstadt.sse.sharedclasses.tracing;

import android.os.Parcel;
import android.os.Parcelable;


public class TargetReachedTraceItem extends TraceItem {
	
	
	private static final long serialVersionUID = 2184482709277468438L;
	
	public static final Parcelable.Creator<TargetReachedTraceItem> CREATOR = new Parcelable.Creator<TargetReachedTraceItem>() {

		@Override
		public TargetReachedTraceItem createFromParcel(Parcel parcel) {
			TargetReachedTraceItem ti = new TargetReachedTraceItem();
			ti.readFromParcel(parcel);
			return ti;
		}

		@Override
		public TargetReachedTraceItem[] newArray(int size) {
			return new TargetReachedTraceItem[size];
		}

	};
	
	
	private TargetReachedTraceItem() {
		super();
	}
	
	
	public TargetReachedTraceItem(int lastExecutedStatement) {
		super(lastExecutedStatement);
	}

}
