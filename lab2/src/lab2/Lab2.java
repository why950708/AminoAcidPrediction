package lab2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class Lab2 {

	public static void main() {

	}

	String[] stringArray = null;

	public ArrayList<Protein> fileParsing(String name) {

		try (Stream<String> stream = Files.lines(Paths.get(name))) {
			// Get the files in and make it into a String array line by line
			stringArray = stream.toArray(size -> new String[size]);
			// stream.forEach(System.out::println);

		} catch (IOException e) {
			e.printStackTrace();
		}
		ArrayList<Protein> retunVal = new ArrayList<Protein>();
		ArrayList<Integer> list = new ArrayList<Integer>();
		int index = 0;
		//Skip the first Few Lines
		while(index < stringArray.length && (stringArray[index].startsWith("#")||stringArray[index].length()==0))
		{
				index++;
		}
		
		//The first protein start
		list.add(index);

		while(index < stringArray.length)
		{
			if(stringArray[index].contains("<>"))
			{
				list.add(index+1);
				if(!stringArray[index-1].contains("end"))
				list.add(index-2);
				else
				list.add(index-1);
			}
			
			index++;
		}

		for(int)
		
		
		return returnVal;
	}
	
	
	
}

class Protein {

ArrayList<String> pairs;

public Protein(String[] pairsIn)
{	
	this.pairs = new ArrayList<String>(Arrays.asList(pairsIn));
	for(int i=0;i<8;i++)
	{
		this.pairs.add("Z");
	}
}

public String getNext(int pos)
{
	return pairs.get(pos);
}

public int length()
{
	return pairs.size();
}



}


