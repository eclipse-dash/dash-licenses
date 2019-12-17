package org.eclipse.dash.bom;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class LicenseFinder {

	public static void main(String[] args) {
		Settings settings = Settings.getSettings(args);
		if (settings == null) return;
		
        Arrays.stream(args).forEach(name -> {
        	DependencyListReader reader = null;
			try {
				reader = getReader(name);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	if (reader != null) {
        		List<IContentId> dependencies = reader.iterator();
        		
        		LicenseChecker checker = new LicenseChecker(settings);
        		checker.getLicenseData(dependencies, data -> {
        			System.out.println(data.getId() + data.getLicense());
        		});    		
        	}
        });
	}
	
	private static DependencyListReader getReader(String name) throws FileNotFoundException {
    	if ("-".equals(name)) {
    		return new FlatFileReader(new InputStreamReader(System.in));
    	} else {
    		File input = new File(name);
    		if (input.exists()) {
				return new FlatFileReader(new FileReader(input));
    		}
    	}
    	return null;
	}
}
