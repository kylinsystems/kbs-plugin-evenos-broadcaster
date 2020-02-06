package org.evenos.trees;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.adempiere.webui.theme.ThemeManager;
import org.compiere.model.MCountry;
import org.compiere.model.MRegion;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.compiere.util.Util;
import org.evenos.util.UserPOJO;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.A;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.ext.TreeSelectableModel;

public class BroadcasterTreeModel extends DefaultTreeModel<Object> implements TreeSelectableModel,
		TreeitemRenderer<BroadcasterTreeNode<Object>> {

	private static final long serialVersionUID = 7822729366554623684L;

	private static final CLogger log = CLogger.getCLogger(BroadcasterTreeModel.class);

	private int ad_user_id;

	private EventListener<Event> eventListener;

	private Map<Integer, List<UserPOJO>> users;

	public Map<Integer, List<UserPOJO>> getUsers() {
		return users;
	}

	List<KeyNamePair> ad_clients;
	List<KeyNamePair> ad_orgs;
	List<KeyNamePair> ad_roles;

	private boolean onlyOnlineUsers;
	private String filterName = "";

	public static final String ATTR_AD_CLIENT_ID = "ATTR_AD_CLIENT_ID";
	public static final String ATTR_AD_ORG_ID = "ATTR_AD_ORG_ID";
	public static final String ATTR_AD_ROLE_ID = "ATTR_AD_ROLE_ID";
	public static final String ATTR_AD_USER_ID = "ATTR_AD_USER_ID";
	public static final String ATTR_C_COUNTRY_ID = "ATTR_C_COUNTRY_ID";
	public static final String ATTR_C_REGION_ID = "ATTR_C_REGION_ID";
	public static final String ATTR_TREE_NODE = "ATTR_TREE_NODE";

	private String DPBroadcaster_Clients = Msg.getMsg(Env.getCtx(), "DPBroadcaster_Clients");
	private String DPBroadcaster_Organizations = Msg.getMsg(Env.getCtx(), "DPBroadcaster_Organizations");
	private String DPBroadcaster_Roles = Msg.getMsg(Env.getCtx(), "DPBroadcaster_Roles");
	private String DPBroadcaster_Countries = Msg.getMsg(Env.getCtx(), "DPBroadcaster_Countries");
	private String DPBroadcaster_Tooltip_Message_To = Msg.getMsg(Env.getCtx(), "DPBroadcaster_Tooltip_Message_To");

	public BroadcasterTreeModel(BroadcasterTreeNode<Object> root, int ad_user_id, EventListener<Event> eventListener,
			boolean onlyOnlineUsers) {
		super(root);
		log.fine("BroadcasterTreeModel erstellt");
		this.eventListener = eventListener;
		this.ad_user_id = ad_user_id;
		this.onlyOnlineUsers = onlyOnlineUsers;
		this.filterName = "";

		this.users = UserPOJO.findVisibleUsers(ad_user_id);
		this.ad_clients = UserPOJO.getClients(this.ad_user_id);
		this.ad_roles = UserPOJO.getRoles(this.ad_user_id);
		this.ad_orgs = UserPOJO.getOrgs(this.ad_user_id, ad_clients, ad_roles);

		refresh();
	}

	public void refresh() {
		log.fine("Refreshing BroadcasterTreeModel");
		int[][] tmpPaths = this.getOpenPaths();
		removeNodesRecursivly(this.getRoot());
		getClients();
		getOrgs();
		getRoles();
		getLocations();
		this.addOpenPaths(tmpPaths);
	}

	private void removeNodesRecursivly(TreeNode<Object> root) {
		for (int i = root.getChildCount() - 1; i >= 0; i--) {

			if (root.getChildAt(i).getChildCount() > 0)
				removeNodesRecursivly(root.getChildAt(i));

			root.remove(i);
		}
	}

	private void getLocations() {
		BroadcasterTreeNode<Object> countries = new BroadcasterTreeNode<Object>(DPBroadcaster_Countries, new ArrayList<TreeNode<Object>>());

		Map<Integer, Map<Integer, Map<Integer, List<UserPOJO>>>> country_region_user_map = new TreeMap<Integer, Map<Integer, Map<Integer, List<UserPOJO>>>>();
		for (List<UserPOJO> users_list : users.values()) {
			for (UserPOJO pojo : users_list) {

				if (this.onlyOnlineUsers && !pojo.isOnline || pojo.C_Country_ID == 0 || pojo.C_Region_ID == 0)
					continue;

				if (!Util.isEmpty(this.filterName)
						&& !pojo.Username.toUpperCase().contains(this.filterName.toUpperCase()))
					continue;

				Map<Integer, Map<Integer, List<UserPOJO>>> region_user_map = country_region_user_map.get(new Integer(
						pojo.C_Country_ID));
				if (region_user_map == null)
					region_user_map = new TreeMap<Integer, Map<Integer, List<UserPOJO>>>();

				Map<Integer, List<UserPOJO>> user_map = region_user_map.get(new Integer(pojo.C_Region_ID));
				if (user_map == null) {
					user_map = new TreeMap<Integer, List<UserPOJO>>();
				}

				List<UserPOJO> user_list = user_map.get(new Integer(pojo.AD_User_ID));
				if (user_list == null) {
					user_list = new ArrayList<UserPOJO>();
				}
				if (!user_list.contains(pojo))
					user_list.add(pojo);

				user_map.put(new Integer(pojo.AD_User_ID), user_list);
				region_user_map.put(new Integer(pojo.C_Region_ID), user_map);
				country_region_user_map.put(new Integer(pojo.C_Country_ID), region_user_map);
			}
		}

		for (MCountry c_country : MCountry.getCountries(Env.getCtx())) {
			Map<Integer, Map<Integer, List<UserPOJO>>> region_user_map = country_region_user_map.get(new Integer(
					c_country.getC_Country_ID()));
			if (region_user_map == null)
				continue;

			BroadcasterTreeNode<Object> country = new BroadcasterTreeNode<Object>(new KeyNamePair(
					c_country.getC_Country_ID(), c_country.getName()), new ArrayList<TreeNode<Object>>());
			country.isCountry = true;

			for (MRegion c_region : MRegion.getRegions(Env.getCtx(), c_country.getC_Country_ID())) {
				Map<Integer, List<UserPOJO>> user_map = region_user_map.get(new Integer(c_region.getC_Region_ID()));
				if (user_map == null)
					continue;

				BroadcasterTreeNode<Object> region = new BroadcasterTreeNode<Object>(new KeyNamePair(
						c_region.getC_Region_ID(), c_region.getName()), new ArrayList<TreeNode<Object>>());
				region.isRegion = true;

				for (Integer ad_user_id : user_map.keySet()) {
					List<UserPOJO> user_list = user_map.get(ad_user_id);
					UserPOJO lastPojo = null;
					for (UserPOJO pojo : user_list) {
						if (lastPojo == null || lastPojo.AD_User_ID != pojo.AD_User_ID) {
							BroadcasterTreeNode<Object> user = new BroadcasterTreeNode<Object>(pojo);
							region.add(user);
						}

						lastPojo = pojo;
					}
				}

				if (region.getChildCount() > 0)
					country.add(region);

			}

			if (country.getChildCount() > 0)
				countries.add(country);
		}

		if (countries.getChildCount() > 0)
			this.getRoot().add(countries);
	}

	private void getRoles() {
		BroadcasterTreeNode<Object> roles = new BroadcasterTreeNode<Object>(DPBroadcaster_Roles, new ArrayList<TreeNode<Object>>());
		this.getRoot().add(roles);

		Map<Integer, Map<Integer, List<UserPOJO>>> role_user_map = new TreeMap<Integer, Map<Integer, List<UserPOJO>>>();
		for (List<UserPOJO> users_list : users.values()) {
			for (UserPOJO pojo : users_list) {

				if (this.onlyOnlineUsers && !pojo.isOnline)
					continue;

				if (!Util.isEmpty(this.filterName)
						&& !pojo.Username.toUpperCase().contains(this.filterName.toUpperCase()))
					continue;

				Map<Integer, List<UserPOJO>> user_map = role_user_map.get(new Integer(pojo.AD_Role_ID));
				if (user_map == null) {
					user_map = new TreeMap<Integer, List<UserPOJO>>();
				}

				List<UserPOJO> user_list = user_map.get(new Integer(pojo.AD_User_ID));
				if (user_list == null) {
					user_list = new ArrayList<UserPOJO>();
				}
				if (!user_list.contains(pojo))
					user_list.add(pojo);

				user_map.put(new Integer(pojo.AD_User_ID), user_list);
				role_user_map.put(new Integer(pojo.AD_Role_ID), user_map);
			}
		}

		for (KeyNamePair ad_role : ad_roles) {

			Map<Integer, List<UserPOJO>> user_map = role_user_map.get(new Integer(ad_role.getKey()));
			if (user_map == null)
				continue;

			BroadcasterTreeNode<Object> role = new BroadcasterTreeNode<Object>(ad_role,
					new ArrayList<TreeNode<Object>>());
			role.isRole = true;
			roles.add(role);

			for (Integer ad_user_id : user_map.keySet()) {
				List<UserPOJO> user_list = user_map.get(ad_user_id);
				UserPOJO lastPojo = null;
				for (UserPOJO pojo : user_list) {
					if (lastPojo == null || lastPojo.AD_User_ID != pojo.AD_User_ID) {
						BroadcasterTreeNode<Object> user = new BroadcasterTreeNode<Object>(pojo);
						role.add(user);
					}

					lastPojo = pojo;
				}
			}
		}
	}

	private void getOrgs() {
		BroadcasterTreeNode<Object> orgs = new BroadcasterTreeNode<Object>(DPBroadcaster_Organizations,
				new ArrayList<TreeNode<Object>>());
		this.getRoot().add(orgs);

		Map<Integer, Map<Integer, List<UserPOJO>>> org_user_map = new TreeMap<Integer, Map<Integer, List<UserPOJO>>>();
		for (List<UserPOJO> users_list : users.values()) {
			for (UserPOJO pojo : users_list) {

				if (this.onlyOnlineUsers && !pojo.isOnline)
					continue;

				if (!Util.isEmpty(this.filterName)
						&& !pojo.Username.toUpperCase().contains(this.filterName.toUpperCase()))
					continue;

				Map<Integer, List<UserPOJO>> user_map = org_user_map.get(new Integer(pojo.AD_Org_ID));
				if (user_map == null) {
					user_map = new TreeMap<Integer, List<UserPOJO>>();
				}

				List<UserPOJO> user_list = user_map.get(new Integer(pojo.AD_User_ID));
				if (user_list == null) {
					user_list = new ArrayList<UserPOJO>();
				}
				if (!user_list.contains(pojo))
					user_list.add(pojo);

				user_map.put(new Integer(pojo.AD_User_ID), user_list);
				org_user_map.put(new Integer(pojo.AD_Org_ID), user_map);
			}
		}

		for (KeyNamePair ad_org : ad_orgs) {

			Map<Integer, List<UserPOJO>> user_map = org_user_map.get(new Integer(ad_org.getKey()));
			if (user_map == null)
				continue;

			BroadcasterTreeNode<Object> org = new BroadcasterTreeNode<Object>(ad_org, new ArrayList<TreeNode<Object>>());
			org.isOrg = true;
			orgs.add(org);

			for (Integer ad_user_id : user_map.keySet()) {
				List<UserPOJO> user_list = user_map.get(ad_user_id);
				UserPOJO lastPojo = null;
				for (UserPOJO pojo : user_list) {
					if (lastPojo == null || lastPojo.AD_User_ID != pojo.AD_User_ID) {
						BroadcasterTreeNode<Object> user = new BroadcasterTreeNode<Object>(pojo);
						org.add(user);
					}

					lastPojo = pojo;
				}
			}
		}

	}

	private void getClients() {

		BroadcasterTreeNode<Object> clients = new BroadcasterTreeNode<Object>(DPBroadcaster_Clients,
				new ArrayList<TreeNode<Object>>());
		this.getRoot().add(clients);

		Map<Integer, Map<Integer, List<UserPOJO>>> client_user_map = new TreeMap<Integer, Map<Integer, List<UserPOJO>>>();
		for (List<UserPOJO> users_list : users.values()) {
			for (UserPOJO pojo : users_list) {

				if (this.onlyOnlineUsers && !pojo.isOnline)
					continue;

				if (!Util.isEmpty(this.filterName)
						&& !pojo.Username.toUpperCase().contains(this.filterName.toUpperCase()))
					continue;

				Map<Integer, List<UserPOJO>> user_map = client_user_map.get(new Integer(pojo.AD_Client_ID));
				if (user_map == null) {
					user_map = new TreeMap<Integer, List<UserPOJO>>();
				}

				List<UserPOJO> user_list = user_map.get(new Integer(pojo.AD_User_ID));
				if (user_list == null) {
					user_list = new ArrayList<UserPOJO>();
				}
				if (!user_list.contains(pojo))
					user_list.add(pojo);

				user_map.put(new Integer(pojo.AD_User_ID), user_list);
				client_user_map.put(new Integer(pojo.AD_Client_ID), user_map);
			}
		}

		for (KeyNamePair ad_client : ad_clients) {

			Map<Integer, List<UserPOJO>> user_map = client_user_map.get(new Integer(ad_client.getKey()));
			if (user_map == null)
				continue;

			BroadcasterTreeNode<Object> client = new BroadcasterTreeNode<Object>(ad_client,
					new ArrayList<TreeNode<Object>>());
			client.isClient = true;
			clients.add(client);

			for (Integer ad_user_id : user_map.keySet()) {
				List<UserPOJO> user_list = user_map.get(ad_user_id);
				UserPOJO lastPojo = null;
				for (UserPOJO pojo : user_list) {
					if (lastPojo == null || lastPojo.AD_User_ID != pojo.AD_User_ID) {
						BroadcasterTreeNode<Object> user = new BroadcasterTreeNode<Object>(pojo);
						client.add(user);
					}

					lastPojo = pojo;
				}
			}
		}
	}

	public static BroadcasterTreeModel create(Integer ad_user_id, EventListener<Event> eventListener,
			boolean onlyOnlineUsers) {

		BroadcasterTreeNode<Object> root = new BroadcasterTreeNode<Object>("Root", new ArrayList<TreeNode<Object>>());
		BroadcasterTreeModel model = new BroadcasterTreeModel(root, ad_user_id, eventListener, onlyOnlineUsers);
		return model;
	}

	@Override
	public void render(Treeitem item, BroadcasterTreeNode<Object> data, int index) throws Exception {

		Treerow tr = new Treerow();
		item.appendChild(tr);
		item.setAttribute(ATTR_TREE_NODE, data);
		tr.addEventListener(Events.ON_CLICK, eventListener);

		Object dataObject = data.getData();
		if (dataObject instanceof String) {
			item.setTooltip("");
			tr.appendChild(new Treecell((String) dataObject));

		} else if (dataObject instanceof KeyNamePair) {

			Treecell tc = new Treecell();
			tr.appendChild(tc);
			A link = new A();
			tc.appendChild(link);
			KeyNamePair keynamepair = (KeyNamePair) dataObject;
			link.setLabel(keynamepair.getName());
			link.addEventListener(Events.ON_CLICK, eventListener);

			item.setTooltiptext(DPBroadcaster_Tooltip_Message_To + " " + keynamepair.getName());

			if (data.isClient)
				item.setAttribute(ATTR_AD_CLIENT_ID, new Integer(keynamepair.getKey()));
			if (data.isOrg)
				item.setAttribute(ATTR_AD_ORG_ID, new Integer(keynamepair.getKey()));
			if (data.isRole)
				item.setAttribute(ATTR_AD_ROLE_ID, new Integer(keynamepair.getKey()));
			if (data.isCountry)
				item.setAttribute(ATTR_C_COUNTRY_ID, new Integer(keynamepair.getKey()));
			if (data.isRegion)
				item.setAttribute(ATTR_C_REGION_ID, new Integer(keynamepair.getKey()));

		} else if (dataObject instanceof UserPOJO) {

			Treecell tc = new Treecell();
			tr.appendChild(tc);
			A link = new A();
			tc.appendChild(link);

			UserPOJO pojo = (UserPOJO) dataObject;

			if (pojo.isOnline)
				link.setImage(ThemeManager.getThemeResource("images/BPartner10.png"));
			else
				link.setImage(ThemeManager.getThemeResource("images/Cancel10.png"));

			link.setLabel(pojo.Username);
			link.addEventListener(Events.ON_CLICK, eventListener);

			item.setTooltiptext(DPBroadcaster_Tooltip_Message_To + " " + pojo.Username);
			item.setAttribute(ATTR_AD_USER_ID, pojo.AD_User_ID);
		}

	}

	public EventListener<Event> getEventListener() {
		return eventListener;
	}

	public void setEventListener(EventListener<Event> eventListener) {
		this.eventListener = eventListener;
	}

	public void setOnlyOnlineUsers(boolean selected) {

		this.onlyOnlineUsers = selected;
		refresh();
	}

	public void setFilterUserName(String filter) {

		this.filterName = filter;
		refresh();

	}

	public void userLoggedIn(Integer user_id) {
		List<UserPOJO> user_list = users.get(user_id);
		for (UserPOJO pojo : user_list)
			pojo.isOnline = true;
	}

	public void userLoggedOut(Integer user_id) {
		List<UserPOJO> user_list = users.get(user_id);
		for (UserPOJO pojo : user_list)
			pojo.isOnline = false;
	}

}
