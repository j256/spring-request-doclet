package com.j256.springrequestdoclet.writer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.j256.springrequestdoclet.collector.EndPoint;

/**
 * Definition of a class which writes out our path information.
 * 
 * @author graywatson
 */
public interface PathMapWriter {

	/**
	 * Write our path information out.
	 */
	public void write(Map<String, List<EndPoint>> endPointMap) throws IOException;
}
