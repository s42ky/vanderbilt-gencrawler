package edu.vanderbilt.webtest.plugins;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.state.StateVertix;

import edu.vanderbilt.webtest.model.visitors.StringVisitor;
import edu.vanderbilt.webtest.model.visitors.StringVisitorIgnoreValues;

public class StateLogger implements OnNewStatePlugin {
    private final String basedir;
    
    public StateLogger(String logdir) {
    	basedir = logdir;
    	
    	File dir = new File(basedir);
        if(!dir.exists()) dir.mkdir();
    }
    
    void OnNewStatePlugin() {
        //Clear files
        File dir = new File(basedir);
        
        for(File f : dir.listFiles()) {
            String fn = f.getName();
            if(fn.endsWith(".log"))
            	f.delete();
        }
        //Reset all states log
        //BufferedWriter bw = new BufferedWriter(new FileWriter(basedir+"allstates.log", false));
        //bw.write("\n");
        //bw.close();
    }
	
	public void onNewState(CrawlSession session) {
		StateVertix state = session.getCurrentState();
		PageTreeVertixIdentifier vtxID = (PageTreeVertixIdentifier) state.getIdentifier();
                
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(basedir+state+".log", false));
            bw.write(state.getDom());
            bw.write("\n\n=============================PageTree===============================\n\n");
            StringVisitor ptsv = new StringVisitor();
            vtxID.visitPageTree(ptsv);
            bw.write(ptsv.getResult());
            bw.write("\n\n=========================PageTreeIgnoreValues=======================\n\n");
            StringVisitor ptsviv = new StringVisitorIgnoreValues(false);
            vtxID.visitPageTree(ptsviv);
            bw.write(ptsviv.getResult());
            bw.write("\n\n=========================parameterlessLookup========================\n\n");
            bw.write(vtxID.getParameterlessLookup());
            bw.write("\n\n============================parameterSet============================\n\n");
            for(String key : vtxID.getParameterIdentifiers()) {
            	bw.write(key+": ");
            	bw.write(vtxID.getValuesForParameter(key).toString());
            	bw.write("\n");
            }
            bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
