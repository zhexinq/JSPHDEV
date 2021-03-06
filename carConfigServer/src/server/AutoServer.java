/**
 * @author zhexinq
 * AutoServer interface to be implemented by BuildAuto and BuildCarModelOptions
 */
package server;

import util.Properties;

import java.util.ArrayList;
import model.Automobile;

public interface AutoServer {
	public boolean loadPropsToAuto(Properties p);
	public ArrayList<String> getAvailableModels();
}
