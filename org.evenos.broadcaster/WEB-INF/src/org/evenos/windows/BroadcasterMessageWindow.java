package org.evenos.windows;

import org.adempiere.model.MBroadcastMessage;
import org.adempiere.webui.LayoutUtils;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.ConfirmPanel;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.Textbox;
import org.adempiere.webui.component.Window;
import org.adempiere.webui.factory.ButtonFactory;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.evenos.util.UserPOJO;
import org.idempiere.broadcast.BroadcastMsgUtil;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Center;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.South;


public class BroadcasterMessageWindow extends Window implements EventListener<Event>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6698486310136569751L;
	
	private Textbox text;
	private Checkbox onlineUsers;
	private Integer ad_client_id;
	private Integer ad_org_id;
	private Integer ad_role_id;
	private Integer ad_user_id;
	private Integer c_country_id;
	private Integer c_region_id;
	private Integer currentUser;
	
	Button btnCancel;
	Button btnOk;
	
	private CLogger log = CLogger.getCLogger(BroadcasterMessageWindow.class);
	
	private String DPBroadcaster_Online_Users_Only = Msg.getMsg(Env.getCtx(), "DPBroadcaster_Online_Users_Only");
	private String DPBroadcaster_Message_To = Msg.getMsg(Env.getCtx(), "DPBroadcaster_Message_To");
	
	public BroadcasterMessageWindow(Integer currentUser, Integer ad_client_id, Integer ad_org_id, Integer ad_role_id, Integer ad_user_id, Integer c_country_id, Integer c_region_id) {
		super();
		this.currentUser = currentUser;
		this.ad_client_id = ad_client_id;
		this.ad_org_id = ad_org_id;
		this.ad_role_id = ad_role_id;
		this.ad_user_id = ad_user_id;
		this.c_country_id = c_country_id;
		this.c_region_id = c_region_id;
		init();		
	}

	private void init() {
	
		String title = "";
		if(ad_client_id!=null) title = DPBroadcaster_Message_To + " " + DB.getSQLValueString(null, "SELECT NAME FROM AD_CLIENT WHERE AD_CLIENT_ID = " + ad_client_id);
		if(ad_org_id!=null) title = DPBroadcaster_Message_To + " " + DB.getSQLValueString(null, "SELECT NAME FROM AD_ORG WHERE AD_ORG_ID = " + ad_org_id);
		if(ad_role_id!=null) title = DPBroadcaster_Message_To + " " + DB.getSQLValueString(null, "SELECT NAME FROM AD_ROLE WHERE AD_ROLE_ID = " + ad_role_id);
		if(ad_user_id!=null) title = DPBroadcaster_Message_To + " " + DB.getSQLValueString(null, "SELECT NAME FROM AD_USER WHERE AD_USER_ID = " + ad_user_id);
		if(c_country_id!=null) title = DPBroadcaster_Message_To + " " + DB.getSQLValueString(null, "SELECT NAME FROM C_Country WHERE C_COUNTRY_ID = " + c_country_id);
		if(c_region_id!=null) title = DPBroadcaster_Message_To + " " + DB.getSQLValueString(null, "SELECT NAME FROM C_Region WHERE C_REGION_ID = " + c_region_id);
		
		this.setPosition("center");
		this.setTitle(title);
		this.setSclass("popup-dialog");
		this.setClosable(true);
		this.setMaximizable(false);
		this.setSizable(true);
		
		
		
		btnCancel = ButtonFactory.createNamedButton(ConfirmPanel.A_CANCEL); 
		btnCancel.addEventListener(Events.ON_CLICK, this);
		
		btnOk = ButtonFactory.createNamedButton(ConfirmPanel.A_OK); 
		btnOk.addEventListener(Events.ON_CLICK, this);
		
		Borderlayout borderlayout = new Borderlayout();
		this.appendChild(borderlayout);
		borderlayout.setHflex("1");
		borderlayout.setVflex("1");
		
		Center centerPane = new Center();
		centerPane.setSclass("dialog-content");
		centerPane.setAutoscroll(true);
		borderlayout.appendChild(centerPane);
		
		Div vbox = new Div();
		vbox.setHeight("100%");
		LayoutUtils.addSclass("broadcast-main-div", vbox);
		vbox.setParent(centerPane);
		
		Div div = new Div();
		LayoutUtils.addSclass("broadcast-text-panel", div);
		div.setParent(vbox);
		div.setHeight("90%");
		div.setStyle("overflow: auto;");
		text = new Textbox("");
		text.setVflex("1");
		text.setHflex("1");
		text.setStyle("margin:5px 5px;overflow: auto; resize: none;");
		text.setRows(10);
		text.setMaxlength(2000);
		text.setParent(div);
		
		Hbox box = new Hbox();
		Label lAllUsers = new Label(DPBroadcaster_Online_Users_Only);
		onlineUsers = new Checkbox();
		lAllUsers.setParent(box);
		onlineUsers.setParent(box);
		box.setParent(vbox);
		
		South southPane = new South();
		southPane.setStyle("text-align: right");
		southPane.setSclass("dialog-footer");
		borderlayout.appendChild(southPane);
		Hbox hbox = new Hbox();
		hbox.setStyle("float: right");
		hbox.appendChild(btnOk);
		hbox.appendChild(btnCancel);
		southPane.appendChild(hbox);
		

		this.setBorder("normal");
		this.setWidth("600px");
		this.setHeight("450px");
		this.setShadow(true);
		this.setAttribute(Window.MODE_KEY, Window.MODE_HIGHLIGHTED);
	}

	@Override
	public void onEvent(Event event) throws Exception {
		//If OK Button was clicked, close the window and run the callback.
		if (Events.ON_CLICK.equals(event.getName())){

			if(event.getTarget().equals(btnOk)){
				if(ad_client_id != null) 
					broadcastClient();
				
				if(ad_org_id != null) 
					broadcastOrg();
				
				if(ad_role_id != null) 
					broadcastRole();
				
				if(ad_user_id != null) 
					broadcastUser(ad_user_id.intValue());
				
				if(c_country_id != null)
					broadcastCountry();
				
				if(c_region_id != null)
					broadcastRegion();
			}
			this.detach();
		}
	}

	private void broadcastRole() {
		for(Integer user_id : UserPOJO.getRoleUserIDs(currentUser, ad_role_id)){
			broadcastUser(user_id);
		}
	}

	private void broadcastOrg() {
		for(Integer user_id : UserPOJO.getOrgUserIDs(currentUser, ad_org_id)){
			broadcastUser(user_id);
		}
	}

	private void broadcastClient() {
		for(Integer user_id : UserPOJO.getClientUserIDs(currentUser, ad_client_id)){
			broadcastUser(user_id);
		}
	}

	private void broadcastRegion() {
		for(Integer user_id : UserPOJO.getRegionUserIDs(currentUser, c_region_id)){
			broadcastUser(user_id);
		}		
	}

	private void broadcastCountry() {
		for(Integer user_id : UserPOJO.getCountryUserIDs(currentUser, c_country_id)){
			broadcastUser(user_id);
		}	
	}

	private void broadcastUser(int ad_user_id) {

		if(ad_user_id == this.currentUser.intValue())
			return;
		
		if(onlineUsers.isChecked()){
			int val = DB.getSQLValue(null, "select u.ad_user_id from ad_user u join ad_session s on s.createdby = u.ad_user_id where s.processed = 'N' and u.ad_user_id = "+ad_user_id);
			if(val <= 0)
				return;
		}
		log.info("Broadcast to user: " + ad_user_id);
		
		MBroadcastMessage msg = new MBroadcastMessage(Env.getCtx(), 0, null);
		msg.setBroadcastMessage(text.getText());
		msg.setBroadcastType(onlineUsers.isChecked() ? MBroadcastMessage.BROADCASTTYPE_Immediate : MBroadcastMessage.BROADCASTTYPE_ImmediatePlusLogin);
		msg.setTarget(MBroadcastMessage.TARGET_User);
		msg.setAD_User_ID(ad_user_id);
		msg.setBroadcastFrequency(MBroadcastMessage.BROADCASTFREQUENCY_UntilAcknowledge);
		msg.setIsActive(true);
		msg.save();
		BroadcastMsgUtil.publishBroadcastMessage(msg.getAD_BroadcastMessage_ID(), null);		
	}
	
}
