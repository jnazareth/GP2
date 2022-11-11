package GP2;

import GP2.cli.gpcli;
import GP2.utils.Utils;

/*
-------------------
history
-------------------
v2.0: major enhancement: XLS automation
v21: Refactor cleanup. Remove multiple classes.
v20: InputProcessor, -clean, Person(EnumMap) implementation. Refactored.
v19: read separator switched to comma & quoted strings from tab
v18: control header
v17: group implementation
v16: cleanup: created separete account, removed static declarations
v15: added category & vendor
v14: bug fix individual transaction amt
v13: added additional header, fixed sys formatting
v12: individual transaction amounts added
v11: added optional "sys" formatting
v10: "sys" account added to output
v9: csv formatting fixed (padding tabs)
v8: "sys" transaction implementation
v7: introducted individual checksum
v6: "percentage" transaction, correct implementation
v5: discard: "percentage" transaction, poor implementation
v4: "clearing" transaction implementation
v2: export to csv implementation
*/

// Import log4j classes.
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class gp2
{
    private static final Logger logger = LogManager.getLogger(gp2.class);

	// ----------------------------------------------------
	// main
	// ----------------------------------------------------
	public static void main (String[] args)
	throws Exception
	{
        logger.trace("Entering application.");

        gpcli gCLI = new gpcli();
        Utils.m_settings = gCLI.parseCommandLine(args);

		//System.out.println("settings" + Utils.m_settings);
		gCLI.processCommandLine();
	}
}
