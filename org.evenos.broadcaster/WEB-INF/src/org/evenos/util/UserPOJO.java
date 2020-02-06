package org.evenos.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.KeyNamePair;

/**
 * POJO wich holds the User id, name, onlinestatus and a client, org, role,
 * country and region.
 * 
 * @author jant
 * 
 */
public class UserPOJO {

	private static CLogger log = CLogger.getCLogger(UserPOJO.class);

	public int AD_User_ID;
	public int AD_Client_ID;
	public int AD_Org_ID;
	public int AD_Role_ID;
	public int C_Country_ID;
	public int C_Region_ID;
	public boolean isOnline;
	public String Username;

	public UserPOJO(int AD_User_ID, String user_name, int isOnline,
			int AD_Client_ID, int AD_Org_ID, int AD_Role_ID, int C_Country_ID,
			int C_Region_ID) {
		this.AD_User_ID = AD_User_ID;
		this.Username = user_name;
		this.isOnline = isOnline > 0 ? true : false;
		this.AD_Client_ID = AD_Client_ID;
		this.AD_Org_ID = AD_Org_ID;
		this.AD_Role_ID = AD_Role_ID;
		this.C_Country_ID = C_Country_ID;
		this.C_Region_ID = C_Region_ID;
	}

	public UserPOJO() {

	}

	/**
	 * Get all Clients the user can access
	 * 
	 * @return
	 */
	public static List<KeyNamePair> getClients(int ad_user_id) {

		List<KeyNamePair> clients = new ArrayList<KeyNamePair>();
		StringBuilder sql = new StringBuilder(
				"SELECT  DISTINCT cli.AD_Client_ID, cli.Name, u.AD_User_ID, u.Name");
		sql.append(" FROM AD_User_Roles ur")
				.append(" INNER JOIN AD_User u on (ur.AD_User_ID=u.AD_User_ID)")
				.append(" INNER JOIN AD_Client cli on (ur.AD_Client_ID=cli.AD_Client_ID)")
				.append(" WHERE ur.IsActive='Y'")
				.append(" AND cli.IsActive='Y'").append(" AND u.IsActive='Y'")
				.append(" AND u.AD_User_ID=? ")
				.append(" ORDER BY cli.ad_client_id");
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setInt(1, ad_user_id);
			rs = pstmt.executeQuery();

			while (rs.next() && rs != null) {
				clients.add(new KeyNamePair(rs.getInt(1), rs.getString(2)));
			}

		} catch (SQLException ex) {
			log.log(Level.SEVERE, sql.toString(), ex);
		} finally {
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}
		return clients;
	}

	/**
	 * Get all Roles the user can access
	 * 
	 * @return
	 */
	public static List<KeyNamePair> getRoles(int ad_user_id) {

		ArrayList<KeyNamePair> roleList = new ArrayList<KeyNamePair>();

		StringBuilder sql = new StringBuilder();
		sql.append("select r.ad_role_id, r.name ");
		sql.append("from ad_user u join ad_user_roles ur on u.ad_user_id = ur.ad_user_id ");
		sql.append("join ad_role r on r.ad_role_id = ur.ad_role_id ");
		sql.append("where u.ad_user_id = ? ");
		sql.append("and ur.isactive='Y' ");
		sql.append("and u.isactive='Y' ");
		sql.append("and r.isactive='Y' ");
		sql.append("group by r.ad_role_id, r.name ");
		sql.append("order by r.ad_role_id ");

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setInt(1, ad_user_id);
			rs = pstmt.executeQuery();

			while (rs.next() && rs != null) {
				int AD_Role_ID = rs.getInt(1);
				roleList.add(new KeyNamePair(new Integer(AD_Role_ID), rs.getString(2)));
			}

		} catch (SQLException ex) {
			log.log(Level.SEVERE, sql.toString(), ex);
		} finally {
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}
		return roleList;
	}

	/**
	 * Get all Orgs the user can access
	 * 
	 * @return
	 */
	public static List<KeyNamePair> getOrgs(int ad_user_id,
			List<KeyNamePair> ad_client_ids, List<KeyNamePair> ad_role_ids) {

		StringBuilder clients = new StringBuilder();
		StringBuilder roles = new StringBuilder();

		for (KeyNamePair ad_client_id : ad_client_ids)
			clients.append(Integer.toString(ad_client_id.getKey())).append(",");
		clients.deleteCharAt(clients.lastIndexOf(","));

		for (KeyNamePair ad_role_id : ad_role_ids)
			roles.append(Integer.toString(ad_role_id.getKey())).append(",");
		roles.deleteCharAt(roles.lastIndexOf(","));

		ArrayList<KeyNamePair> orgList = new ArrayList<KeyNamePair>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT distinct o.AD_Org_ID,o.Name ");
		sql.append("FROM AD_Org o ");
		sql.append("INNER JOIN AD_Role r on (r.AD_Role_ID in (")
				.append(roles.toString()).append(")) ");
		sql.append("INNER JOIN AD_Client c on (c.AD_Client_ID in (")
				.append(clients.toString()).append(")) ");
		sql.append("WHERE o.IsActive='Y' ");
		sql.append("AND o.AD_Client_ID IN (0, c.AD_Client_ID) ");
		sql.append("AND o.IsSummary='N' ");
		sql.append("AND (r.IsAccessAllOrgs='Y' ");
		sql.append("OR (r.IsUseUserOrgAccess='N' ");
		sql.append("AND o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_Role_OrgAccess ra ");
		sql.append("WHERE ra.AD_Role_ID=r.AD_Role_ID AND ra.IsActive='Y')) ");
		sql.append("OR (r.IsUseUserOrgAccess='Y' AND o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_User_OrgAccess ua ");
		sql.append("WHERE ua.AD_User_ID=? ");
		sql.append("AND ua.IsActive='Y'))) ");
		sql.append("ORDER BY o.ad_org_id ");

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setInt(1, ad_user_id);

			rs = pstmt.executeQuery();

			while (rs.next() && rs != null) {
				int AD_Org_ID = rs.getInt(1);
				orgList.add(new KeyNamePair(new Integer(AD_Org_ID), rs.getString(2)));
			}

		} catch (SQLException ex) {
			log.log(Level.SEVERE, sql.toString(), ex);
		} finally {
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}
		return orgList;
	}
	
	/**
	 * Get all users the given user can see by his clients, orgs and roles
	 * @param ad_user_id
	 * @return
	 */
	public static Map<Integer, List<UserPOJO>> findVisibleUsers(Integer ad_user_id) {

		Map<Integer, List<UserPOJO>> users = new TreeMap<Integer, List<UserPOJO>>();

		StringBuilder sql = new StringBuilder();
		sql.append("select u.ad_user_id, u.name, ur.ad_client_id, c.name, o.ad_org_id, o.name,r.ad_role_id, r.name, cloc.c_country_id, cloc.c_region_id, s.processed ");
		sql.append("from ad_user u join ad_user_roles ur on u.ad_user_id = ur.ad_user_id ");
		sql.append("join ad_role r on r.ad_role_id = ur.ad_role_id ");
		sql.append("join ad_role_orgaccess roa on roa.ad_role_id = r.ad_role_id ");
		sql.append("join ad_org o on roa.ad_org_id = o.ad_org_id ");
		sql.append("join ad_client c on ur.ad_client_id = c.ad_client_id ");
		sql.append("LEFT join c_bpartner_location bploc on u.c_bpartner_location_id = bploc.c_bpartner_location_id ");
		sql.append("LEFT join c_location cloc on bploc.c_location_id = cloc.c_location_id ");
		sql.append("LEFT JOIN ad_session s on (u.ad_user_id = s.createdby and s.processed = 'N') ");
		sql.append("where u.ad_client_id in ( ");
		sql.append("	SELECT  DISTINCT cli.AD_Client_ID ");
		sql.append("	FROM AD_User_Roles ur ");
		sql.append("	INNER JOIN AD_User u on (ur.AD_User_ID=u.AD_User_ID) ");
		sql.append("	INNER JOIN AD_Client cli on (ur.AD_Client_ID=cli.AD_Client_ID) ");
		sql.append("	WHERE ur.IsActive='Y' ");
		sql.append("	AND cli.IsActive='Y' ");
		sql.append("	AND u.IsActive='Y' ");
		sql.append("	AND u.AD_User_ID=? ");
		sql.append(") ");
		sql.append("and o.ad_org_id in ( ");
		sql.append("	SELECT distinct o.AD_Org_ID ");
		sql.append("	FROM AD_Org o ");
		sql.append("	INNER JOIN AD_Role r on (r.AD_Role_ID in ( ");
		sql.append("		select r.ad_role_id ");
		sql.append("		from ad_user u join ad_user_roles ur on u.ad_user_id = ur.ad_user_id ");
		sql.append("		join ad_role r on r.ad_role_id = ur.ad_role_id ");
		sql.append("		where u.ad_user_id = ? ");
		sql.append("		and ur.isactive='Y' ");
		sql.append("		and u.isactive='Y' ");
		sql.append("		and r.isactive='Y' ");
		sql.append("		group by r.ad_role_id ");
//		sql.append("		order by r.ad_role_id ");
		sql.append("	)) ");
		sql.append("	INNER JOIN AD_Client c on (c.AD_Client_ID in ( ");
		sql.append("		SELECT  DISTINCT cli.AD_Client_ID ");
		sql.append("		FROM AD_User_Roles ur ");
		sql.append("		INNER JOIN AD_User u on (ur.AD_User_ID=u.AD_User_ID) ");
		sql.append("		INNER JOIN AD_Client cli on (ur.AD_Client_ID=cli.AD_Client_ID) ");
		sql.append("		WHERE ur.IsActive='Y' ");
		sql.append("		AND cli.IsActive='Y' ");
		sql.append("		AND u.IsActive='Y' ");
		sql.append("		AND u.AD_User_ID=? ");
		sql.append("	)) ");
		sql.append("	WHERE o.IsActive='Y'  ");
		sql.append("	AND o.AD_Client_ID IN (c.AD_Client_ID) ");
		sql.append("	AND o.IsSummary='N' ");
		sql.append("	AND (r.IsAccessAllOrgs='Y' ");
		sql.append("	OR (r.IsUseUserOrgAccess='N'  ");
		sql.append("	AND o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_Role_OrgAccess ra  ");
		sql.append("	WHERE ra.AD_Role_ID=r.AD_Role_ID AND ra.IsActive='Y')) ");
		sql.append("	OR (r.IsUseUserOrgAccess='Y' AND o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_User_OrgAccess ua ");
		sql.append("	WHERE ua.AD_User_ID= ? ");
		sql.append("	AND ua.IsActive='Y'))) ");
//		sql.append("	ORDER BY o.ad_org_id ");
		sql.append(") ");
		sql.append("and r.ad_role_id in ( ");
		sql.append("	select r.ad_role_id ");
		sql.append("	from ad_user u join ad_user_roles ur on u.ad_user_id = ur.ad_user_id ");
		sql.append("	join ad_role r on r.ad_role_id = ur.ad_role_id ");
		sql.append("	where u.ad_user_id = ? ");
		sql.append("	and ur.isactive='Y' ");
		sql.append("	and u.isactive='Y' ");
		sql.append("	and r.isactive='Y' ");
		sql.append("	group by r.ad_role_id ");
//		sql.append("	order by r.ad_role_id ");
		sql.append(") ");
		sql.append("and u.isActive='Y' ");
		sql.append("and o.isactive='Y' ");
		sql.append("and r.isactive='Y' ");
		sql.append("and roa.isactive='Y' ");
		sql.append("and ur.isactive='Y' ");
		sql.append("and ur.dpbinvisibleinrole = 'N' ");
		sql.append("order by u.ad_user_id, c.ad_client_id, o.ad_org_id, r.ad_role_id ");

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setInt(1, ad_user_id);
			pstmt.setInt(2, ad_user_id);
			pstmt.setInt(3, ad_user_id);
			pstmt.setInt(4, ad_user_id);
			pstmt.setInt(5, ad_user_id);
			rs = pstmt.executeQuery();

			while (rs.next() && rs != null) {
				log.fine("User: " + rs.getString(2) + " - Client: "
						+ rs.getString(4) + " - Org: " + rs.getString(6)
						+ " Role: " + rs.getString(8));

				UserPOJO pojo = new UserPOJO();
				pojo.AD_User_ID = rs.getInt(1);
				pojo.Username = rs.getString(2);
				pojo.AD_Client_ID = rs.getInt(3);
				pojo.AD_Org_ID = rs.getInt(5);
				pojo.AD_Role_ID = rs.getInt(7);
				pojo.C_Country_ID = rs.getInt(9);
				pojo.C_Region_ID = rs.getInt(10);
				pojo.isOnline = rs.getString(11) != null && rs.getString(11).equalsIgnoreCase("N") ? true : false;
				
				List<UserPOJO> user_list = users.get(new Integer(rs.getInt(1)));
				if(user_list == null){
					user_list = new ArrayList<UserPOJO>();
					users.put(new Integer(rs.getInt(1)), user_list);
				}
				user_list.add(pojo);
			}

		} catch (SQLException ex) {
			log.log(Level.SEVERE, sql.toString(), ex);
		} finally {
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}

		return users;
	}


	public static List<Integer> getClientUserIDs(Integer ad_user_id, Integer ad_client_id){
		
		StringBuilder sql = new StringBuilder();
		sql.append("select distinct u.ad_user_id, u.name ");
		sql.append("from ad_user u join ad_user_roles ur on u.ad_user_id = ur.ad_user_id ");
		sql.append("join ad_role r on r.ad_role_id = ur.ad_role_id ");
		sql.append("join ad_role_orgaccess roa on roa.ad_role_id = r.ad_role_id ");
		sql.append("join ad_org o on roa.ad_org_id = o.ad_org_id ");
		sql.append("join ad_client c on ur.ad_client_id = c.ad_client_id ");
		sql.append("LEFT join c_bpartner_location bploc on u.c_bpartner_location_id = bploc.c_bpartner_location_id ");
		sql.append("LEFT join c_location cloc on bploc.c_location_id = cloc.c_location_id ");
		sql.append("LEFT JOIN ad_session s on (u.ad_user_id = s.createdby and s.processed = 'N') ");
		sql.append("where u.ad_client_id = ? ");
		sql.append("and o.ad_org_id in ( ");
		sql.append("	SELECT distinct o.AD_Org_ID ");
		sql.append("	FROM AD_Org o ");
		sql.append("	INNER JOIN AD_Role r on (r.AD_Role_ID in ( ");
		sql.append("		select r.ad_role_id ");
		sql.append("		from ad_user u join ad_user_roles ur on u.ad_user_id = ur.ad_user_id ");
		sql.append("		join ad_role r on r.ad_role_id = ur.ad_role_id ");
		sql.append("		where u.ad_user_id = ? ");
		sql.append("		and ur.isactive='Y' ");
		sql.append("		and u.isactive='Y' ");
		sql.append("		and r.isactive='Y' ");
		sql.append("		group by r.ad_role_id ");
		sql.append("	)) ");
		sql.append("	INNER JOIN AD_Client c on (c.AD_Client_ID in ( ");
		sql.append("		SELECT  DISTINCT cli.AD_Client_ID ");
		sql.append("		FROM AD_User_Roles ur ");
		sql.append("		INNER JOIN AD_User u on (ur.AD_User_ID=u.AD_User_ID) ");
		sql.append("		INNER JOIN AD_Client cli on (ur.AD_Client_ID=cli.AD_Client_ID) ");
		sql.append("		WHERE ur.IsActive='Y' ");
		sql.append("		AND cli.IsActive='Y' ");
		sql.append("		AND u.IsActive='Y' ");
		sql.append("		AND u.AD_User_ID=? ");
		sql.append("	)) ");
		sql.append("	WHERE o.IsActive='Y'  ");
		sql.append("	AND o.AD_Client_ID IN (c.AD_Client_ID) ");
		sql.append("	AND o.IsSummary='N' ");
		sql.append("	AND (r.IsAccessAllOrgs='Y' ");
		sql.append("	OR (r.IsUseUserOrgAccess='N'  ");
		sql.append("	AND o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_Role_OrgAccess ra  ");
		sql.append("	WHERE ra.AD_Role_ID=r.AD_Role_ID AND ra.IsActive='Y')) ");
		sql.append("	OR (r.IsUseUserOrgAccess='Y' AND o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_User_OrgAccess ua ");
		sql.append("	WHERE ua.AD_User_ID=? ");
		sql.append("	AND ua.IsActive='Y'))) ");
		sql.append(") ");
		sql.append("and r.ad_role_id in ( ");
		sql.append("	select r.ad_role_id ");
		sql.append("	from ad_user u join ad_user_roles ur on u.ad_user_id = ur.ad_user_id ");
		sql.append("	join ad_role r on r.ad_role_id = ur.ad_role_id ");
		sql.append("	where u.ad_user_id = ? ");
		sql.append("	and ur.isactive='Y' ");
		sql.append("	and u.isactive='Y' ");
		sql.append("	and r.isactive='Y' ");
		sql.append("	group by r.ad_role_id ");
		sql.append(") ");
		sql.append("and u.isActive='Y' ");
		sql.append("and o.isactive='Y' ");
		sql.append("and r.isactive='Y' ");
		sql.append("and roa.isactive='Y' ");
		sql.append("and ur.isactive='Y' ");
		sql.append("and ur.dpbinvisibleinrole = 'N' ");
		sql.append("order by u.ad_user_id ");
		
		List<Integer> retVal = new ArrayList<Integer>();
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setInt(1, ad_client_id);
			pstmt.setInt(2, ad_user_id);
			pstmt.setInt(3, ad_user_id);
			pstmt.setInt(4, ad_user_id);
			pstmt.setInt(5, ad_user_id);
			rs = pstmt.executeQuery();

			while (rs.next() && rs != null) {
				retVal.add(new Integer(rs.getInt(1)));
			}

		} catch (SQLException ex) {
			log.log(Level.SEVERE, sql.toString(), ex);
		} finally {
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}
		
		return retVal;
	}
	public static List<Integer> getOrgUserIDs(Integer ad_user_id, Integer ad_org_id){
		StringBuilder sql = new StringBuilder();
		sql.append("select distinct u.ad_user_id, u.name ");
		sql.append("from ad_user u join ad_user_roles ur on u.ad_user_id = ur.ad_user_id ");
		sql.append("join ad_role r on r.ad_role_id = ur.ad_role_id ");
		sql.append("join ad_role_orgaccess roa on roa.ad_role_id = r.ad_role_id ");
		sql.append("join ad_org o on roa.ad_org_id = o.ad_org_id ");
		sql.append("join ad_client c on ur.ad_client_id = c.ad_client_id ");
		sql.append("LEFT join c_bpartner_location bploc on u.c_bpartner_location_id = bploc.c_bpartner_location_id ");
		sql.append("LEFT join c_location cloc on bploc.c_location_id = cloc.c_location_id ");
		sql.append("LEFT JOIN ad_session s on (u.ad_user_id = s.createdby and s.processed = 'N') ");
		sql.append("where u.ad_client_id in ( ");
		sql.append("	SELECT  DISTINCT cli.AD_Client_ID ");
		sql.append("	FROM AD_User_Roles ur ");
		sql.append("	INNER JOIN AD_User u on (ur.AD_User_ID=u.AD_User_ID) ");
		sql.append("	INNER JOIN AD_Client cli on (ur.AD_Client_ID=cli.AD_Client_ID) ");
		sql.append("	WHERE ur.IsActive='Y' ");
		sql.append("	AND cli.IsActive='Y' ");
		sql.append("	AND u.IsActive='Y' ");
		sql.append("	AND u.AD_User_ID=? ");
		sql.append(") ");
		sql.append("and o.ad_org_id = ? ");
		sql.append("and r.ad_role_id in ( ");
		sql.append("	select r.ad_role_id ");
		sql.append("	from ad_user u join ad_user_roles ur on u.ad_user_id = ur.ad_user_id ");
		sql.append("	join ad_role r on r.ad_role_id = ur.ad_role_id ");
		sql.append("	where u.ad_user_id = ? ");
		sql.append("	and ur.isactive='Y' ");
		sql.append("	and u.isactive='Y' ");
		sql.append("	and r.isactive='Y' ");
		sql.append("	group by r.ad_role_id ");
		sql.append(") ");
		sql.append("and u.isActive='Y' ");
		sql.append("and o.isactive='Y' ");
		sql.append("and r.isactive='Y' ");
		sql.append("and roa.isactive='Y' ");
		sql.append("and ur.isactive='Y' ");
		sql.append("and ur.dpbinvisibleinrole = 'N' ");
		sql.append("order by u.ad_user_id ");
		
		List<Integer> retVal = new ArrayList<Integer>();
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setInt(1, ad_user_id);
			pstmt.setInt(2, ad_org_id);
			pstmt.setInt(3, ad_user_id);
			rs = pstmt.executeQuery();

			while (rs.next() && rs != null) {
				retVal.add(new Integer(rs.getInt(1)));
			}

		} catch (SQLException ex) {
			log.log(Level.SEVERE, sql.toString(), ex);
		} finally {
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}
		
		
		return retVal;
	}
	public static List<Integer> getRoleUserIDs(Integer ad_user_id, Integer ad_role_id){
		StringBuilder sql = new StringBuilder();
		sql.append("select distinct u.ad_user_id, u.name ");
		sql.append("from ad_user u join ad_user_roles ur on u.ad_user_id = ur.ad_user_id ");
		sql.append("join ad_role r on r.ad_role_id = ur.ad_role_id ");
		sql.append("join ad_role_orgaccess roa on roa.ad_role_id = r.ad_role_id ");
		sql.append("join ad_org o on roa.ad_org_id = o.ad_org_id ");
		sql.append("join ad_client c on ur.ad_client_id = c.ad_client_id ");
		sql.append("LEFT join c_bpartner_location bploc on u.c_bpartner_location_id = bploc.c_bpartner_location_id ");
		sql.append("LEFT join c_location cloc on bploc.c_location_id = cloc.c_location_id ");
		sql.append("LEFT JOIN ad_session s on (u.ad_user_id = s.createdby and s.processed = 'N') ");
		sql.append("where u.ad_client_id in ( ");
		sql.append("	SELECT  DISTINCT cli.AD_Client_ID ");
		sql.append("	FROM AD_User_Roles ur ");
		sql.append("	INNER JOIN AD_User u on (ur.AD_User_ID=u.AD_User_ID) ");
		sql.append("	INNER JOIN AD_Client cli on (ur.AD_Client_ID=cli.AD_Client_ID) ");
		sql.append("	WHERE ur.IsActive='Y' ");
		sql.append("	AND cli.IsActive='Y' ");
		sql.append("	AND u.IsActive='Y' ");
		sql.append("	AND u.AD_User_ID=? ");
		sql.append(") ");
		sql.append("and o.ad_org_id in ( ");
		sql.append("	SELECT distinct o.AD_Org_ID ");
		sql.append("	FROM AD_Org o ");
		sql.append("	INNER JOIN AD_Role r on (r.AD_Role_ID = ? )");
		sql.append("	INNER JOIN AD_Client c on (c.AD_Client_ID in ( ");
		sql.append("		SELECT  DISTINCT cli.AD_Client_ID ");
		sql.append("		FROM AD_User_Roles ur ");
		sql.append("		INNER JOIN AD_User u on (ur.AD_User_ID=u.AD_User_ID) ");
		sql.append("		INNER JOIN AD_Client cli on (ur.AD_Client_ID=cli.AD_Client_ID) ");
		sql.append("		WHERE ur.IsActive='Y' ");
		sql.append("		AND cli.IsActive='Y' ");
		sql.append("		AND u.IsActive='Y' ");
		sql.append("		AND u.AD_User_ID=? ");
		sql.append("	)) ");
		sql.append("	WHERE o.IsActive='Y'  ");
		sql.append("	AND o.AD_Client_ID IN (c.AD_Client_ID) ");
		sql.append("	AND o.IsSummary='N' ");
		sql.append("	AND (r.IsAccessAllOrgs='Y' ");
		sql.append("	OR (r.IsUseUserOrgAccess='N'  ");
		sql.append("	AND o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_Role_OrgAccess ra  ");
		sql.append("	WHERE ra.AD_Role_ID=r.AD_Role_ID AND ra.IsActive='Y')) ");
		sql.append("	OR (r.IsUseUserOrgAccess='Y' AND o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_User_OrgAccess ua ");
		sql.append("	WHERE ua.AD_User_ID=? ");
		sql.append("	AND ua.IsActive='Y'))) ");
		sql.append(") ");
		sql.append("and r.ad_role_id = ?  ");
		sql.append("and u.isActive='Y' ");
		sql.append("and o.isactive='Y' ");
		sql.append("and r.isactive='Y' ");
		sql.append("and roa.isactive='Y' ");
		sql.append("and ur.isactive='Y' ");
		sql.append("and ur.dpbinvisibleinrole = 'N' ");
		sql.append("order by u.ad_user_id ");
		
		List<Integer> retVal = new ArrayList<Integer>();
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = DB.prepareStatement(sql.toString(), null);
			pstmt.setInt(1, ad_user_id);
			pstmt.setInt(2, ad_role_id);
			pstmt.setInt(3, ad_user_id);
			pstmt.setInt(4, ad_user_id);
			pstmt.setInt(5, ad_role_id);
			rs = pstmt.executeQuery();

			while (rs.next() && rs != null) {
				retVal.add(new Integer(rs.getInt(1)));
			}

		} catch (SQLException ex) {
			log.log(Level.SEVERE, sql.toString(), ex);
		} finally {
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}
		
		
		return retVal;
	}
	public static List<Integer> getCountryUserIDs(Integer ad_user_id, Integer c_country_id){
		
		Map<Integer, List<UserPOJO>> user_map = findVisibleUsers(ad_user_id);
		
		List<Integer> retVal = new ArrayList<Integer>();
		
		for(Integer user_id : user_map.keySet()){
			List<UserPOJO> pojo_list = user_map.get(user_id);
			for(UserPOJO pojo : pojo_list){
				if (pojo.C_Country_ID == c_country_id.intValue()) {
					if(!retVal.contains(user_id)){
						retVal.add(user_id);
					}
				}
			}
		}
		
		return retVal;
	}
	public static List<Integer> getRegionUserIDs(Integer ad_user_id, Integer c_region_id){
		Map<Integer, List<UserPOJO>> user_map = findVisibleUsers(ad_user_id);
		
		List<Integer> retVal = new ArrayList<Integer>();
		
		for(Integer user_id : user_map.keySet()){
			List<UserPOJO> pojo_list = user_map.get(user_id);
			for(UserPOJO pojo : pojo_list){
				if (pojo.C_Region_ID == c_region_id.intValue()) {
					if(!retVal.contains(user_id)){
						retVal.add(user_id);
					}
				}
			}
		}
		
		return retVal;
	}
}
