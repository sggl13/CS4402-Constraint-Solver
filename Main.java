import java.util.ArrayList;
/*
 * 180008512
 */

import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		System.out.println("Starting...");
		
		boolean exit = false;
		
		while(!exit) {
			
			System.out.println("\n  ========  MENU  ========");
			System.out.println("  1. Forward Checking\n  2. Maintaining Arc Consistency\n  0. Exit");
			System.out.println("  ========================");
			System.out.println("\nPlease choose an option from the menu (0..2): ");
			
			Scanner scan = new Scanner(System.in);
			int option = scan.nextInt();
			
			String instance = "";
			
			if(option != 0) {
				System.out.println("\nPlease enter the filename of the instance: ");
				instance = scan.next();
			}
			
			switch(option) {
				case 1:
						FC fcSolver = new FC(instance);
						break;
				case 2:
						MAC macSolver = new MAC(instance);
						break;
				case 0:
						System.out.println("\nClosing the program...");
						exit = true;
						break;
			}
		}
		//FC macSolver = new FC("8Queens.csp");
	}
}

