/*
 * 180008512
 */

import java.util.ArrayList;
import java.util.HashMap;

public class MAC {
	
	private BinaryCSPReader reader = new BinaryCSPReader();
	private BinaryCSP binaryCSP;
	private ArrayList<BinaryConstraint> constraints;
	
	private ArrayList<Integer> unassigned = new ArrayList(); //List of unassigned variables (varList).
	private ArrayList<Integer> assigned = new ArrayList<>(); //List of assigned variables.
	private HashMap<Integer, ArrayList<Integer>> varDomains = new HashMap<>(); //Variables domain.
	
	private int macIteration = 0; //Count the number of MAC procedures.
	private boolean solutionFound = false; //Checks if a solution was found (Used to stop the algorithm).
	
	private long start; //Used to measure the time taken to find a solution.
	
	public MAC(String instance) {
		binaryCSP = reader.readBinaryCSP(instance);
		constraints = binaryCSP.getConstraints();
		setVars();
		
		System.out.println("\nPerforming MAC3 on instance: " + instance);
		start = System.currentTimeMillis();
		MAC3(unassigned);
	}
	
	
	/**
	 * Method to populate the unassigned list with all the variables and varDomains Hash Map
	 * with the corresponding domains of each variable.
	 */
	private void setVars() {
		for(int i = 0; i < binaryCSP.getNoVariables(); i++) {
			int variable = i;
			ArrayList<Integer> domain = new ArrayList<>();
			for(int j = binaryCSP.getLB(i); j < binaryCSP.getUB(i)+1; j++) {
				domain.add(j);
			}
			unassigned.add(i);
			varDomains.put(variable, domain);
		}
	}
	
	
	//////////////////////////////////////////////
	//			 MAC3 MAIN PROCEDURE			//
	//////////////////////////////////////////////				
	private void MAC3(ArrayList<Integer> varList) {
		
		macIteration++;
		
		int var = selectVar(varList);	//Select a variable.
		int val = selectVal(var);	//Select a value from the variable's domain.		
		
		
		HashMap<Integer, ArrayList<Integer>> prevDomains = (HashMap<Integer, ArrayList<Integer>>) varDomains.clone(); //Saving domains to undo the pruning.

		assign(var, val);	
		
		if(assigned.size() == binaryCSP.getNoVariables() && !solutionFound) { //Print the first solution found.
			printSolution();
			solutionFound = true;
			System.out.println("\n Solution found after " + macIteration + " nodes");
			long finish = System.currentTimeMillis();
			long timeElapsed = finish - start;
			System.out.println(" Solution found after " + timeElapsed + " ms");
		}
		else if(AC3(var) && !solutionFound) { //If a solution was not found, proceed.
			varList = getVarList();
			MAC3(varList);
		}
		
		varDomains = prevDomains; //Undoing pruning.
		unassign(var, val); 
		
		deleteValue(var, val);
		prevDomains = (HashMap<Integer, ArrayList<Integer>>) varDomains.clone(); 	//Saving domains to undo the pruning.

		if(!varDomains.get(var).isEmpty()) {
			if(AC3(var))
				MAC3(varList);
			else
				varDomains = prevDomains; //Undoing pruning.
		}

		restoreValue(var, val);		
	}
	
	
	/**
	 * AC3 MAIN PROCEDURE
	 * @param var
	 * @return true if consistent, otherwise return false.
	 */
	private boolean AC3(int var) {
		
		ArrayList<int[]> queue = new ArrayList<>(); //Initialises Queue.
		
		for(BinaryConstraint c : constraints) { //Inputs arcs into the queue.
			if(c.getFirstVar() == var && !assigned.contains(c.getSecondVar())){
				int[] arc = new int[2];
				arc[0] = var;
				arc[1] = c.getSecondVar();
				queue.add(arc);
			}
		}
		
		while(!queue.isEmpty()) { //While queue is not empty.
			int[] arc = queue.get(0); //Select first arc of the queue.
			queue.remove(0); //Removes arc from the queue.
			try {
				if(revise(arc[0], arc[1])) { 
						for(BinaryConstraint c : constraints) {  //Inputs new arcs into the queue.
							if(c.getFirstVar() == arc[1]) {
								int[] newArc = {arc[1], c.getSecondVar()};
								queue.add(newArc);
							}
						}
				}
			}
			catch(Exception e) { //If pruned domain is empty.
				return false;
			}
		}
		
		return true;
	}
	
	
	/**
	 * Prunes domain of arc(var, futureVar)
	 * @param var
	 * @param futureVar
	 * @return false if there where no changes, otherwise return true.
	 * @throws Exception when the pruned domain is empty.
	 */
	private boolean revise(int var, int futureVar) throws Exception{
		
		boolean changed = false;
		
		ArrayList<Integer> acceptableValues = getAcceptableValues(var, futureVar); //Values from supported tuples for arc(var, futureVar).
		ArrayList<Integer> newDomain = (ArrayList<Integer>) varDomains.get(futureVar).clone(); //Get domain.
		
		for(int i = 0; i < varDomains.get(futureVar).size(); i++) {
			if(!acceptableValues.contains(varDomains.get(futureVar).get(i))) { //If domain contains an unsupported value.
				newDomain.remove(newDomain.indexOf(varDomains.get(futureVar).get(i))); //Prune unsupported value.
				changed = true; //There was a change, so chnage is set to true;
			}
		}
		
		varDomains.put(futureVar, newDomain); //Replace the domain with the new pruned domain.
		
		if(newDomain.isEmpty()) //If the new pruned domain is empty.
			throw new Exception();
			
		return changed;
	}

	
	/**
	 * Method to get the values that where supported by the tuples of c(var, futureVar).
	 * @param var
	 * @param futureVar
	 * @return an ArrayList with the supported values.
	 */
	private ArrayList<Integer> getAcceptableValues(int var, int futureVar) {
		
		ArrayList<Integer> acceptableValues = new ArrayList<>();
		
		for(BinaryConstraint c : constraints) {
			if(c.getFirstVar() == var && c.getSecondVar() == futureVar) {
				for(BinaryTuple bt : c.getTuples()) {
					for(int val : varDomains.get(var)) {
						if(bt.getVal1() == val) {
							acceptableValues.add(bt.getVal2());
						}
					}
				}
			}
		}
		return acceptableValues;
	}
	
	
	/**
	 * Method to get a varList with the variables that have not been assigned yet.
	 * @return populated ArrayList varList
	 */
	private ArrayList<Integer> getVarList() {
		ArrayList<Integer> varList = new ArrayList<>();
		for(int i = 0; i < binaryCSP.getNoVariables(); i++) {
			if(!assigned.contains(i)) { //If variable i has not been assigned yet.
				varList.add(i); //Add the variable to the list.
			}
		}
		return varList;
	}	
	
	
	/**
	 * Unassigns a variable by removing the variable of the assigned list.
	 * @param var
	 * @param val
	 */
	private void unassign(int var, int val) {
		assigned.remove(assigned.indexOf(var));
	}
	
	
	/**
	 * Assigns a variable by adding it to the assigned list, and prunes all 
	 * of the values from the domain apart from the assigned value (Domain will only contain val).
	 * @param var
	 * @param val
	 */
	private void assign(int var, int val) {
		assigned.add(var);
		ArrayList<Integer> domain = new ArrayList<>();
		domain.add(val);		
		varDomains.put(var, domain);
	}
	
	
	/**
	 * Re-introduces the value in the domain of the specified variable. 
	 * @param var 
	 * @param val 
	 */
	private void restoreValue(int var, int val) {
		ArrayList<Integer> domain = varDomains.get(var);
		domain.add(val);
		varDomains.put(var, domain);
	}
	
	
	/**
	 * Deletes a value from the domain of the specified variable.
	 * @param var
	 * @param val
	 */
	private void deleteValue(int var, int val) {
		ArrayList<Integer> domain = varDomains.get(var);
		domain.remove(domain.indexOf(val));
		varDomains.put(var, domain);
	}
	
	
	/**
	 * Selects the first value from the domain of the specified variable, after the domain
	 * is ordered in ascending order.
	 * @param var
	 * @return val
	 */
	private int selectVal(int var) {
		ArrayList<Integer> domain = varDomains.get(var);
		domain.sort(null); //Sorts domain in ascending order.
		return domain.get(0);
	}
	
	
	/**
	 * Selects a variable from the varList. At the beginning, return the first variable. 
	 * Else, return the variable with the smallest domain.
	 * @param varList
	 * @return variable
	 */
	private int selectVar(ArrayList<Integer> varList) {
		int variable = varList.get(0);
		int smallestDomain = varDomains.get(variable).size();
		if(varList.size() == binaryCSP.getNoVariables()) //If at the beginning
			return variable;
		else {
			for(int i = 0; i < varList.size(); i++) {
				if(varDomains.get(varList.get(i)).size() < smallestDomain) { 
					variable = varList.get(i); //Keeps track of the variable with the smallest domain.
					smallestDomain = varDomains.get(varList.get(i)).size(); //Keeps track of the size of the smallest domain encountered.
				}
			}
		}
		return variable;
	}
	
	
	/**
	 * Prints the solution.
	 */
	private void printSolution() {
		System.out.println("\n Solution: ");
		for(int i = 0; i < varDomains.size(); i++) {
			System.out.println(" Variable " + (i+1) + ":  " + varDomains.get(i).toString());
		}		
	}	
}
	










































