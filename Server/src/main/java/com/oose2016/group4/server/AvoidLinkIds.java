package com.oose2016.group4.server;

/**
 * LinkIds to avoid
 */
public class AvoidLinkIds {
	/**
	 * Nested class for LinkIds.
	 */
	public static class LinkId {
		private int linkId, count;

		/**
		 * Get link id.
		 * @return linkId
		 */
		public int getLinkId() {
			return linkId;
		}
		
		/**
		 * Set link id
		 * @param id Id for linkId
		 */
		public void setLinkId(int id) {
			linkId = id;
		}
	}

	@SuppressWarnings("unused")
	private int[] red, yellow;

	/**
	 * Constructor for AvoidLinkIds
	 * @param red array of red linkIds
	 * @param yellow array of yellow linkIds
	 */
	public AvoidLinkIds(int[] red, int[] yellow) {
		this.red = red;
		this.yellow = yellow;
	}

	/**
	 * Get Red link ids.
	 * @return array of red linkids
	 */
	public int[] getRed() {
		return this.red;
	}
	
	/**
	 * Get yellow link ids.
	 * @return array of yellow linkids
	 */
	public int[] getYellow() {
		return this.yellow;
	}
}
