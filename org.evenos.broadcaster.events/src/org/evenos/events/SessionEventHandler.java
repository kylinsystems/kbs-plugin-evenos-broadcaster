package org.evenos.events;

import java.util.HashMap;
import java.util.Map;

import org.adempiere.base.Service;
import org.adempiere.base.event.AbstractEventHandler;
import org.adempiere.base.event.IEventTopics;
import org.compiere.model.MSession;
import org.compiere.model.PO;
import org.idempiere.distributed.IMessageService;
import org.idempiere.distributed.ITopic;
import org.osgi.service.event.Event;

public class SessionEventHandler extends AbstractEventHandler {

	private static final String ON_USER_LOGGED_IN_TOPIC = "onUserLoggedInTopic";
	private static final String ON_USER_LOGGED_OUT_TOPIC = "onUserLoggedOutTopic";
	
	@Override
	protected void doHandleEvent(Event event) {

		if (event.getTopic().equals(IEventTopics.PO_AFTER_NEW)) {
			PO po = getPO(event);
			if (po instanceof MSession) {
				MSession session = (MSession)po;
				publishEvent(session.getCreatedBy(), ON_USER_LOGGED_IN_TOPIC);
			}
		} else if (event.getTopic().equals(IEventTopics.PO_AFTER_CHANGE)) {
			PO po = getPO(event);
			if (po instanceof MSession && ((MSession) po).isProcessed()) {
				MSession session = (MSession)po;
				publishEvent(session.getCreatedBy(), ON_USER_LOGGED_OUT_TOPIC);
			}
		}
	}

	private void publishEvent(int createdBy, String topic) {
		Map<String, Integer> properties = new HashMap<String, Integer>();
		properties.put(topic, new Integer(createdBy));
		
		IMessageService service = Service.locator().locate(IMessageService.class).getService();
		if (service != null) {
			ITopic<Map<String,Integer>> itopic = service.getTopic(topic);
			itopic.publish(properties);
		}	
	}

	@Override
	protected void initialize() {
		registerTableEvent(IEventTopics.PO_AFTER_NEW, MSession.Table_Name);
		registerTableEvent(IEventTopics.PO_AFTER_CHANGE, MSession.Table_Name);
	}

}
