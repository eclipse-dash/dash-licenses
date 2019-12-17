package org.eclipse.dash.bom;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dash.bom.LicenseSupport.Status;

public class LicenseFinder {

	public static void main(String[] args) {
		ISettings settings = CommandLineSettings.getSettings(args);
		if (settings == null) return;
		
        Arrays.stream(args).forEach(name -> {
        	IDependencyListReader reader = null;
			try {
				reader = getReader(name);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	if (reader != null) {
        		List<IContentId> dependencies = reader.iterator();
        		
        		LicenseChecker checker = new LicenseChecker(settings);
        		checker.getLicenseData(dependencies, (data, status) -> {
        			// FIXME Support different options for output.
        			// CSV for now.
        			System.out.println(String.format("%s, %s, %s, %s", data.getId(), data.getLicense(), status == Status.Approved ? "approved" : "restricted", data.getAuthority()));
        		});    		
        	}
        });
	}
	
	private static IDependencyListReader getReader(String name) throws FileNotFoundException {
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
