package com.wakeappdriver.framework;

import com.wakeappdriver.framework.interfaces.Alerter;


public class AlerterContainer {
	private Alerter generalAlerter;
	private Alerter noIdenAlerter;
	private Alerter EmergencyAlerter;
	
	public AlerterContainer(Alerter general, Alerter noIden, Alerter emergency){
		this.generalAlerter = general;
		this.noIdenAlerter = noIden;
		this.EmergencyAlerter = emergency;
	}

	public Alerter getGeneralAlerter() {
		return generalAlerter;
	}

	public Alerter getNoIdenAlerter() {
		return noIdenAlerter;
	}

	public Alerter getEmergencyAlerter() {
		return EmergencyAlerter;
	}
	
}
