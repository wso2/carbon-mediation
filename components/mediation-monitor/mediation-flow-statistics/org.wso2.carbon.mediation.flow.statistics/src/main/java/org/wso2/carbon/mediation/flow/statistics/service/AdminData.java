package org.wso2.carbon.mediation.flow.statistics.service;

/**
 * Created by virajrs on 11/23/15.
 */
public class AdminData {

	private String componetID;

	private String componetType;

	public AdminData(String componetID, String componetType) {
		this.componetID = componetID;
		this.componetType = componetType;
	}

	public String getComponetType() {
		return componetType;
	}

	public String getComponetID() {
		return componetID;
	}
}
