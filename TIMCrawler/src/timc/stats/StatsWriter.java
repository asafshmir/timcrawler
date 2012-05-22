package timc.stats;


public interface StatsWriter {
	
	/** Write a new <code>TestRecord</code>.
	 * 
	 * @param test the <code>TestRecord</code> to be written
	 * @return a unique id that should be given to <code>writeSessionStats</code>.
	 */
	public String writeTestStats(TestRecord test);
	
	/** Update a test record's data.
	 * 
	 * @param testId the id of the test record to be updates.
	 * @param test the data that should be used to update the record.
	 */
	public void updateTestStats(Object testId, TestRecord test);
	
	/** Write a new <code>SessionRecord</code>.
	 * 
	 * @param testId Id received by <code>writeTrackerSessionStats</code>.
	 * @param session the <code>SessionRecord</code> to be written.
	 */
	public void writeSessionStats(Object testId, SessionRecord session);
	
	/** Update the tracker's <code>SessionRecord</code>, or insert a new one if it doesn't exist.
	 * 
	 * @param testId Id received by <code>writeTrackerSessionStats</code>.
	 * @param session the <code>SessionRecord</code> to be written.
	 */
	public void writeTrackerSessionStats(Object testId, SessionRecord session);
	
	/** Initialize the writer.
	 * 
	 * @return true if initialization succeeded, and false otherwise.
	 */
	public boolean initWriter();
	
	/** Close the writer.
	 */
	public void closeWriter();

}
