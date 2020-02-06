package org.evenos.windows;

import org.adempiere.webui.component.ToolBarButton;
import org.adempiere.webui.component.Window;
import org.zkoss.zhtml.Text;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Center;
import org.zkoss.zul.Image;
import org.zkoss.zul.Separator;
import org.zkoss.zul.Vbox;

public class BroadcasterAboutWindow extends Window{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5328647035196781092L;

	public BroadcasterAboutWindow(){
		super();
		this.setPosition("center");
		this.setTitle("About Broadcaster");
		this.setSclass("popup-dialog");
		this.setClosable(true);
		this.setMaximizable(false);
		
		this.setBorder("normal");
		this.setWidth("400px");
		this.setHeight("300px");
		this.setShadow(true);
		this.setAttribute(Window.MODE_KEY, Window.MODE_HIGHLIGHTED);
		
		Borderlayout borderlayout = new Borderlayout();
		this.appendChild(borderlayout);
		borderlayout.setHflex("1");
		borderlayout.setVflex("1");
		
		Center centerPane = new Center();
		centerPane.setSclass("dialog-content");
		centerPane.setAutoscroll(true);
		borderlayout.appendChild(centerPane);
		

		Vbox vb = new Vbox();
		vb.setWidth("100%");
		vb.setHeight("100%");
		vb.setAlign("center");
		vb.setPack("center");
		vb.setParent(centerPane);

		Vbox vbox = new Vbox();
		vbox.setWidth("100%");
		vbox.setAlign("center");
		vbox.setParent(vb);
		
		Image image = new Image("http://www.evenos-consulting.de/uploads/media/logo_03.png");
		image.setParent(vbox);

		vbox = new Vbox();
		vbox.setWidth("100%");
		vbox.setAlign("center");
		vbox.setParent(vb);
		
		Text text = new Text("Developed by Jan Thielemann");
		text.setParent(vbox);
		Separator separator = new Separator();
		separator.setParent(vbox);
		
		vbox = new Vbox();
		vbox.setWidth("100%");
		vbox.setAlign("center");
		vbox.setParent(vb);
		
		separator = new Separator();
		separator.setParent(vbox);
		ToolBarButton link = new ToolBarButton();
		link.setLabel("evenos Consulting GmbH");
		link.setHref("http://www.evenos-consulting.de");
		link.setTarget("_blank");
		link.setParent(vbox);
		
		separator = new Separator();
		separator.setParent(vbox);
		link = new ToolBarButton();
		link.setLabel("Send E-Mail");
		link.setHref("mailto:jan.thielemann@evenos.de");
		link.setTarget("_blank");
		link.setParent(vbox);

		separator = new Separator();
		separator.setParent(vbox);
		link = new ToolBarButton();
		link.setLabel("Wiki Site");
		link.setHref("http://wiki.idempiere.org/en/Plugin:_Broadcaster_Dashboard_Panel");
		link.setTarget("_blank");
		link.setParent(vbox);
	}
}
