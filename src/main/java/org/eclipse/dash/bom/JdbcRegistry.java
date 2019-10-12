package org.eclipse.dash.bom;

import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.derby.jdbc.EmbeddedDriver;

public class JdbcRegistry implements ContentRegistry {
	public JdbcRegistry() {

		try {
			DriverManager.registerDriver(new EmbeddedDriver());
			DriverManager.getConnection("jdbc:derby:bom;create=true");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public ContentInfo find(ContentId id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cache(ContentInfo each) {
		// TODO Auto-generated method stub
		
	}
}
