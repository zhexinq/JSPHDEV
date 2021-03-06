/**
 * @author zhexinq
 * abtract class to contain all implementations of any methods
 * declared in the interface
 */

package adapter;

import model.Automobile;
import util.FileIO;
import util.Properties;
import java.io.*;

import java.util.LinkedHashMap;

import database.DBHandler;

import java.util.ArrayList;

import exception.WrongInputException;
import scale.*;

public abstract class ProxyAutomobile {
	// using "make+model as the unique identifier"
	private static LinkedHashMap<String, Automobile> autos = new LinkedHashMap<String, Automobile>(); // (modelName, auto) pair
	private Integer threadId = 0;
	private DBHandler dbHandle = new DBHandler(); // for syncing LHM with database
	
	/* in CreateAuto API */
	/* build the auto model object */
	public boolean buildAuto(String filename) {
		FileIO io = new FileIO();
		Automobile a1;
			
		/* handle error when filename is wrong */
		try {
			a1 = io.buildAutoObject(filename);
			/* put the built model to collection */
			if (a1 != null)  {
				System.out.println("Built a new vehicle:");
				a1.printInfo(); 
				autos.put(a1.getUniqueName(), a1);
				dbHandle.createAutoInDB(a1); // sync in DB
				return true;
			}
		}
		catch (WrongInputException e) {
			e.recordLog("log.txt", e);
			e.printInfo();
			filename = fix(e.getErrno());
		}
		return false;
	}
	/* print the auto model information */
	public void printAuto(String modelName) {
		if (autos.containsKey(modelName))
			autos.get(modelName).printInfo();
		else {
			System.out.println("The model doesn't exist");
		}
	}
	
	/* update the OptionSet name of a specified model */
	public void updateOptionSetName(String modelname, String setName, String newName) {
		if (autos.containsKey(modelname)) {
			Automobile a1 = autos.get(modelname);
			a1.updatOptionSetName(setName, newName);
			dbHandle.updateAutoOptionSetNameInDB(a1.getUniqueName(), setName, newName); // sync in DB
		}
		else
			System.out.println("model does not exist.");
	}
	/* update the price of an Option of specified model and OptionSet */
	public void updateOptionPrice(String modelname, String setName, String optionName, int newprice) {
		if (autos.containsKey(modelname)) {
			Automobile a1 = autos.get(modelname);
			a1.updateOptionPrice(setName, optionName, newprice);
			dbHandle.updateAutoOptionPriceInDB(a1.getUniqueName(), setName, optionName, newprice); // sync in DB
		}
		else
			System.out.println("model deose not exist.");
	}
	/* make a choice in an OptionSet of an Auto */
	public void makeOptionChoice(String modelname, String setName, String optName) {
		Automobile a1 = autos.get(modelname);
		a1.setOptionChoice(setName, optName);
		synchronized (a1) {
			
		}
	}
	
	/* delete operations on Auto object */
	public void deleteAuto(String modelName) {
		if (autos.containsKey(modelName)) {
			autos.remove(modelName);
			dbHandle.deleteAutoInDB(modelName); // sync in DB
		}
	}
	
	/* synchronized editing */
	public void editSetNameSync(String model, String setName, String newName) {
		Automobile a1 = autos.get(model);
		StringBuilder threadName = new StringBuilder(model).append(" ").append(threadId);
		threadId++;
		EditSetName editSetName = new EditSetName(threadName.toString(), a1, setName, newName);
		editSetName.start();
	}
	public void editOptionNameSync(String model, String setName, String optName, String newName) {
		Automobile a1 = autos.get(model);
		StringBuilder threadName = new StringBuilder(model).append(" ").append(threadId);
		threadId++;
		EditOptionName editOptionName = new EditOptionName(threadName.toString(), a1, setName, optName, newName);
		editOptionName.start();
	}
	
	/* load a props file into Auto and add to LHM autos */
	// CarModel->CarMake->BasePrice->OptionSet1->OptionSet1Option1->...
	// only used in the server side
	// return true if props loaded successfully, false otherwise
	public boolean loadPropsToAuto(Properties p) {
		String carModel;  
		String carMake;  
		int basePrice;  
		Automobile a1;
		
		try {
			/* parsing basic info */
			carModel = p.getProperties("CarModel");
			carMake = p.getProperties("CarMake");
			basePrice = Integer.parseInt(p.getProperties("BasePrice"));
			a1 = new Automobile(carMake, carModel, basePrice);
			
			/* parsing the sets info and options */	
			String optSetQuery, optQuery;
			String setName, optName;
			int optPrice;
		
			int i = 1;
			while (true) {
				ArrayList<String> setOptions = new ArrayList<String>();
				ArrayList<Integer> setPrices = new ArrayList<Integer>();
				StringBuilder queryStrBase = new StringBuilder("OptionSet");
				optSetQuery = queryStrBase.append(i).toString();
				setName = p.getProperties(optSetQuery);
				if (setName == null)
					break;
//				System.out.println("set name: " + setName);
				queryStrBase.append("Option");
				int j = 1;
				while (true) {
					StringBuilder optQueryStrBase = new StringBuilder(queryStrBase);
					optQuery = optQueryStrBase.append(j).toString();
					String optnameValue = p.getProperties(optQuery);
					if (optnameValue == null)
						break;
//					System.out.println(optnameValue);
					String[] nameValuePair = optnameValue.split(",");
					optName = nameValuePair[0];
					optPrice = Integer.parseInt(nameValuePair[1]);
//					System.out.println("opt name: " + optName);
//					System.out.println("opt price: " + optPrice);
					setOptions.add(optName);
					setPrices.add(optPrice);
					j++;
				}
				a1.updateNewOptionSet(setName, setOptions, setPrices);
				i++;
			}
			System.out.println("Built a new vehicle:");
			a1.printInfo();
			/* add the built auto to LHM */
			autos.put(a1.getUniqueName(), a1);
			dbHandle.createAutoInDB(a1); // sync in DB
		} catch (Exception e) {
			System.out.println("Cannot load the props to Auto");
			return false;
		}
		return true;
	}
	
	/* provide a list of available models */
	public ArrayList<String> getAvailableModels() {
		ArrayList<String> models = new ArrayList<String>();
		for (String model:autos.keySet()) {
			models.add(model);
		}
		return models;
	}
	
	/* get a selected car model */
	public Automobile getSelectedModel(String modelName) {
		return autos.get(modelName);
	}
	
	/* fix error */
	public String fix(int errno) {
		return null;
	}
	
	/* unit test */
	public static void main(String[] args) {
		BuildAuto autoFactory = new BuildAuto();
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream("props.dat"));
			Properties props = (Properties)in.readObject(); 
			autoFactory.loadPropsToAuto(props);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
