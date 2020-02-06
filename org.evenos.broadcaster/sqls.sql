--get users with same roles as given
select u.ad_user_id, u.name, ur.ad_client_id, c.name, o.ad_org_id, o.name,r.ad_role_id, r.name, cloc.c_country_id, cloc.c_region_id, s.processed
from ad_user u join ad_user_roles ur on u.ad_user_id = ur.ad_user_id
join ad_role r on r.ad_role_id = ur.ad_role_id
join ad_role_orgaccess roa on roa.ad_role_id = r.ad_role_id
join ad_org o on roa.ad_org_id = o.ad_org_id
join ad_client c on ur.ad_client_id = c.ad_client_id
LEFT join c_bpartner_location bploc on u.c_bpartner_location_id = bploc.c_bpartner_location_id
LEFT join c_location cloc on bploc.c_location_id = cloc.c_location_id
LEFT JOIN ad_session s on (u.ad_user_id = s.createdby and s.processed = 'N')
where u.ad_client_id in (
	SELECT  DISTINCT cli.AD_Client_ID
	FROM AD_User_Roles ur
	INNER JOIN AD_User u on (ur.AD_User_ID=u.AD_User_ID)
	INNER JOIN AD_Client cli on (ur.AD_Client_ID=cli.AD_Client_ID)
	WHERE ur.IsActive='Y'
	AND cli.IsActive='Y'
	AND u.IsActive='Y'
	AND u.AD_User_ID=100
)
and o.ad_org_id in (
	SELECT distinct o.AD_Org_ID
	FROM AD_Org o
	INNER JOIN AD_Role r on (r.AD_Role_ID in (
		select r.ad_role_id
		from ad_user u join ad_user_roles ur on u.ad_user_id = ur.ad_user_id
		join ad_role r on r.ad_role_id = ur.ad_role_id
		where u.ad_user_id = 100
		and ur.isactive='Y'
		and u.isactive='Y'
		and r.isactive='Y'
		group by r.ad_role_id
		order by r.ad_role_id
	))
	INNER JOIN AD_Client c on (c.AD_Client_ID in (
		SELECT  DISTINCT cli.AD_Client_ID
		FROM AD_User_Roles ur
		INNER JOIN AD_User u on (ur.AD_User_ID=u.AD_User_ID)
		INNER JOIN AD_Client cli on (ur.AD_Client_ID=cli.AD_Client_ID)
		WHERE ur.IsActive='Y'
		AND cli.IsActive='Y'
		AND u.IsActive='Y'
		AND u.AD_User_ID=100
	))
	WHERE o.IsActive='Y' 
	AND o.AD_Client_ID IN (c.AD_Client_ID)
	AND o.IsSummary='N'
	AND (r.IsAccessAllOrgs='Y'
	OR (r.IsUseUserOrgAccess='N' 
	AND o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_Role_OrgAccess ra 
	WHERE ra.AD_Role_ID=r.AD_Role_ID AND ra.IsActive='Y'))
	OR (r.IsUseUserOrgAccess='Y' AND o.AD_Org_ID IN (SELECT AD_Org_ID FROM AD_User_OrgAccess ua
	WHERE ua.AD_User_ID=100
	AND ua.IsActive='Y')))
	ORDER BY o.ad_org_id
)
and r.ad_role_id in (
	select r.ad_role_id
	from ad_user u join ad_user_roles ur on u.ad_user_id = ur.ad_user_id
	join ad_role r on r.ad_role_id = ur.ad_role_id
	where u.ad_user_id = 100
	and ur.isactive='Y'
	and u.isactive='Y'
	and r.isactive='Y'
	group by r.ad_role_id
	order by r.ad_role_id
)
and u.isActive='Y'
and o.isactive='Y'
and r.isactive='Y'
and roa.isactive='Y'
and ur.isactive='Y'
order by u.ad_user_id, c.ad_client_id, o.ad_org_id, r.ad_role_id
 
