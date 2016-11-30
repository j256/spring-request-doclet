package com.j256.springrequestdoclet.writer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.j256.springrequestdoclet.collector.EndPoint;

/**
 * Class which writes out our end-point documentation.
 * 
 * @author graywatson
 */
public interface EndPointMapWriter {

	/**
	 * Write our end-point information out.
	 */
	public void write(Map<String, List<EndPoint>> endPointMap) throws IOException;
}
