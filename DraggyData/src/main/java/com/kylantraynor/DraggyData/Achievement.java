package com.kylantraynor.DraggyData;

import java.util.List;

public interface Achievement {
	public abstract String getId();
	
	public abstract String getName();
	
	public abstract Achievement getRequirement();
	
	public abstract List<String> getDescription();
}
