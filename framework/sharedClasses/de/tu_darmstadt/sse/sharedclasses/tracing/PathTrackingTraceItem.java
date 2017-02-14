package de.tu_darmstadt.sse.sharedclasses.tracing;

import android.os.Parcel;
import android.os.Parcelable;


public class PathTrackingTraceItem extends TraceItem {

	
	private static final long serialVersionUID = -8948293905139569335L;

	public static final Parcelable.Creator<PathTrackingTraceItem> CREATOR = new Parcelable.Creator<PathTrackingTraceItem>() {

		@Override
		public PathTrackingTraceItem createFromParcel(Parcel parcel) {
			PathTrackingTraceItem ti = new PathTrackingTraceItem();
			ti.readFromParcel(parcel);
			return ti;
		}

		@Override
		public PathTrackingTraceItem[] newArray(int size) {
			return new PathTrackingTraceItem[size];
		}

	};

	private boolean lastConditionResult;

	
	private PathTrackingTraceItem() {
		super();
	}

	
	public PathTrackingTraceItem(int lastExecutedStatement,
			boolean lastConditionResult) {
		super(lastExecutedStatement);
		this.lastConditionResult = lastConditionResult;
	}

	
	public boolean getLastConditionalResult() {
		return this.lastConditionResult;
	}

	@Override
	public void writeToParcel(Parcel parcel, int arg1) {
		super.writeToParcel(parcel, arg1);
		parcel.writeByte((byte) (lastConditionResult ? 0 : 1));
	}
	
	@Override
	protected void readFromParcel(Parcel parcel) {
		super.readFromParcel(parcel);
		this.lastConditionResult = parcel.readByte() == 1;
	}

}
