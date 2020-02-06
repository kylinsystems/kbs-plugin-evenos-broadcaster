package org.adempiere.webui.dashboard;

import java.util.Map;

import org.adempiere.base.Service;
import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.component.Checkbox;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.Textbox;
import org.adempiere.webui.theme.ThemeManager;
import org.adempiere.webui.util.IServerPushCallback;
import org.adempiere.webui.util.ServerPushTemplate;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.evenos.trees.BroadcasterTreeModel;
import org.evenos.trees.BroadcasterTreeNode;
import org.evenos.windows.BroadcasterAboutWindow;
import org.evenos.windows.BroadcasterMessageWindow;
import org.idempiere.distributed.IMessageService;
import org.idempiere.distributed.ITopic;
import org.idempiere.distributed.ITopicSubscriber;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Image;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.Vlayout;

public class DPBroadcaster extends DashboardPanel implements
		EventListener<Event>, ITopicSubscriber<Map<String, Integer>> {// ,
																		// EventHandler{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3956606532708796545L;

	private CLogger log = CLogger.getCLogger(DPBroadcaster.class);

	private Vlayout layout = new Vlayout();
	private Div contentArea = new Div();
	private Tree tree;
	private BroadcasterTreeModel treeModel;
	private Div headerArea = new Div();
	private Checkbox checkboxIsOnline = new Checkbox();
	private Textbox filterUser = new Textbox();

	private Image about;

	private String oldFilterValue = "";

	private int ad_user_id = Env.getContextAsInt(Env.getCtx(), "#AD_User_ID");

	Desktop desktop = AEnv.getDesktop();

	private static final String ON_USER_LOGGED_IN_TOPIC = "onUserLoggedInTopic";
	private static final String ON_USER_LOGGED_OUT_TOPIC = "onUserLoggedOutTopic";

	private String DPBroadcaster_User_Name = Msg.getMsg(Env.getCtx(),
			"DPBroadcaster_User_Name");
	private String DPBroadcaster_Online_Users_Only = Msg.getMsg(Env.getCtx(),
			"DPBroadcaster_Online_Users_Only");
	private String DPBroadcaster_Online_Users_Only_Tooltip = Msg.getMsg(
			Env.getCtx(), "DPBroadcaster_Online_Users_Only_Tooltip");

	private ServerPushTemplate template;
	private BroadcasterServerPushCallback callback;

	class BroadcasterServerPushCallback implements IServerPushCallback {

		BroadcasterTreeModel model;

		public BroadcasterServerPushCallback(BroadcasterTreeModel model) {
			this.model = model;
		}

		@Override
		public void updateUI() {
			model.refresh();
		}

	}

	public DPBroadcaster() {
		super();

		this.setSclass("broadcaster-box");
		this.setHeight("220px");

		initLayout();
		initComponents();

		IMessageService service = Service.locator()
				.locate(IMessageService.class).getService();
		if (service != null) {
			ITopic<Map<String, Integer>> intopic = service
					.getTopic(ON_USER_LOGGED_IN_TOPIC);
			intopic.subscribe(this);

			ITopic<Map<String, Integer>> outtopic = service
					.getTopic(ON_USER_LOGGED_OUT_TOPIC);
			outtopic.subscribe(this);
		}

		this.template = new ServerPushTemplate(desktop);
		this.callback = new BroadcasterServerPushCallback(treeModel);

	}

	private void initLayout() {
		// The Layout holds all components of the dashboard panel
		layout.setParent(this);
		layout.setSclass("broadcaster-layout");
		layout.setSpacing("0px");
		layout.setStyle("height: 100%; width: 100%");

		// The header area holds a Vbox with the Dropdown for
		// Client/Org/Role/Location and the isOnline checkbox
		headerArea.setParent(layout);
		headerArea.setHflex("1");
		headerArea.setStyle("margin:5px 5px;");

		// The content area holds a Vbox with the users
		contentArea.setParent(layout);
		contentArea.setVflex("1");
		contentArea.setHflex("1");
		contentArea.setStyle("margin:5px 5px;overflow: auto;");
	}

	private void initComponents() {

		treeModel = BroadcasterTreeModel.create(ad_user_id, this, false);

		tree = new Tree();
		tree.setMultiple(false);
		tree.setWidth("100%");
		tree.setVflex(true);
		tree.setPageSize(-1); // Due to bug in the new paging functionality
		tree.setModel(treeModel);
		tree.setItemRenderer(treeModel);
		tree.setStyle("border: none");
		tree.addEventListener(Events.ON_CLICK, this);

		contentArea.appendChild(tree);

		checkboxIsOnline.addEventListener(Events.ON_CLICK, this);
		checkboxIsOnline
				.setTooltiptext(DPBroadcaster_Online_Users_Only_Tooltip);

		filterUser.addEventListener(Events.ON_BLUR, this);
		filterUser.addEventListener(Events.ON_OK, this);
		filterUser.setHflex("1");

		about = new Image(
				ThemeManager.getThemeResource("images/InfoIndicator16.png"));
		about.addEventListener(Events.ON_CLICK, this);
		about.setStyle("cursor: pointer;");

		// Add Combobox and Checkbox to header area
		Label labelUserFilter = new Label(DPBroadcaster_User_Name);
		Label labelIsOnline = new Label(DPBroadcaster_Online_Users_Only);

		Hbox box = new Hbox();
		box.setHflex("1");
		box.setStyle("margin:5px 5px;");
		box.appendChild(labelUserFilter);
		box.appendChild(filterUser);
		box.appendChild(labelIsOnline);
		box.appendChild(checkboxIsOnline);
		box.appendChild(about);
		headerArea.appendChild(box);

	}

	@Override
	public void onEvent(Event event) throws Exception {
		Component comp = event.getTarget();
		String eventName = event.getName();

		if (eventName.equals(Events.ON_CLICK)) {
			if (comp.equals(checkboxIsOnline)) {
				log.fine("Changed online Users only: "
						+ checkboxIsOnline.isSelected());

				treeModel.setOnlyOnlineUsers(checkboxIsOnline.isSelected());

			} else if (comp instanceof Treerow) {
				Treeitem selectedItem = (Treeitem) comp.getParent();
				Object attribue = selectedItem
						.getAttribute(BroadcasterTreeModel.ATTR_TREE_NODE);
				if (attribue instanceof BroadcasterTreeNode<?>) {
					@SuppressWarnings("unchecked")
					BroadcasterTreeNode<Object> node = (BroadcasterTreeNode<Object>) attribue;
					if (treeModel.isObjectOpened(node))
						treeModel.removeOpenObject(node);
					else
						treeModel.addOpenObject(node);
				}
			} else if (comp.equals(about)) {
				BroadcasterAboutWindow w = new BroadcasterAboutWindow();
				w.setPage(this.getPage());
				w.doHighlighted();
			} else {
				doOnClick(comp);
			}
		} else if (event.getName().equals(Events.ON_BLUR)
				|| event.getName().equals(Events.ON_OK)) {

			filterUser.setValue(filterUser.getValue().trim());
			if (!filterUser.getValue().equals(oldFilterValue)) {
				treeModel.setFilterUserName(filterUser.getValue());
				oldFilterValue = filterUser.getValue();
			}
		}

	}

	private void doOnClick(Component acomp) {
		Component comp = null;
		try {
			if (acomp.getParent().getParent().getParent() instanceof Treeitem)
				comp = acomp.getParent().getParent().getParent();

			if (comp != null) {
				BroadcasterMessageWindow w = new BroadcasterMessageWindow(
						this.ad_user_id,
						(Integer) comp
								.getAttribute(BroadcasterTreeModel.ATTR_AD_CLIENT_ID),
						(Integer) comp
								.getAttribute(BroadcasterTreeModel.ATTR_AD_ORG_ID),
						(Integer) comp
								.getAttribute(BroadcasterTreeModel.ATTR_AD_ROLE_ID),
						(Integer) comp
								.getAttribute(BroadcasterTreeModel.ATTR_AD_USER_ID),
						(Integer) comp
								.getAttribute(BroadcasterTreeModel.ATTR_C_COUNTRY_ID),
						(Integer) comp
								.getAttribute(BroadcasterTreeModel.ATTR_C_REGION_ID));
				w.setPage(this.getPage());
				w.doHighlighted();
			}
		} catch (Exception e) {
			// DO Nothing
		}
	}

	@Override
	public void onMessage(Map<String, Integer> message) {

		try {
			if (message.keySet().contains(ON_USER_LOGGED_IN_TOPIC)) {
				treeModel.userLoggedIn(message.get(ON_USER_LOGGED_IN_TOPIC));
			} else if (message.keySet().contains(ON_USER_LOGGED_OUT_TOPIC))
				treeModel.userLoggedOut(message.get(ON_USER_LOGGED_OUT_TOPIC));
		} catch (Exception e) {
			log.severe("Error during update of BroadcasterTreeModel: " + e);
		}

		template.executeAsync(callback);

	}

	@Override
	public void detach() {

		IMessageService service = Service.locator()
				.locate(IMessageService.class).getService();
		if (service != null) {
			ITopic<Map<String, Integer>> intopic = service
					.getTopic(ON_USER_LOGGED_IN_TOPIC);
			intopic.unsubscribe(this);

			ITopic<Map<String, Integer>> outtopic = service
					.getTopic(ON_USER_LOGGED_OUT_TOPIC);
			outtopic.unsubscribe(this);
		}
		super.detach();

	}

}
