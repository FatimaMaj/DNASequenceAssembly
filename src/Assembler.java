

import java.io.*;
import java.util.*;

public class Assembler {

	private String fileName;
	private List<String> fragments = new LinkedList<String>();
	private int agentSize = 100;
	private int InitialModelSize;
	private String ss; // search space
	private int ssSize;
	private int hypoPos;
	private String Initialmodel;
	private String model;
	private int modelSize;
	ArrayList<Agent> agents = new ArrayList<Agent>();
	private int activeCount;
	private int modelPos;
	private Random r = new Random();
//	private StringBuilder sb_ss = new StringBuilder();
	private int modelIndex;
	private int matchFrag; 
//	private int modelsizeforEqualMethod ;	
	public static boolean passAllFrag = false;
	
	ArrayList<HighSimilarity> highsimilarity = new ArrayList<HighSimilarity>();
	
	// CONSTRUCTORS
	public Assembler( String fileName ) throws IOException
	{
		this.fileName = fileName;
		createFragments();
	}
	
	public Assembler( List<String> fragments )
	{
		this.fragments = fragments;
	}
	
	public Assembler( String fileName, int InitialModelSize, int agentSize ) throws IOException
	{
		this.fileName = fileName;
		this.InitialModelSize = InitialModelSize;
		this.agentSize = agentSize;	
		createFragments();
	}
	
	public Assembler( List<String> fragments, int InitialModelSize, int agentSize )
	{
		this.fragments = fragments;
		this.InitialModelSize = InitialModelSize;
		this.agentSize = agentSize;
	}
	
	// ======================================
	//read from search space file that has the entire fragments
	public void createFragments() throws IOException
	{
		String line = null;		
		BufferedReader F_ss = null; // search space file

		try {
			F_ss = new BufferedReader(new FileReader( fileName ) ); 
			
			line = F_ss.readLine(); 
			while (line != null) {
				//convert all the (input) fragments to lower case
				line=line.toLowerCase().trim();
				//sb_ss.append(line);
				fragments.add(line);
				line = F_ss.readLine();
			}
			
		} finally {
			F_ss.close();
		}
		
		for (int f=0; f<fragments.size(); f++)
		{
			fragments.get(f);
		}
	}
	
	public List<String> setOrientation() throws FileNotFoundException, UnsupportedEncodingException{
		
		List<String> finalFrags = new LinkedList<String>(); 
		boolean findMatchInReverseFrag  = true; // find match in reverse fragment
		boolean findMatchInHealthyFrag = true; // find match in NOT reverse(healthy) fragment
		int sizeOfPreviousFF=0;  // size of previous first fragment 
		int sizeOfCurrentFF=0;  // size of current first fragment (assembled)
		
		System.out.println("PLEASE WAIT........!");	
		sizeOfPreviousFF = fragments.get(0).length();
		
		while (true)
		{
			//healthy means the fragment that is not reverse and the orientation have not been change
		    findMatchInHealthyFrag = true;
			findMatchInReverseFrag = true; //the fragment that has unknown orientation
		
			assemble(); // start assembling		
			sizeOfCurrentFF = fragments.get(0).length();
			
			// if assemble any fragment ((if find match))
			if (sizeOfCurrentFF > sizeOfPreviousFF )
			{
				// find a match during searching	
				findMatchInHealthyFrag = true; 										   
				finalFrags.add(fragments.get(0));
			}
			else
				// doesn't find a match during searching
				findMatchInHealthyFrag = false; 
			// update the size of Previous first fragment
			sizeOfPreviousFF = sizeOfCurrentFF; 
			
			//Reverse the bases and change the orientation of first fragment(assembled fragment)		
			CreateReverseFirstFrag(fragments.get(0));  
			
			System.out.println("****After_Reverse: "+fragments.get(0));
			System.out.println("*********************");
			
			//start assembling
			assemble();
		
			sizeOfCurrentFF = fragments.get(0).length();
			// if assemble any fragment (if find a match)
			if (sizeOfCurrentFF > sizeOfPreviousFF )
			{
				findMatchInReverseFrag = true; // find a match during searching 
				finalFrags.add(fragments.get(0));
			}
			else
				// dosen't find any match during searching 
				findMatchInReverseFrag = false; 
		
			//Reverse the bases and change the orientation of first fragment(assembled fragment)		
			CreateReverseFirstFrag(fragments.get(0)); 
	
			// if dosn't find match model in both state of fragment (healthy and unhealthy)
			if (findMatchInReverseFrag == false && findMatchInHealthyFrag == false)
			{
			
				PrintWriter remainingFragmentFile = new PrintWriter("remainingFragment.txt", "UTF-8");
				
				/*
				 * Explanation for following 'if' condition:
				 * write 2 because finally two fragments have remained:
				 * The first fragment is assembled fragment;
				 * the second fragment is search space. 
				 * it passes all fragments. and delete the match fragment
				 * *use the "passAllFrag" in main method.
				 * after first assembling, the program try to assemble the remaining fragments
				 * until all fragments participate in the assembling. 
				 */
				if (fragments.size()<2)
					passAllFrag = true; // pass through all fragments
					
				for ( int i = 1; i < fragments.size(); i++ )
					remainingFragmentFile.print( fragments.get(i)+"\n" );		
				
				remainingFragmentFile.close ();
				System.out.println("Starting new contig");
				break;
			}
		}//end while
		
		finalFrags.add(fragments.get(0));
		System.out.println("==============================");
		System.out.println("GlobalActiveAgent: \t" + globalActiveAgent + "\n\n" + 
				"IterationCounter: \t" + (iterationCounter-1) + "\n\n");
		return finalFrags;	
	}
	public List<String> assemble() 
	{
		List<String> finalFrags = new LinkedList<String>(); 
		boolean foundM1=false;
		boolean foundM2=false;
		
		//While not finding model from the beginning and start of fragment
		while (true)
		{
			foundM1 = false;
			foundM2 = false;			
//*************** get_Model_from_End_of_Fragment ***************//
			//get model from the end of first fragment(assembled fragment)
	
			model = ModelfromEndofFrag(fragments.get(0));
			Initialmodel = model; 
			System.out.println("Model from End:  "+ model);
			
			//number of match model in the search space
			// the match models that is get from end of fragment
			int numOfMatchModelEnd=0; 
			
			//start searching, fragment by fragment
			for(int i=1; i<fragments.size(); i++)
			{		
				//get fragment as search space
				ss = fragments.get(i); // ss -> search space		
				System.out.println("Search Space:  " + ss +"\tSearch Space #"+i );
				System.out.println("Initial model: "+Initialmodel);
				modelPos = SDS();
				//if SDS find a match model
				if ( modelPos != -1 )
				{
					matchFrag = i;
					//get position of the match model
					modelIndex = fragments.get(matchFrag).indexOf(model);
					//extend the model 
					model = extendModelOfEndOfFirstFragment(modelIndex, model);
					//if model doesen't reach to the start of match fragment
					if (model == "*")
						continue;
					System.out.println("FOUND!  PASSED in ====> Choose model from the end");
					System.out.println("Model_After_Extending: "+model);
					foundM1 = true;
					modelSize = model.length();
					
					//for finding the maximum similarity fragments
					highsimilarity.add(new HighSimilarity(matchFrag, modelSize));
					highsimilarity.get(numOfMatchModelEnd).setModelSize(modelSize);
					highsimilarity.get(numOfMatchModelEnd).setFragNum(matchFrag);
					
					System.out.println("Match Fragment: "+fragments.get(matchFrag)+"\tMatch_Fragment#: "
					+matchFrag);
					System.out.println("==================");
					
					/*  When SDS find a match model in every fragment of
					 *  search_space the number of match model increase 
					 */
					++numOfMatchModelEnd;
					// again the extended model get the Initial model for the next searching cycle
					model = Initialmodel;			
				}//end if
				else System.out.println("NOT FOUND! Failed in ====> "
						+ "fining the match model from the end of fragment");
			}//end for
			
			/*
			 * if model is found
			 * Select the fragment with maximum similarity and assemble it
			 * (assigning the selected fragment)
			 */
			if ( foundM1 == true ) 
			{	
				int largestModelEnd = 0;
				int largestPos=0;
			
				for(int i =0; i<numOfMatchModelEnd; i++) {
					if(highsimilarity.get(i).getModelSize() > largestModelEnd) {
						largestModelEnd = highsimilarity.get(i).getModelSize();	            	
						largestPos = i;
	            	}//end if
				}//end for
				System.out.println("Selected_Fragment#: "+highsimilarity.get(largestPos).getFragNum()+
						"\t"+ fragments.get(highsimilarity.get(largestPos).getFragNum()));
				
				/* highsimilarity.get(largestPos).getFragNum() ->
				 * is the selected fragment that has maximum similarity
				 */
				//Assemble the match fragment
				CreateFirstFragM1(fragments.get(0), largestModelEnd,
						highsimilarity.get(largestPos).getFragNum()); 
			
				finalFrags.add(fragments.get(0));
				// remove the match fragment from search space
				fragments.remove(highsimilarity.get(largestPos).getFragNum());
				// TO SEE THE NEW FIRST FRAGMENT:
				System.out.println("First Fragment after assembling:: " + fragments.get(0)); 
			}//end if
			
			
//****************Model_from_Start_of_Fragment****************//	
			System.out.println("************* Model_from_Start_of_Fragment *************");
			//get model from the start of first fragment(assembled fragment)
			model = ModelfromStartofFrag(fragments.get(0));
			Initialmodel = model;
			System.out.println("Model from start:  "+ model);
			
			//number of match model that is have been taken from start of first fragments
			int numOfMatchModelStart=0;
			//start searching, fragment by fragment
			for(int i=1; i<fragments.size(); i++)
			{
				//get fragment as search space
				ss = fragments.get(i);//ss is -> search space
				System.out.println("Search Space:  " + ss+"\tSearch Space #"+i );
				System.out.println("Initial Model From Start:  "+ Initialmodel);
				//start searching the model 
				modelPos = SDS();
				//if SDS find match model
				if ( modelPos != -1 ){//else go to the next fragment in the search space	
					matchFrag = i;
					modelIndex = fragments.get(matchFrag).indexOf(model);
					model = extendModelOfStartOfFirstFragment(modelIndex, model);		
					//if extended model doesen't reach to the end of match fragment
					if (model == "*")
						continue;
					
					System.out.println("Found! PASSED in ====> Choose model from the beginning");
					System.out.println("Model_2 After Extending: "+model);
					System.out.println("Model Type 2:  " + model);
					
					foundM2 = true;
							
					modelSize = model.length();
			
					//processing for finding maximum similarity fragment		
					highsimilarity.add(new HighSimilarity(matchFrag, modelSize));
					highsimilarity.get(numOfMatchModelStart).setModelSize(modelSize);
					highsimilarity.get(numOfMatchModelStart).setFragNum(matchFrag);
					
					System.out.println("Match Fragment(model_from_start_state): "+fragments.get(matchFrag));
					System.out.println("==================");
					System.out.println("Frag-pos & modelSize: "+
					highsimilarity.get(numOfMatchModelStart).getFragNum()
					+"    "+highsimilarity.get(numOfMatchModelStart).getModelSize());
		            /*  When SDS find a match model in every fragment of
			         *  search_space, the number of match model increase: 
			         */
					++numOfMatchModelStart;
					// again the extended model get the Initial model for next searching period
					model = Initialmodel;
				}//end if	
				else System.out.println("NOT FOUND! Failed in ====> "
						+ "fining the match model from the start of fragment");
			}//end for
			
			//Select the fragment with maximum similarity and assemble it
			//if find Model from Start == true)
			if (foundM2 == true )
			{			
				int largestModelStart = 0;
				int largestPos2=0;
				
				for(int i =0; i<numOfMatchModelStart; i++) {				
					if(highsimilarity.get(i).getModelSize() > largestModelStart) {
						largestModelStart = highsimilarity.get(i).getModelSize();
						largestPos2 = i;
					}
			     }//end for
				/* highsimilarity.get(largestPos2).getFragNum() ->
				 * is the selected fragment that has maximum similarity.
				 * fragments.get(0)->is the assembled fragment that increases in 
				 * every stage when find a suitable match
				 * largestModelStart-> the size of largest model that program find 
				 */
				CreateFirstFragM2(fragments.get(0), largestModelStart,
						highsimilarity.get(largestPos2).getFragNum());
				//	finalFrags.add(fragments.get(0));
				
				//Remove the match fragment from search space after assembling
				fragments.remove(highsimilarity.get(largestPos2).getFragNum());
				System.out.println("First Fragment after assembling: " + fragments.get(0));
				System.out.println("====================");
			}
					
			if ( foundM1 == false && foundM2 == false ){
				System.out.println("Neither model from end nor model "
						+ "from start is found so searching is finished"
						+"  "+fragments.get(0));
				break;
			}
		}// end while 
	
		// finalFrags.add(fragments.get(0));
		System.out.println("==============================");	
		return finalFrags; //return final assembled fragment 
	}
	
	//find model by equals method
	public int findModel( String SearchSpace, String myModel)
	{	
		int modelLength = myModel.length();	
		for ( int i = 0; i < ss.length()-modelLength+1; i++ ) 
		{	
			String temp = ss.substring(i, i+modelLength); 		
			if ( temp.equals(myModel) )
			{
				System.out.println("MODEL_POS: "+i+"   Model_Length: "+modelLength);			
				return i;
			}
		}	
		return -1;
	}
	// for reversing and changing the orientation of fragment  
	public void CreateReverseFirstFrag(String firstFrag) {
		
		firstFrag = firstFrag.replaceAll("(?i)a", "w");
		firstFrag = firstFrag.replaceAll("(?i)t", "x");
		firstFrag = firstFrag.replaceAll("(?i)c", "y");
		firstFrag = firstFrag.replaceAll("(?i)g", "z");
	
		firstFrag = firstFrag.replace("w", "t");
		firstFrag = firstFrag.replace("x", "a");
		firstFrag = firstFrag.replace("y", "g");
		firstFrag = firstFrag.replace("z", "c");
		
		firstFrag = new StringBuilder(firstFrag).reverse().toString();
		fragments.set(0, firstFrag);	
	}
	
	// Assembling *** get model from end of fragment
	//Delete the same characters (overlap) from the first fragment
	public void CreateFirstFragM1(String s1 , int modelSize, int matchFrag) {
		String  firstFrag = "";
	   s1 = s1.substring(0, s1.length()-modelSize);
	   if (s1.length()==0)
	   {
		   firstFrag = fragments.get(matchFrag);
	   }
	   else
	     firstFrag = s1+fragments.get(matchFrag);
		fragments.set(0, firstFrag);	
	}
	
	// Assembling *** get model from start of fragment
	// Delete the same characters (overlap) from the first fragment
	public void CreateFirstFragM2(String s1, int modelSize, int matchFrag) {
		String  firstFrag = "";
		  s1 = s1.substring(modelSize, s1.length());
		  
		  if (s1.length()== 0)
		  {
			   firstFrag = fragments.get(matchFrag);	  
		  }
		   else	  
		 firstFrag = fragments.get(matchFrag)+s1;
		fragments.set(0, firstFrag);
	}
	
	public String CreateSearchSpace(int fragSize) {
		String SS_frags = "";

		for (int i = 1; i < fragSize; i++) { 
			SS_frags += fragments.get(i); 
		}
		return SS_frags;
	}
	// get Model from the end of fragment
	public String ModelfromEndofFrag(String frag) {
		String model_frag = "";
		model_frag = frag.substring(frag.length() - InitialModelSize, frag.length());
		return model_frag;
	}
	// get Model from the beginning of the fragment
	public String ModelfromStartofFrag(String frag) { 
		String model_frag = "";
		model_frag = frag.substring(0, InitialModelSize);	
		return model_frag;
	}
	
	// Model is on the end (or right) of first fragment 
	public String extendModelOfEndOfFirstFragment(int modelIndex, String m) {	
		 String model = m;
			 
		//modelIndex --> starting point of model of match fragment
		char CharofFirstFrag = 0; //char of first fragment before model
		char SSFragChar = 0; //char of search space fragment (match fragment)
		//index of char before model of first fragment
		int modIndxFF = fragments.get(0).length()-model.length(); 
		while (modelIndex > 0){ 
			
			modelIndex--;
			SSFragChar = CharofFirstFrag = fragments.get(matchFrag).charAt(modelIndex);
			modIndxFF--;
			if (modIndxFF < 0 ) break;
			CharofFirstFrag = fragments.get(0).charAt(modIndxFF);
			
			//model.substring(letterPickedNum, 
			// letterPickedNum+microF).compareToIgnoreCase(ss.substring(agents.get(i).getH() 
			 //+ letterPickedNum,agents.get(i).getH() + letterPickedNum+microF ) ) ==0
			if (SSFragChar == CharofFirstFrag)
			{
				model = CharofFirstFrag+"" + model;
			    //modelsizeforEqualMethod = model.length(); //is for equal method
			}
			else{//if model doesen't reach to the start of match fragment
				if (modelIndex > 0)
					return "*";
					break;
				}	
		}
		return model;
	}
	
	// Model is on the start (or left) of first fragment 
	//extend Model From Start of first fragment
	public String extendModelOfStartOfFirstFragment(int modelIndex, String m) {	
		//modelIndex --> starting point of model of match fragment
	    String model = m;
		char CharofFirstFrag = 0; //char of first fragment after model
		char SSFragChar = 0; //char of search space fragment (match fragment)
		int modIndxFF = model.length(); //index of char after model in first fragment 

		int endOfFrag = fragments.get(matchFrag).length();		
		modelIndex =  modelIndex+model.length(); //index after model in the match fragment
		
		while (modelIndex < endOfFrag){
		
			if (model.length() >= fragments.get(0).length())
				break;			
			SSFragChar = fragments.get(matchFrag).charAt(modelIndex);
			CharofFirstFrag = fragments.get(0).charAt(modIndxFF);
						
			if (SSFragChar == CharofFirstFrag){
				model = model+CharofFirstFrag+"";
				//modelsizeforEqualMethod = model.length();	//is for equal method	
			}		
			else{//if model doesen't reach to the end of match fragment
				if ( (modelIndex < endOfFrag)) 
					return "*";	
					break;	
				}	
            modelIndex++;
			modIndxFF++;	
		}
		return model;
	}
	
	public int SDS(){			
		modelSize = model.length();
		ssSize = ss.length();	
		initialise();
			
		activeCount = 0;
		//iteration 	
		for(int i=0; i<50; i++){
			test();
			diffusion();
		}	
			
		int NumShouldBeActic = (100*agentSize)/100; //% of agents should be active	
		System.out.println("Active_Count: \t" + activeCount);
		if ( activeCount >= NumShouldBeActic)
			return agents.get(0).getH();			
		else
			return -1;
	}	
	
	public void initialise() {
		agents = new ArrayList<Agent>();

		for (int i = 0; i < agentSize; i++) {
			hypoPos = r.nextInt(ssSize - modelSize + 1);
			agents.add(new Agent(hypoPos, false)); 			
		}
	}	
	int iterationCounter = 0;
	int globalActiveAgent = 0;
	public void test() {
		activeCount = 0;
		int letterPickedNum = 0;
		int microF = (int)(modelSize/10)+1; //microF should not be zero
			
		for (int i = 0; i < agents.size(); i++) {
			
			if ( modelSize == microF )
				letterPickedNum = 0;
			else
				letterPickedNum = r.nextInt(modelSize - microF);
			
			 //if model and specific part of search space is equal
			 //str1.compareToIgnoreCase(str2);
			 if ( model.substring(letterPickedNum, 
					 letterPickedNum+microF).compareTo(ss.substring(agents.get(i).getH() 
					 + letterPickedNum,agents.get(i).getH() + letterPickedNum+microF ) ) ==0 )
			 {
				activeCount++;
				// the current agent get active label
				agents.get(i).status = true; 	
			} else {
				// the current agent get inactive label
				agents.get(i).status = false; 
			}	
		}
		globalActiveAgent = globalActiveAgent + activeCount;
		iterationCounter++;
	}//end test

	public void diffusion() {
		int RAgent;
		int RHyp = 0;
		for (int i = 0; i < agents.size(); i++) {
			if (agents.get(i).status == false) {
				RAgent = r.nextInt(agents.size());
				
				// if random agent is the same as current agent
				// select another random agent
				while ( i == RAgent ) 
					RAgent = r.nextInt(agents.size());
				
				if (agents.get(RAgent).getS() == true) {
					agents.get(i).setH(agents.get(RAgent).getH());
					
				} else if (agents.get(RAgent).getS() == false) {
					RHyp = r.nextInt(ssSize - modelSize + 1);
					agents.get(i).setH(RHyp);
					}
			} else if (agents.get(i).status == true) {
				//do nothing
			}
		}//end for
	}//end diffusion
}