package org.ssssssss.magicapi.model;

import java.util.ArrayList;
import java.util.List;

public class SynchronizeResponse {

	private List<SynchronizeRequest.Info> added = new ArrayList<>();

	private List<SynchronizeRequest.Info> removed = new ArrayList<>();

	private List<SynchronizeRequest.Info> updated = new ArrayList<>();

	public void addRemoved(SynchronizeRequest.Info info) {
		this.removed.add(info);
	}

	public void addUpdated(SynchronizeRequest.Info info) {
		this.updated.add(info);
	}

	public void addAdded(SynchronizeRequest.Info info) {
		this.added.add(info);
	}

	public List<SynchronizeRequest.Info> getAdded() {
		return added;
	}

	public void setAdded(List<SynchronizeRequest.Info> added) {
		this.added = added;
	}

	public List<SynchronizeRequest.Info> getRemoved() {
		return removed;
	}

	public void setRemoved(List<SynchronizeRequest.Info> removed) {
		this.removed = removed;
	}

	public List<SynchronizeRequest.Info> getUpdated() {
		return updated;
	}

	public void setUpdated(List<SynchronizeRequest.Info> updated) {
		this.updated = updated;
	}
}
