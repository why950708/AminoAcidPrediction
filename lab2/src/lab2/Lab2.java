package lab2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Lab2 {

	public static void main() {

	}

	String[] stringArray = null;

	public Protein[] fileParsing(String name) {

		try (Stream<String> stream = Files.lines(Paths.get(name))) {
			// Get the files in and make it into a String array line by line
			stringArray = stream.toArray(size -> new String[size]);
			// stream.forEach(System.out::println);

		} catch (IOException e) {
			e.printStackTrace();
		}
		Protein[] returnVal = new Protein[stringArray.length];
		return returnVal;
	}
}

class Protein {


public Protein(int numOfAcid, )
{
	ArrayList<String> = 
}
}