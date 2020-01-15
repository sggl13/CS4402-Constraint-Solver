# CS4402 Constraint Solver Implementation

## 1. Forward Checking

For Forward Checking, the data structures utilised where:
•	An ArrayList of Integers, named unassigned. This list will be populated with all the variables at the beginning, and it will be the initial list that will be passed to the forwardChecking(ArrayList<Integer> varList) method.	

•	An ArrayList of Integers, named assigned. This list is used to keep track of the variables that have been assigned.	

•	A HashMap, named varDomains. This HashMap is used to store the domains of the variables. It uses the variable number as the key for retrieving the domain of that variable. Moreover, All the values for a domain are stored within an ArrayList of Integers.

The FC class also makes use of 15 different methods, which will be described in the following section.

### 1.1 Methods Description

The methods that the FC class makes use of are:
•	setVars ─ This method is only used to populate the unassigned ArrayList with all the variables of the instance, and the varDomains HashMap with all of the domains for each variable, before initialising the Forward Checking algorithm.	

•	forwardChecking ─ This is the main method for the Forward Checking procedure, and takes an ArrayList of Integers as a parameter, which at the beginning it’s the unassiged ArrayList mentioned before. Firstly, it checks if the algorithm has reached a solution, in which case it will print the solution and exit. Then it will proceed to select a variable from the varList, and a value from the variable’s domain using the methods selectVar and selectVal respectively. Finally, it calls the branchFCLeft and branchFCRight methods respectively.	

•	branchFCLeft ─ This method also takes in the varList as a parameter, as well as the variable and value selected in the forwardCheck method. Firstly, it makes a copy of all of the current domains, which will be used for undoing the pruning. It will then proceed to assign the variable and value using the assign method. Then It will proceed to check the consistency of future arcs, using the reviseFutureArcs method. In the case it is consistent, it will re-create the varList using getVarList and do a recursive call by calling forwardChecking and passing the new varList generated. In the case future arcs are not consistent, it will undo the pruning by restoring the variables domain using the copy made at the beginning of the method, and the variable and value are unassigned using the unassign method.

•	branchFCRight ─ This method takes the same parameters as brancgFCLeft. It starts by deleting the passed value from the domain of the passed variable using the deleteValue method. Secondly, it checks if the variable’s domain is empty after the previous action. If it is not empty, it will make a copy of the domains to be able to undo the pruning, and revises the future arcs. If future arcs are consistent, it proceeds to make a recursive call to forwardChecking, but in this case passing the same varList that was passed to this method. Otherwise, the pruning is undone using the copy of the domains, and the value is restored within the variable’s domain using the restoreValue method.	

•	reviseFutureArcs ─ This method takes the varList and variable as parameters. Firstly, It traverses through the varList, and for each element in the list, if it is not the variable that was passed, it proceeds to call the method revise for the arc. If there is any inconsistent arc, it will return false, otherwise it will return true.	

•	revise ─ This method takes two variables, the current variable and the future variable. It then takes all the supported values for that arc using the getAcceptableValues method, and prunes the future variable’s domain. The pruning is performed by removing any value from the future variable domain that it is not a supported value of that arc. If by the end, the domain that is being pruned is empty, it returns false as it is not consistent; otherwise it returns true.	

•	getAcceptableValues ─ The methods takes the two variables of the arc, and goes through the tuples and domains to produce a list containing the values that are supported, which is then returned and used by the revise method for the pruning.

•	getVarList ─ This is the method used to re-create the varList and will return a varList with only the variables that have not been assigned yet.	

•	assign ─ This is the method used for assigning a variable. To do so, it inputs the variable into the assigned ArrayList, and removes every value from its domain, apart from the value that was assigned to it.	

•	unassign ─ This is the method used for unassigning a variable, by removing it from the assigned ArrayList. It does not undo the pruning as this is already done when undoing the pruning in the branchFCLeft and branchFCLeft methods.	

•	restoreValue ─ This method takes a variable and value, and re-introduces the value into the variable’s domain.

•	deleteValue ─ This method does the contrary of restoreValue. Instead of interesting back the value into the domain, it deletes it.	

•	selectVal ─ This is the method used to select a value from a variable’s domain. To do so, it takes the domain of the variable, and sorts it into ascending order, and then proceeds to return the first element of the ordered domain.	

•	selectVar ─ This is the method used to select a variable. If it is at the beginning, it will always return the first variable of the list. If it is not at the beginning, it will return the variable that has the smallest domain within the varList.	

•	printSolution ─ This method is only used to output the solution.

## 2. Maintaining Arc Consistency

MAC uses the same data structures as Forward Checking, with only one exception. MAC, specifically the AC3 method, makes use of an ArrayList of Integer arrays named queue, which are used to input the arcs that are going to be revised by that same method. This ArrayList will be treated as a queue. Hence, it will always access and remove the first element of the list and add at the end of the list.

### 2.1 Methods Description

As MAC makes use of many of the methods that have been described on the above section, it will only be described those methods that are only used by MAC, or the methods that have been changed for this algorithm.

•	MAC3 ─ This is the main MAC method. It starts by selecting a variable and value using selectVar and selectVal respectively. It then proceeds to make a copy of the domains to undo the pruning. It assigns the veriable using assign, and checks if there is a solution. If there is a solution, print the solution and terminate. Otherwise, continue. It then calls AC3, if it is consistent, it recreates the varList and do a recursive call passing the new varList to MAC3. Otherwise, the pruning is undone using the copy of the domains from before, and unassigns the variable using unassign. It will then proceed with the “Right” branching, which follows the same pattern as the method branchFCRight, but calling AC3 instead of reviseFutureArcs.	

•	AC3 ─ Firstly, it initialises and empty queue, and inserts the corresponding arcs. It then enters a while loop that will not terminate until the queue is empty, or an exception is caught. Within the while loop, the first arc of the queue is selected and removed, and it is passed to revise. If there was any change in revise, new arcs are added to the queue. If revise empties a domain, it throws an exception that is caught within the loop and returns false to finalise. Otherwise it proceeds, and by the end of the process if a problem was not encountered, true is returned.

•	Revise ─ This method has the same layout as the Forward Check revise. The difference is that it returns false if there has not been any change in the domain, true if there has been a change, and it throws and exception if a domain is emptied.

## 3. Empirical Evaluation

For this part, the time taken from the start of an algorithm until a solution was found is recorded, as well as the number of nodes visited through the process. Unfortunately, the Forward Checking algorithm implemented doesn’t seem to create a solution for the sudoku instances, but it does work with the Queens instances and langfords2_3/4 instances.

Therefore, the evaluation of the algorithms will be based on those instances the Forward Checking algorithm does produce a solution for. 

Although Forward Checking is more efficient than other algorithms, thanks to the combination of constraint propagation and search, by implementing AC, makes MAC more efficient than Forward Checking. For instance, taking 4Queens and langfords2_3 as an example; in the case of 4Queens, Forward Checking managed to find a solution after 9 nodes and 1 millisecond and MAC after 5 nodes and 0 millisecond; in the case of langfords2_3, Forward Checking found a solution after 16 nodes and 2 milliseconds, while MAC achieved it after 6 nodes and 0 milliseconds. However, the time elapsed does not seem to be very accurate for most of the cases, as many of the times it finds a solution in less than 1 millisecond. 

