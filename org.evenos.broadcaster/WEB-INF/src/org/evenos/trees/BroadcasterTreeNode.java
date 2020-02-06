package org.evenos.trees;

import java.util.Collection;

import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.TreeNode;

public class BroadcasterTreeNode<E> extends DefaultTreeNode<E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8924540209550781217L;
	
	
	public boolean isClient;
	public boolean isOrg;
	public boolean isRole;
	public boolean isCountry;
	public boolean isRegion;
	
	public BroadcasterTreeNode(E data) {
		super(data);
	}

	public BroadcasterTreeNode(E data, boolean nullAsMax) {
		super(data, nullAsMax);
	}

	public BroadcasterTreeNode(E data,
			Collection<? extends TreeNode<E>> children) {
		super(data, children);
	}
	
	public BroadcasterTreeNode(E data,
			Collection<? extends TreeNode<E>> children, boolean nullAsMax) {
		super(data, children, nullAsMax);
	}

	public BroadcasterTreeNode(E data, TreeNode<E>[] children) {
		super(data, children);
	}

}
