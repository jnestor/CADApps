package edu.lafayette.vcache.core;
import java.io.*;

/** create a stream of traces from an input stream formatted in the DineroIV trace format. */

/*----------------------------------------------------------------------*/ 
/*                                                                      */
/* Reads access data from 3 columns:                                    */
/*    type(int)     address(hex)       size of word in bytes(int)       */
/*                                                                      */
/*----------------------------------------------------------------------*/ 

public class D4Trace {
    InputStream str;

    // constants used by DineroIV format
    public static final int DATA_READ = 0;
    public static final int DATA_WRITE = 1;
    public static final int INSTR_FETCH = 2;
    public static final int MISC_ACCESS = 3;
    public static final int COPY_BACK = 4;
    public static final int INVALIDATE_BLOCK = 5;
    
    public D4Trace(InputStream str) {



    }

    public CacheAccess nextAccess() {
    	return null; // scaffolding
    }
  
}
