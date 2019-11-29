package uk.ac.dmu.iesd.cascade.context;

import java.io.File;
import java.io.IOException;

import java.util.*;

import javax.swing.JComponent;

import org.jfree.chart.ChartPanel;

import cern.jet.random.EmpiricalWalker;
import cern.jet.random.Normal;
import cern.jet.random.Binomial;
import cern.jet.random.Uniform;
import cern.jet.random.Poisson;

import repast.simphony.context.*;
import repast.simphony.engine.schedule.*;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.projection.*;
import repast.simphony.ui.widget.SnapshotTaker;
import repast.simphony.engine.environment.RunEnvironment;

import uk.ac.dmu.iesd.cascade.base.Consts;
import uk.ac.dmu.iesd.cascade.market.IBMTrader;
import uk.ac.dmu.iesd.cascade.market.IPxTrader;
import uk.ac.dmu.iesd.cascade.market.ITrader;

/**
 * <em>CascadeContext</em> is the main context for the <em>Cascade</em> framework.
 * Context can be seen as container or a virtual environment that is populated 
 * with agents, proto-agents or other actors/components, etc.
 * The <code>CascadeContext</code> holds internal states (information) that could be 
 * useful for population (e.g. agents) about the world (environment) in which they live in.
 * At this moment, for instance, the information about the weather is provided to agents
 * through the public interface implemented by this class. These can be seen a global
 * information/behaviour. 
 *   
 * @author J. Richard Snape
 * @author Babak Mahdavi
 * @version $Revision: 1.2 $ $Date: 2011/05/12 11:00:00 $
 * 
 * Version history (for intermediate steps see Git repository history
 * 
 * 1.1 - File Initial scenario creator (??)
 * 1.2 - Class name has been changed
 *       Boolean verbose variable has been added
 *       Name of some variables changed
 *       Babak 
 * 1.3 - float values changed to double    
 *       
 *       
 */
public class CascadeContext extends DefaultContext{
	

	/*
	 * Context parameters
	 * 
	 * This is place to add any context-specific environment variables
	 * Used for (e.g.) weather, system base demand etc.
	 * things stored here should be of the type that are loaded once only, at simulation
	 * start and stored for the entire duration of the simulation.
	 * 
	 */

	// Note at the moment, no geographical info is needed to read the weather
	// this is because weather is a flat file and not spatially differentiated

	int weatherDataLength; // length of arrays - note that it is a condition that each row of the input file
	// represents one time step, but the model is agnostic to what time period each
	// tick represents.
	double[] insolationArray; //Note this is an integrated value in Wh per metre squared
	double[] windSpeedArray;// instantaneous value
	double[] windDirectionArray; // Direction in degrees from North.  May not be needed as yet, but useful to have potentially
	double[] airTemperatureArray; // instantaneous value
	double[] airDensityArray; // instantaneous value
	double[] systemPriceSignalDataArray;
	int systemPriceSignalDataLength;
	
	int totalNbOfProsumers;
	int randomSeed;
	private int lengthOfDemandProfiles;
	
	public static boolean verbose = false;  // use to produce verbose output based on user choice (default is false)
	protected static boolean chartSnapshotOn = false;  // use
	public int ticksPerDay;
	protected int chartSnapshotInterval;
	
	public int signalMode=-1;
	
	private Network<?> socialNetwork;
	protected Network<?> economicNetwork;
	private Network<?> windNetwork;
	
	public GregorianCalendar simulationCalendar;
	
	SnapshotTaker snapshotTaker1;
	Collection<JComponent> chartCompCollection;
	ArrayList<SnapshotTaker> snapshotTakerArrList;
	public EmpiricalWalker drawOffGenerator;
	public EmpiricalWalker occupancyGenerator;
	public EmpiricalWalker vehicleArrivalGenerator;
	public Poisson journeyLengthGenerator;
	public Normal waterUsageGenerator;
	
	public Normal buildingLossRateGenerator;
	public Normal thermalMassGenerator;
	
	public Uniform coldAndWetApplTimeslotDelayRandDist;
	public EmpiricalWalker wetApplProbDistGenerator;
	
	public Binomial hhProsumerElasticityTest;
	

	/**
	 * This method return the social network 
	 * @return <tt>socialNetwork</tt> associated to the context
	 * @see #setSocialNetwork
	 */
	public Network<?> getSocialNetwork(){
		return this.socialNetwork;
	}
	
	/**
	 * Sets the social network associated to this context 
	 * @param n the social network
	 * @see #getSocialNetwork
	 */
	public void setSocialNetwork(Network<?> n){
		this.socialNetwork = n;
	}
	
	
	/**
	 * This method return the economic network 
	 * @return <tt>economicNetwork</tt> associated to the context
	 * @see #setEconomicNetwork
	 */
	public Network getEconomicNetwork(){
		return this.economicNetwork;
	}
	
	/**
	 * Sets the economic network associated to this context 
	 * @param n the economic network
	 * @see #getEconomicNetwork
	 */
	public void setEconomicNetwork(Network<?> n){
		this.economicNetwork = n;
	}
	
	
	/**
	 * This method returns the tick time. 
	 * It is a wrapper around RepastEssential.GgetTickCount method, which returns the tick count as integer.
	 * @return current tick count of the model 
	 */
	public int getTickCount() {
		return (int) RepastEssentials.GetTickCount();
	}
	
	public int getTimeslotOfDay() {
		return (int) RepastEssentials.GetTickCount() % ticksPerDay;
	}
	
	/**
	 * This method return the number of <tt> tickPerDay </tt>
	 * @return <code>tickPerDay</code>
	 */
	public int getNbOfTickPerDay() {
		return this.ticksPerDay;
	}
	
	public void setNbOfTickPerDay(int tick) {
		this.ticksPerDay = tick;
	   
		
	}
	
	/*public int getLengthOfDemandProfiles() {
		return this.lengthOfDemandProfiles;
	} */
	
	public void setChartSnapshotInterval(int interval) {
		this.chartSnapshotInterval = interval;
	}
	
	public int getChartSnapshotInterval() {
		return this.chartSnapshotInterval;
	}
	
	public void setTotalNbOfProsumers(int nbOfPros) {
		this.totalNbOfProsumers = nbOfPros;
	}
	
	public int getTotalNbOfProsumers() {
		return this.totalNbOfProsumers;
	}
	
	public void setRandomSeedValue(int rs) {
		this.randomSeed = rs;
	}
	
	public int getRandomSeedValue() {
		return this.randomSeed;
	}
	
	
	/**
	 * This method returns the elapse of time in number of days.
	 * It depends on how a day is initially defined. If a day is divided up to 48 timeslots, 
	 * then the second day starts at timeslot 49. 
	 * However, in order to have it usefully workable with arrays, the first day is returned as 0, second day as 1 and so forth.
	 * @return the elapsed time in terms of number of day, starting from 0
	 */
	public int getDayCount() {
		return (int) RepastEssentials.GetTickCount()/this.getNbOfTickPerDay();
	}
	
	/**
	 * This method determines whether a day has changed since a given reference point.
	 * @param sinceDay a day reference from which the elapse of day is tested.
	 *  @return <code>true</code> if the day has changed since <tt>sinceDay</tt>
     *          <code>false</code> otherwise
	 * see {@link #getDayCount()}
	 */
	public boolean isDayChangedSince(int sinceDay) {
		boolean dayChanged = false;
		int daysSoFar = getDayCount();
		int daysSinceStart = daysSoFar - sinceDay;
		if (daysSinceStart >= 1)
			dayChanged = true;
		return dayChanged;
	}
	
	/**
	 * This method determines whether a given timeslot is the beginning of the day
	 * It is built rather for readability than its functionality.
	 * @param timeslot a timeslot of the day to be tested whether it indicates the beginning of the day
	 * @return <code>true</code> if given timeslot corresponds to the beginning of the day, <code>false</code> otherwise
	 */
	public boolean isBeginningOfDay(int timeslot) {
		if (timeslot == 0)
			return true;
		else return false;	
	}
	
	/**
	 * This method determines whether a given timeslot is the end of the day
	 * It is built rather for readability than its functionality.
	 * @param timeslot a timeslot of the day to be tested whether it indicates the end of the day
	 * @return <code>true</code> if given timeslot corresponds to the end of the day, <code>false</code> otherwise
	 */
	public boolean isEndOfDay(int timeslot) {
		if (timeslot == this.ticksPerDay-1)
			return true;
		else return false;	
	}
	
	
	/**
	 * This method determines whether it is the beginning of the day
	 * @return <code>true</code> if it is the beginning of the day, <code>false</code> otherwise
	 */
	public boolean isBeginningOfDay() {
		double time = RepastEssentials.GetTickCount();
		int timeOfDay = (int) (time % getNbOfTickPerDay());
		if (timeOfDay == 0)
			return true;
		else return false;	
	
	}
	/*
	 * Accesor methods to context variables
	 */
	/**
	 * @param time - the time in ticks for which to get the insolation
	 * @return the insolation at the time (in ticks) passed in
	 */
	public double getInsolation(int time)
	{
		return insolationArray[time % weatherDataLength];
	}

	/**
	 * @param time - the time in ticks for which to get the wind speed
	 * @return the wind speed at the time (in ticks) passed in
	 */
	public double getWindSpeed(int time)
	{
		return windSpeedArray[time % weatherDataLength];
	}
	
	/**
	 * @param time - the time in ticks for which to get the air temperature
	 * @return the air temperature at the time (in ticks) passed in
	 */
	public double getAirTemperature(int time)
	{
		return airTemperatureArray[time % weatherDataLength];
	}
	
	/**
	 * @param time - the time in ticks for which to get the air density
	 * @return the air density at the time (in ticks) passed in
	 */
	public double getAirDensity(int time)
	{
		return airDensityArray[time % weatherDataLength];
	}
	
	/**
	 * @param time - the time in ticks for which to get the insolation
	 * @return the insolation at the time (in ticks) passed in
	 */
	public double[] getInsolation(int time, int length)
	{
		int start = time % weatherDataLength;
		return Arrays.copyOfRange(insolationArray, start, start + length);
	
	}

	/**
	 * @param time - the time in ticks for which to get the wind speed
	 * @return the wind speed at the time (in ticks) passed in
	 */
	public double[] getWindSpeed(int time, int length)
	{
		int start = time % weatherDataLength;
		return Arrays.copyOfRange(windSpeedArray, start, start + length);
	
	}
	
	/**
	 * @param time - the time in ticks for which to get the air temperature
	 * @return the air temperature at the time (in ticks) passed in
	 */
	public double[] getAirTemperature(int time, int length)
	{
		int start = time % weatherDataLength;
		return Arrays.copyOfRange(airTemperatureArray, start, start + length);
	}
	
	/**
	 * @param time - the time in ticks for which to get the air density
	 * @return the air density at the time (in ticks) passed in
	 */
	public double[] getAirDensity(int time, int length)
	{
		int start = time % weatherDataLength;
		return Arrays.copyOfRange(airDensityArray,start, start+length);
	}
	
	/**
	 * @return the weatherDataLength
	 */
	public int getWeatherDataLength() {
		return weatherDataLength;
	}

	/**
	 * @param weatherDataLength the weatherDataLength to set
	 */
	public void setWeatherDataLength(int weatherDataLength) {
		this.weatherDataLength = weatherDataLength;
	}

	/**
	 * @return the insolation
	 */
	public double[] getInsolation() {
		return insolationArray;
	}

	/**
	 * @param insolation the insolation to set
	 */
	public void setInsolation(double[] insolation) {
		this.insolationArray = insolation;
	}

	/**
	 * @return the windSpeed
	 */
	public double[] getWindSpeed() {
		return windSpeedArray;
	}

	/**
	 * @param windSpeed the windSpeed to set
	 */
	public void setWindSpeed(double[] windSpeed) {
		this.windSpeedArray = windSpeed;
	}

	/**
	 * @return the windDirection
	 */
	public double[] getWindDirection() {
		return windDirectionArray;
	}

	/**
	 * @param windDirection the windDirection to set
	 */
	public void setWindDirection(double[] windDirection) {
		this.windDirectionArray = windDirection;
	}

	/**
	 * @return the airTemperature
	 */
	public double[] getAirTemperature() {
		return airTemperatureArray;
	}

	/**
	 * @param airTemperature the airTemperature to set
	 */
	public void setAirTemperature(double[] airTemperature) {
		this.airTemperatureArray = airTemperature;
	}

	/**
	 * @return the systemPriceSignalDataLength
	 */
	public int getSystemPriceSignalDataLength() {
		return systemPriceSignalDataLength;
	}

	/**
	 * @param systemPriceSignalDataLength the systemPriceSignalDataLength to set
	 */
	public void setSystemPriceSignalDataLength(int systemPriceSignalDataLength) {
		this.systemPriceSignalDataLength = systemPriceSignalDataLength;
	}

	/**
	 * @return the systemPriceSignalData
	 */
	public double[] getSystemPriceSignalData() {
		return systemPriceSignalDataArray;
	}

	/**
	 * @param systemPriceSignalData the systemPriceSignalData to set
	 */
	public void setSystemPriceSignalData(double[] systemPriceSignalData) {
		this.systemPriceSignalDataArray = systemPriceSignalData;
	}

	/*
	 * Have a nice toString() method to give good
	 * debug info
	 */
	public String toString() {
		String description;
		StringBuilder myDesc = new StringBuilder();
		myDesc.append("Instance of Cascade Context, hashcode = ");
		myDesc.append(this.hashCode());
		myDesc.append("\n contains arrays:");
		myDesc.append("\n insolation of length " + insolationArray.length);
		myDesc.append("\n windSpeed of length " + windSpeedArray.length);
		myDesc.append("\n airTemp of length " + airTemperatureArray.length);
		myDesc.append("\n and baseDemand of length " + systemPriceSignalDataArray.length);
		description = myDesc.toString();		
		return description;
	}
	
	private String getFileNameForChart(int chartNb) {
		String chartName; 

		switch (chartNb) {
		 case 0:  
			 if (Consts.TAKE_SNAPSHOT_OF_CHART_0_Insol)
				 chartName = "chart0_Insol_r"+getTickCount()+Consts.FILE_CHART_FORMAT_EXT;  
			 else chartName="";
			 break;
		 case 1:  
			 if (Consts.TAKE_SNAPSHOT_OF_CHART_1_AirTemp)
				 chartName = "chart1_AirTemp_r"+getTickCount()+Consts.FILE_CHART_FORMAT_EXT; 
			 else chartName="";
			 break;
		 case 2:  
			 if (Consts.TAKE_SNAPSHOT_OF_CHART_2_WindSpeed)
				 chartName = "chart2_WindSpeed_r"+getTickCount()+Consts.FILE_CHART_FORMAT_EXT;   
			 else chartName="";
			 break;
		 case 3:  
			 if (Consts.TAKE_SNAPSHOT_OF_CHART_3_AggSumOfD)
			 chartName = "chart3_AggSumOfD_r"+getTickCount()+Consts.FILE_CHART_FORMAT_EXT;
			 else chartName="";
			 break;
		 case 4:  chartName = "chart4_SvsC_r"+getTickCount()+Consts.FILE_CHART_FORMAT_EXT;   break;
		 case 5:  
			 if (Consts.TAKE_SNAPSHOT_OF_CHART_5_SmartAdapt)
				 chartName = "chart5_SmartAdapt_r"+getTickCount()+Consts.FILE_CHART_FORMAT_EXT;
			 else chartName="";
			 break;
		 case 6:  chartName = "chart6_AggCost_r"+getTickCount()+Consts.FILE_CHART_FORMAT_EXT;   break;
		 case 7:  chartName = "chart7_BvsD_r"+getTickCount()+Consts.FILE_CHART_FORMAT_EXT;   break;
		 case 8:  
			 if (Consts.TAKE_SNAPSHOT_OF_CHART_8_Market)
				 chartName = "chart8_Market_r"+getTickCount()+Consts.FILE_CHART_FORMAT_EXT;   
			 else chartName="";
			 break;
		 case 9:  chartName = "chart9_r"+getTickCount()+Consts.FILE_CHART_FORMAT_EXT;   break;
		 case 10:  chartName = "chart10_r"+getTickCount()+Consts.FILE_CHART_FORMAT_EXT;   break;


		 default: chartName = "chartDefaultName_"+Consts.FILE_CHART_FORMAT_EXT;; break;
		}

		return chartName;	
	}

	public void takeSnapshot() {

		if (this.getDayCount()> Consts.AGGREGATOR_PROFILE_BUILDING_PERIODE - 2) {

			try {
				for (int i=0; i<snapshotTakerArrList.size();i++) {
					SnapshotTaker snapshotTaker = snapshotTakerArrList.get(i);
					String fileName = getFileNameForChart(i);
					if (fileName != "") {
						//if (Consts.DEBUG) System.out.println("takeSnapshot: fileName is empty");
						File file = new File(fileName);
						snapshotTaker.save(file, "png");
					}
				}

			} catch (IOException e) {
				// Print out the exception that occurred
				 System.out.println("CascadeContext: Unable to takeSnapshot "+e.getMessage());
			}
		}

	}

	public void setChartCompCollection(Collection<JComponent> c) {
		chartCompCollection=c;
		if (chartSnapshotOn) {
			buildChartSnapshotTakers(c);
			buildChartSnapshotSchedule();
		}
	}
	
	public Collection<JComponent> getChartCompCollection() {
		return chartCompCollection;
	}
	
	private void buildChartSnapshotTakers (Collection<JComponent> chartCompCollection) {
		Iterator<JComponent> compIter= chartCompCollection.iterator();
		snapshotTakerArrList = new ArrayList<SnapshotTaker>();
		while ( compIter.hasNext() ){
			ChartPanel chartComp = (ChartPanel) compIter.next();
			//if (Consts.DEBUG) System.out.println(chartComp.getChart().getTitle().getText());
			SnapshotTaker snapshotTaker = new SnapshotTaker(chartComp);
			snapshotTakerArrList.add(snapshotTaker);
		}
	}
	
	private void buildChartSnapshotSchedule() {

		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		//ScheduleParameters params = ScheduleParameters.createOneTime(1);
		//if (Consts.DEBUG) System.out.println("chartCompCol: null?: "+getChartCompCollection());
		//if ((chartSnapshotOn) && (getChartCompCollection() != null)){
		ScheduleParameters params = ScheduleParameters.createRepeating(0, getChartSnapshotInterval(),ScheduleParameters.LAST_PRIORITY);
		schedule.schedule(params, this, "takeSnapshot"); 

	}
	
	
	
	/******************
	 * This method steps the model's internal gregorian calendar on each model tick
	 *  
	 * Input variables: none
	 * 
	 ******************/
	@ScheduledMethod(start = 0, interval = 1, shuffle = true, priority = ScheduleParameters.FIRST_PRIORITY)
	public void calendarStep() {
		//if (Consts.DEBUG) System.out.println("calendarStep()");
		simulationCalendar.add(GregorianCalendar.MINUTE, Consts.MINUTES_PER_DAY / ticksPerDay);		
	}
	
	public Date getDateTime() {
		return simulationCalendar.getTime();
	}

	/**
     * Constructs the cascade context 
     * 
     */
	public CascadeContext(Context context)
	{
		super(context.getId(), context.getTypeID());
		if (verbose)
			System.out.println("CascadeContext created with context " + context.getId() + " and type " + context.getTypeID());

		Iterator<Projection<?>> projIterator = context.getProjections().iterator();

		while (projIterator.hasNext()) {
			Projection<?> proj = projIterator.next();
			this.addProjection(proj);
			if (verbose)
				System.out.println("CascadeContext: Added projection: "+ proj.getName());
		}

		this.setId(context.getId());
		this.setTypeID(context.getTypeID());
		

	}
	
	//----------------
	
	private Network networkOfRegisteredPxTraders;
	private Network networkOfRegisteredBMTraders;
	private int gasHeatedPercentage;

	
	public boolean isFirstDay() {
		if (getDayCount() == 0)
			return true;
		else return false;
	}
	
	public boolean isMarketFirstDay() {		
		if ((getDayCount() - ((Consts.AGGREGATOR_PROFILE_BUILDING_SP + Consts.AGGREGATOR_TRAINING_SP)/48)) == 0)
			return true;
		else return false;
	}
	
	public boolean isSecondDay() {
		if (getDayCount() == 1)
			return true;
		else return false;
	}
	
	public boolean isMarketSecondDay() {
		if (getDayCount() == ((Consts.AGGREGATOR_PROFILE_BUILDING_SP + Consts.AGGREGATOR_TRAINING_SP)/48)+1 )
			return true;
		else return false;
	}
	
	public int getSettlementPeriod(){
		return getTimeslotOfDay();
	}
	
	/*public Network getNetworkOfRegisteredPxTraders(){
		return this.networkOfRegisteredPxTraders;
	}
	
	public Network getNetworkOfRegisteredBMTraders(){
		return this.networkOfRegisteredBMTraders;
	}
	
	public ArrayList<IPxTrader> getListOfRegisteredPxTraders() {
		ArrayList<IPxTrader> aListOfPxTranders = new ArrayList<IPxTrader>();

		Network pxTradersNet = getNetworkOfRegisteredPxTraders();
		Iterable<RepastEdge> edgeIter = pxTradersNet.getEdges();
		if(verbose) 
			System.out.println("There are "+ pxTradersNet.size() + " registered PxTraders");
		for (RepastEdge edge : edgeIter) {
			Object obj = edge.getTarget();
			if (obj instanceof IPxTrader)
				aListOfPxTranders.add((IPxTrader) obj);    		
			else
				System.err.println(this.getClass()+"::Wrong Class Type: IPxTrader agent is expected");
		}
		return aListOfPxTranders;
	} 
	
	
	public ArrayList<IBMTrader> getListOfRegisteredBMTraders() {
		ArrayList<IBMTrader> aListOfBMTranders = new ArrayList<IBMTrader>();

		Network bmTradersNet = getNetworkOfRegisteredBMTraders();
		Iterable<RepastEdge> edgeIter = bmTradersNet.getEdges();
		if(verbose) 
			System.out.println("There are "+ bmTradersNet.size() + " registered BMTraders");
		for (RepastEdge edge : edgeIter) {
			Object obj = edge.getTarget();
			if (obj instanceof IBMTrader)
				aListOfBMTranders.add((IBMTrader) obj);    		
			else
				System.err.println(this.getClass()+"::Wrong Class Type: IBMTrader agent is expected");
		}
		return aListOfBMTranders;
	}	
	
	*/
	
	public ArrayList<IPxTrader> getListOfPxTraders() {
		ArrayList<IPxTrader> aListOfPxTraders = new ArrayList<IPxTrader>();
		Iterable<IPxTrader> pxTraderIter = (Iterable<IPxTrader>) (this.getObjects(IPxTrader.class));
		for (IPxTrader pxTrader: pxTraderIter) 
			aListOfPxTraders.add(pxTrader);    	

		return aListOfPxTraders;	
	}
	
	public ArrayList<IBMTrader> getListOfBMTraders() {
		ArrayList<IBMTrader> aListOfBMTraders = new ArrayList<IBMTrader>();
		Iterable<IBMTrader> bmTraderIter = (Iterable<IBMTrader>) (this.getObjects(IBMTrader.class));
		for (IBMTrader bmTrader: bmTraderIter) 
			aListOfBMTraders.add(bmTrader);    	

		return aListOfBMTraders;	
	} 
	
	public ArrayList<ITrader> getListOfTraders() {
		ArrayList<ITrader> aListOfTraders = new ArrayList<ITrader>();
		Iterable<ITrader> traderIter = (Iterable<ITrader>) (this.getObjects(ITrader.class));
		for (ITrader trader: traderIter) 
			aListOfTraders.add(trader);    	

		return aListOfTraders;	
	}

	/**
	 * @param percentageOfHHProsWithGas
	 */
	public void setGasPercentage(int percentageOfHHProsWithGas)
	{
		this.gasHeatedPercentage = percentageOfHHProsWithGas;
		
	}
	
	public int getGasPercentage()
	{
		return this.gasHeatedPercentage;
		
	}
	
	
	
}
