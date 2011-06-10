package org.openedit.webdav;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {
	public static Test suite()
	{
		TestSuite suite = new TestSuite( "Test for entermedia" );
		
		suite.addTestSuite( RangeTest.class );
		
		return suite;
	}
}
