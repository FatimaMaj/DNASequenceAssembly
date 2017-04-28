/*
 * Author: Fatimah Majid al-Rifaie
 * Submit date: June 2014
 * In this project I implement SDS (an Artificial intelligence algorithm)
 * to find a pattern from search space.
 */


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Assemble {	
	public static void main(String[] args) throws IOException {	
		PrintWriter resultFile = new PrintWriter("Output.txt", "UTF-8");
		int L = 0;	
		int agentSize = 100;
		int modelLength = 5; //for big dataset//20; 	
		// write the file name:
		Assembler assembler = new Assembler("sampleTest_01.txt", modelLength, agentSize); 
		List<String> fragments = new LinkedList<String>();	
	
		/*
		 *USE modelLength = 5, for sampleTest files
		 *sampleTest_01.txt // comment the 'CreateReverseFirstFrag' method due to
		 *					// (sampleTest_01) has characters other than (ATCG)
		 *sampleTest_02.txt   
		 *sampleTest_03.txt
		 * 
		 * NAME OF FRAGMENT FILES THAT CAN BE TESTED
		 * USE modelLength = 20 for following files
		 * x60189_4.txt
		 * frag_x60189_4.txt
		 * 
		 * x60189_5.txt
		 * frag_x60189_5.txt
		 * 
		 * x60189_6.txt
		 * frag_x60189_6.txt
		 * 
		 * x60189_7.txt
		 * frag_x60189_7.txt
		 *	
		 *complete contig has 20100 bases according to the papers
		 * frag_j02459_7.txt  
		 * j02459_7.txt
		 *
		 * m15421_5.txt 
		 * frag_m15421_5.txt
		 * 
		 * m15421_6.txt   		//generates 2 sequences***
		 * 				  		// The second sequence is generated in reverse orientation
		 * frag_m15421_6.txt
		 * 
		 * m15421_7.txt  		//generates 2 sequence  
 						  		//the second sequence is generated in reverse orientation
		 * frag_m15421_7.txt  	//generate 2 sequence 
		 * 
		 * 38524243_7.txt    	//long file (first contig: 58,124 characters;
		 * 					 	//second contig: 18870 characters)
		 * frag_38524243_7.txt  //is like (38524243_7.txt) 
		 */
		
		Pattern p;
		Matcher matches = null;
		//while pass all fragments in the file
		while (Assembler.passAllFrag == false)		
		{
			L++; //level
			fragments = assembler.setOrientation();
			
			/* Regarding following 'for' loop:
			 * (i+=2)--> to doesn't show the reverse result of fragment
			 * Although sometime it shows the reverse. using (i+=2)to decrease
			 * showing the number of reverse fragments as less as possible
			 * i++ --> show all results (in both orientation). 
			 * If there is result in the output file that is
			 * completely different with the result of real data_set file,
			 * means that orientation of that result is changed
			 */
			String str =null;
			for ( int i = 0; i < fragments.size(); i+=2) //i++
			{
				resultFile.println(fragments.get(i));
				resultFile.println(" Finish Level "+L);
				System.out.println("Part of result: #"+L+" "+fragments.get(i));
				str = fragments.get(i);			
			}
			System.out.println("Complete_Result #"+L+": "+str);
			System.out.println("********** The end of Level "+L +"**********");
						
			/***For deleting duplicate fragment***/		
			Scanner s = new Scanner(new File("remainingFragment.txt"));
			ArrayList<String> list = new ArrayList<String>();
			while (s.hasNext()){
			    list.add(s.next());
			}		
			// Remove the duplicate fragments
			for (int i = 0; i < list.size(); i++) {
				p = Pattern.compile(list.get(i), Pattern.CASE_INSENSITIVE);
				matches = p.matcher(fragments.toString());
				if (matches.find())
				{
					 System.out.println("Found : "+list.get(i));
					 list.remove(i);
					 System.out.println("Number of remaining fragments after removing: " + list.size());				
				}
			}
			//if all the fragments are removed
			if (list.size()<0)
				break;
			//Contains remaining fragments
			PrintWriter SecondRemaining = new PrintWriter("SecondRemaining.txt", "UTF-8");
			
			for ( int i = 0; i < list.size(); i++ ) {
				SecondRemaining.print(list.get(i)+"\n");
			}	
			SecondRemaining.close();	
			assembler = new Assembler("SecondRemaining.txt", modelLength, agentSize);		
			s.close();			
		}//end while		
		resultFile.close ();
		System.out.println("All fragments are assembled!");
	}
}//end class